package com.trailmedic.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.domain.model.Session
import com.trailmedic.domain.usecase.GetSessionHistoryUseCase
import com.trailmedic.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getSessionHistoryUseCase: GetSessionHistoryUseCase,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val allSessions: StateFlow<List<Session>> = getSessionHistoryUseCase.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContactName: StateFlow<String> = settingsManager.emergencyContactName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val emergencyContactPhone: StateFlow<String> = settingsManager.emergencyContactPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    suspend fun getSessionById(id: String): Session? {
        return getSessionHistoryUseCase.getSessionById(id)
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            getSessionHistoryUseCase.deleteSession(id)
        }
    }

    fun updateOutcomeNote(sessionId: String, note: String) {
        viewModelScope.launch {
            getSessionHistoryUseCase.updateOutcomeNote(sessionId, note)
        }
    }
}
