package com.trailmedic.domain.usecase

import com.trailmedic.domain.ai.WildernessClinicalAIReasoner
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData
import com.trailmedic.domain.repository.ChatRepository
import com.trailmedic.domain.repository.SymptomTreeRepository
import com.trailmedic.utils.SettingsManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RunEmergencyInterviewUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiReasoner: WildernessClinicalAIReasoner,
    private val symptomTreeRepository: SymptomTreeRepository,
    private val settingsManager: SettingsManager
) {
    suspend operator fun invoke(
        category: EmergencyCategory,
        messages: List<Message>,
        phase: ConversationPhase,
        questionIndex: Int,
        onToken: (String) -> Unit
    ): String {
        val useLLM = try {
            settingsManager.useLLM.first()
        } catch (e: Exception) {
            true
        }

        val activeCategory = aiReasoner.detectEmergencyCategory(messages, category)
        val extract = aiReasoner.extractRelevantKnowledge(messages)
        val clinicalData = if (extract != null) {
            SymptomEmergencyData(
                id = extract.conditionTag.lowercase().replace(" ", "_"),
                name = extract.conditionName,
                triggerKeywords = listOf(extract.conditionTag),
                questions = listOf(extract.triageQuestion),
                firstAidSteps = extract.firstAidSteps,
                warningSigns = extract.warningSigns,
                evacuationNote = extract.evacuationNote
            )
        } else {
            symptomTreeRepository.getCategoryFallback(activeCategory)
        }

        return if (useLLM && chatRepository.isModelReady()) {
            try {
                val res = chatRepository.generateResponse(messages, phase, clinicalData, onToken)
                if (res.isBlank() || res.length < 25 || containsRefusal(res)) {
                    generateFromAIReasoner(activeCategory, messages, phase, questionIndex, onToken)
                } else {
                    res
                }
            } catch (t: Throwable) {
                generateFromAIReasoner(activeCategory, messages, phase, questionIndex, onToken)
            }
        } else {
            generateFromAIReasoner(activeCategory, messages, phase, questionIndex, onToken)
        }
    }

    private fun containsRefusal(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("cannot provide") ||
               lower.contains("can't provide") ||
               lower.contains("unable to provide") ||
               lower.contains("not able to provide") ||
               lower.contains("not a doctor") ||
               lower.contains("cannot give medical")
    }

    private suspend fun generateFromAIReasoner(
        category: EmergencyCategory,
        messages: List<Message>,
        phase: ConversationPhase,
        questionIndex: Int,
        onToken: (String) -> Unit
    ): String {
        val generatedText = aiReasoner.generateDynamicResponse(category, messages, phase, questionIndex)

        // Stream words smoothly for responsive, non-blocking UI
        val words = generatedText.split(" ")
        for (i in words.indices) {
            val word = if (i == 0) words[i] else " " + words[i]
            onToken(word)
            kotlinx.coroutines.delay(10)
        }
        return generatedText
    }
}

