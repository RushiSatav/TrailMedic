package com.trailmedic.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.data.llm.LLMInferenceEngine
import com.trailmedic.ui.navigation.Screen
import com.trailmedic.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val llmEngine: LLMInferenceEngine
) : ViewModel() {

    private val _targetDestination = MutableStateFlow<String?>(null)
    val targetDestination = _targetDestination.asStateFlow()

    init {
        determineDestination()
    }

    private fun determineDestination() {
        viewModelScope.launch {
            // Brief visual branded splash delay
            delay(1200)

            val isFirstLaunch = settingsManager.isFirstLaunch.first()
            val isModelDownloaded = llmEngine.isModelDownloaded()

            val route = when {
                isModelDownloaded && !isFirstLaunch -> Screen.Home.route
                isFirstLaunch -> Screen.Onboarding.route
                else -> Screen.ModelDownload.route
            }

            _targetDestination.value = route
        }
    }
}
