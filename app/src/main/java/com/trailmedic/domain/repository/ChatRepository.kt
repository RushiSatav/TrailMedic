package com.trailmedic.domain.repository

import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message

interface ChatRepository {
    suspend fun generateResponse(
        conversationHistory: List<Message>,
        phase: ConversationPhase,
        onToken: (String) -> Unit
    ): String

    fun isModelReady(): Boolean
}
