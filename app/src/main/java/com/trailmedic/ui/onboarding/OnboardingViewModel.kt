package com.trailmedic.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.utils.ModelDownloadManager
import com.trailmedic.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val downloadManager: ModelDownloadManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val downloadState = downloadManager.downloadState
    val downloadProgress = downloadManager.downloadProgress

    val isFirstLaunch: StateFlow<Boolean> = settingsManager.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val wifiOnly: StateFlow<Boolean> = settingsManager.wifiOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val availableStorageGB: Float
        get() = downloadManager.getAvailableStorageGB()

    val isModelDownloaded: Boolean
        get() = downloadManager.isModelDownloaded()

    fun startDownload() {
        viewModelScope.launch {
            downloadManager.downloadModel()
        }
    }

    fun cancelDownload() {
        downloadManager.cancelDownload()
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setWifiOnly(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsManager.setFirstLaunchCompleted()
        }
    }
}
