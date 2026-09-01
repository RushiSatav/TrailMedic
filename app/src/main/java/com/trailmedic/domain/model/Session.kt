package com.trailmedic.domain.model

import java.util.UUID

data class Session(
    val id: String = UUID.randomUUID().toString(),
    val emergencyType: String,
    val messages: List<Message>,
    val firstAidSummary: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long,
    val outcomeNote: String = ""
)
