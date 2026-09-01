package com.trailmedic.domain.usecase

import com.trailmedic.domain.ai.WildernessClinicalAIReasoner
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.repository.ChatRepository
import com.trailmedic.utils.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RunEmergencyInterviewUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiReasoner: WildernessClinicalAIReasoner,
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

        return if (useLLM && chatRepository.isModelReady()) {
            try {
                chatRepository.generateResponse(messages, phase, onToken)
            } catch (t: Throwable) {
                generateFromAIReasoner(category, messages, phase, questionIndex, onToken)
            }
        } else {
            generateFromAIReasoner(category, messages, phase, questionIndex, onToken)
        }
    }

    private fun generateFromAIReasoner(
        category: EmergencyCategory,
        messages: List<Message>,
        phase: ConversationPhase,
        questionIndex: Int,
        onToken: (String) -> Unit
    ): String {
        val generatedText = aiReasoner.generateDynamicResponse(category, messages, phase, questionIndex)

        // Stream tokens smoothly for interactive UI
        for (char in generatedText) {
            onToken(char.toString())
        }
        return generatedText
    }
}

