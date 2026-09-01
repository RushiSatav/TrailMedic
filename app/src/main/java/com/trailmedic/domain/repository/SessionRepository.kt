package com.trailmedic.domain.repository

import com.trailmedic.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    fun getRecentSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun updateOutcomeNote(sessionId: String, outcomeNote: String)
    suspend fun deleteSession(id: String)
}
