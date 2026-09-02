package com.trailmedic.domain.repository

import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData

interface ChatRepository {
    suspend fun generateResponse(
        conversationHistory: List<Message>,
        phase: ConversationPhase,
        clinicalData: SymptomEmergencyData? = null,
        onToken: (String) -> Unit
    ): String

    fun isModelReady(): Boolean
}
