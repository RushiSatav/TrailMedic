package com.trailmedic.data.repository

import com.trailmedic.data.llm.LLMInferenceEngine
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val llmEngine: LLMInferenceEngine
) : ChatRepository {

    override suspend fun generateResponse(
        conversationHistory: List<Message>,
        phase: ConversationPhase,
        onToken: (String) -> Unit
    ): String {
        return llmEngine.generateResponse(conversationHistory, phase, onToken)
    }

    override fun isModelReady(): Boolean {
        return llmEngine.isModelReady || llmEngine.isModelDownloaded()
    }
}
