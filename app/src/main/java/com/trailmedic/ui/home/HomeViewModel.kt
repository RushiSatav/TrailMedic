package com.trailmedic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.data.llm.LLMInferenceEngine
import com.trailmedic.domain.model.Session
import com.trailmedic.domain.usecase.GetSessionHistoryUseCase
import com.trailmedic.utils.BatteryAwareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSessionHistoryUseCase: GetSessionHistoryUseCase,
    private val batteryAwareManager: BatteryAwareManager,
    private val llmEngine: LLMInferenceEngine
) : ViewModel() {

    val recentSessions: StateFlow<List<Session>> = getSessionHistoryUseCase.getRecentSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batteryPercent: StateFlow<Int> = batteryAwareManager.batteryPercent
    val isBatteryUnder15: StateFlow<Boolean> = batteryAwareManager.isBatteryUnder15
    val isBatteryUnder5: StateFlow<Boolean> = batteryAwareManager.isBatteryUnder5
    val isLowMemoryDevice: StateFlow<Boolean> = batteryAwareManager.isLowMemoryDevice

    private val _isModelReady = MutableStateFlow(llmEngine.isModelReady || llmEngine.isModelDownloaded())
    val isModelReady = _isModelReady.asStateFlow()

    private val _isReloadingModel = MutableStateFlow(false)
    val isReloadingModel = _isReloadingModel.asStateFlow()

    init {
        checkModelStatus()
    }

    fun checkModelStatus() {
        _isModelReady.value = llmEngine.isModelReady || llmEngine.isModelDownloaded()
    }

    fun reloadModel() {
        viewModelScope.launch {
            _isReloadingModel.value = true
            try {
                llmEngine.initialize()
            } catch (ignored: Throwable) {
            }
            _isModelReady.value = llmEngine.isModelReady || llmEngine.isModelDownloaded()
            _isReloadingModel.value = false
        }
    }
}
