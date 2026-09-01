package com.trailmedic.data.repository

import com.google.gson.Gson
import com.trailmedic.data.local.dao.SessionDao
import com.trailmedic.data.local.entity.SessionEntity
import com.trailmedic.domain.model.Session
import com.trailmedic.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val gson: Gson
) : SessionRepository {

    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override fun getRecentSessions(): Flow<List<Session>> {
        return sessionDao.getRecentSessions().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override suspend fun getSessionById(id: String): Session? {
        return sessionDao.getSessionById(id)?.toDomain(gson)
    }

    override suspend fun saveSession(session: Session) {
        val entity = SessionEntity.fromDomain(session, gson)
        sessionDao.insertSession(entity)
    }

    override suspend fun updateOutcomeNote(sessionId: String, outcomeNote: String) {
        sessionDao.updateOutcomeNote(sessionId, outcomeNote)
    }

    override suspend fun deleteSession(id: String) {
        sessionDao.deleteSession(id)
    }
}
