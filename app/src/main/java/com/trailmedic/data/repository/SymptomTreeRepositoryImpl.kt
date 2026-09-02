package com.trailmedic.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trailmedic.domain.ai.ClinicalKnowledgeExtractor
import com.trailmedic.domain.model.ClinicalExtractionResult
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.FirstAidIntentData
import com.trailmedic.domain.model.SymptomEmergencyData
import com.trailmedic.domain.model.SymptomTreeRoot
import com.trailmedic.domain.repository.SymptomTreeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SymptomTreeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val clinicalKnowledgeExtractor: ClinicalKnowledgeExtractor
) : SymptomTreeRepository {

    private val emergencies: List<SymptomEmergencyData> by lazy {
        loadSymptomTree()
    }

    private val cachedFirstAidIntents: List<FirstAidIntentData> by lazy {
        loadFirstAidIntents()
    }

    init {
        // Trigger dataset preloading and extractor initialization
        val em = emergencies
        val intents = cachedFirstAidIntents
        clinicalKnowledgeExtractor.loadDatasets(intents, em)
    }

    private fun loadSymptomTree(): List<SymptomEmergencyData> {
        return try {
            context.assets.open("symptom_tree.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val root = gson.fromJson(reader, SymptomTreeRoot::class.java)
                    root.emergencies
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadFirstAidIntents(): List<FirstAidIntentData> {
        // Try firstaidqa_v1.json first, fallback to first_aid_intents.json
        val fileNames = listOf("firstaidqa_v1.json", "first_aid_intents.json")
        for (fileName in fileNames) {
            try {
                context.assets.open(fileName).use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val listType = object : TypeToken<List<FirstAidIntentData>>() {}.type
                        val list: List<FirstAidIntentData>? = gson.fromJson(reader, listType)
                        if (!list.isNullOrEmpty()) {
                            return list
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }
        return emptyList()
    }

    override fun getAllEmergencies(): List<SymptomEmergencyData> = emergencies

    override fun getFirstAidIntents(): List<FirstAidIntentData> = cachedFirstAidIntents

    override fun getEmergencyById(id: String): SymptomEmergencyData? {
        val emMatch = emergencies.firstOrNull { it.id.equals(id, ignoreCase = true) }
        if (emMatch != null) return emMatch

        val intentMatch = cachedFirstAidIntents.firstOrNull {
            it.tag.equals(id, ignoreCase = true) || it.tag.replace(" ", "_").equals(id, ignoreCase = true)
        }
        return intentMatch?.toSymptomEmergencyData()
    }

    override fun extractKnowledge(query: String): ClinicalExtractionResult? {
        return clinicalKnowledgeExtractor.extractKnowledgeForPrompt(query)
    }

    override fun findMatchingEmergency(query: String): SymptomEmergencyData? {
        val extract = clinicalKnowledgeExtractor.extractKnowledgeForPrompt(query)
        if (extract != null) {
            val fromEm = emergencies.firstOrNull { it.id.equals(extract.conditionTag, ignoreCase = true) || it.name.equals(extract.conditionName, ignoreCase = true) }
            if (fromEm != null) return fromEm

            val fromIntent = cachedFirstAidIntents.firstOrNull { it.tag.equals(extract.conditionTag, ignoreCase = true) }
            if (fromIntent != null) return fromIntent.toSymptomEmergencyData()
        }

        val lowerQuery = query.lowercase()
        return emergencies.firstOrNull { emergency ->
            emergency.triggerKeywords.any { kw -> lowerQuery.contains(kw.lowercase()) }
        }
    }

    override fun getCategoryFallback(category: EmergencyCategory): SymptomEmergencyData {
        val directMatch = getEmergencyById(category.id)
        if (directMatch != null) return directMatch

        val keywordMatch = emergencies.firstOrNull { emergency ->
            val kwTokens = category.keywords.split(" ")
            kwTokens.any { emergency.triggerKeywords.contains(it) }
        }
        if (keywordMatch != null) return keywordMatch

        return emergencies.firstOrNull() ?: SymptomEmergencyData(
            id = "general",
            name = "Wilderness First Aid",
            triggerKeywords = listOf("help", "emergency"),
            questions = listOf(
                "Is the person conscious, breathing, and able to talk to you?",
                "Is there active severe bleeding or severe broken bone?",
                "Are they experiencing chest pain, severe cold, or difficulty breathing?",
                "What is their current physical and mental status?"
            ),
            firstAidSteps = listOf(
                "Ensure immediate scene safety from weather, rockfall, or wildlife.",
                "Check responsiveness (AVPU: Alert, Voice, Pain, Unresponsive) and breathing.",
                "Control any severe bleeding with firm direct pressure.",
                "Prevent hypothermia: insulate from ground and wrap in dry space blanket.",
                "Keep patient calm, hydrated (if conscious and not vomiting), and rested.",
                "Monitor vital signs every 15 minutes."
            ),
            warningSigns = listOf(
                "Loss of consciousness or unresponsiveness",
                "Rapid shallow breathing or blue/grey lips",
                "Severe bleeding that does not stop with pressure"
            ),
            evacuationNote = "Activate Satellite SOS or PLB. Note coordinates. Signal with 3 whistle blasts every minute."
        )
    }
}

