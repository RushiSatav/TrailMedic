package com.trailmedic.domain.usecase

import com.trailmedic.domain.model.Session
import com.trailmedic.domain.repository.SessionRepository
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(session: Session) {
        sessionRepository.saveSession(session)
    }
}
