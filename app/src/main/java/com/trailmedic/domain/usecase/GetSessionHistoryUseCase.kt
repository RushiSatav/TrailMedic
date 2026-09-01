package com.trailmedic.domain.usecase

import com.trailmedic.domain.model.Session
import com.trailmedic.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionHistoryUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    fun getAllSessions(): Flow<List<Session>> = sessionRepository.getAllSessions()

    fun getRecentSessions(): Flow<List<Session>> = sessionRepository.getRecentSessions()

    suspend fun getSessionById(id: String): Session? = sessionRepository.getSessionById(id)

    suspend fun deleteSession(id: String) = sessionRepository.deleteSession(id)

    suspend fun updateOutcomeNote(sessionId: String, note: String) =
        sessionRepository.updateOutcomeNote(sessionId, note)
}
