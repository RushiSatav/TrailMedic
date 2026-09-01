package com.trailmedic.data.repository

import android.content.Context
import com.google.gson.Gson
import com.trailmedic.domain.model.EmergencyCategory
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
    private val gson: Gson
) : SymptomTreeRepository {

    private val emergencies: List<SymptomEmergencyData> by lazy {
        loadSymptomTree()
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

    override fun getAllEmergencies(): List<SymptomEmergencyData> = emergencies

    override fun getEmergencyById(id: String): SymptomEmergencyData? {
        return emergencies.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }

    override fun findMatchingEmergency(query: String): SymptomEmergencyData? {
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
