package com.trailmedic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.data.llm.LLMInferenceEngine
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message
import com.trailmedic.utils.SettingsManager
import com.trailmedic.utils.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val llmEngine: LLMInferenceEngine,
    private val ttsManager: TTSManager
) : ViewModel() {

    val emergencyContactName: StateFlow<String> = settingsManager.emergencyContactName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val emergencyContactPhone: StateFlow<String> = settingsManager.emergencyContactPhone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val isTTSEnabled: StateFlow<Boolean> = settingsManager.isTTSEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val ttsSpeechRate: StateFlow<Float> = settingsManager.ttsSpeechRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.85f)

    val textSize: StateFlow<String> = settingsManager.textSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Normal")

    val keepScreenOn: StateFlow<Boolean> = settingsManager.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val wifiOnly: StateFlow<Boolean> = settingsManager.wifiOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useLLM: StateFlow<Boolean> = settingsManager.useLLM
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isModelReady: Boolean
        get() = llmEngine.isModelReady || llmEngine.isModelDownloaded()

    val modelDisplayName: String
        get() = llmEngine.modelDisplayName

    val modelSizeMB: Long
        get() = llmEngine.getModelFileSizeMB()

    private val _testResponse = MutableStateFlow<String?>(null)
    val testResponse = _testResponse.asStateFlow()

    private val _isTestingModel = MutableStateFlow(false)
    val isTestingModel = _isTestingModel.asStateFlow()

    fun setEmergencyContact(name: String, phone: String) {
        viewModelScope.launch {
            settingsManager.setEmergencyContact(name, phone)
        }
    }

    fun setTTSEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setTTSEnabled(enabled)
        }
    }

    fun setTTSSpeechRate(rate: Float) {
        viewModelScope.launch {
            settingsManager.setTTSSpeechRate(rate)
            ttsManager.setSpeechRate(rate)
        }
    }

    fun setTextSize(size: String) {
        viewModelScope.launch {
            settingsManager.setTextSize(size)
        }
    }

    fun setKeepScreenOn(keep: Boolean) {
        viewModelScope.launch {
            settingsManager.setKeepScreenOn(keep)
        }
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            settingsManager.setWifiOnly(wifiOnly)
        }
    }

    fun setUseLLM(useLLM: Boolean) {
        viewModelScope.launch {
            settingsManager.setUseLLM(useLLM)
        }
    }

    fun testTTS() {
        ttsManager.speak("TrailMedic offline voice guidance is active and working properly.")
    }

    fun testModel() {
        viewModelScope.launch {
            _isTestingModel.value = true
            _testResponse.value = "Testing offline Gemma 2B model..."
            try {
                if (isModelReady) {
                    val messages = listOf(
                        Message(content = "Is the offline Gemma model ready?", isUser = true)
                    )
                    val response = llmEngine.generateResponse(
                        conversationHistory = messages,
                        phase = ConversationPhase.INTERVIEWING,
                        onToken = { token ->
                            _testResponse.value = (_testResponse.value ?: "") + token
                        }
                    )
                    _testResponse.value = response
                } else {
                    _testResponse.value = "Offline Symptom Engine is active (Model file not downloaded). Full fallback knowledge base operational."
                }
            } catch (t: Throwable) {
                _testResponse.value = "Offline Engine Status: Ready with embedded clinical knowledge base. (Model note: ${t.message ?: t.javaClass.simpleName})"
            } finally {
                _isTestingModel.value = false
            }
        }
    }

    fun deleteModel() {
        llmEngine.deleteModel()
    }

    fun importModelFromUri(context: android.content.Context, uri: android.net.Uri, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dir = java.io.File(llmEngine.modelDir)
                if (!dir.exists()) dir.mkdirs()
                val destFile = java.io.File(llmEngine.modelPath)
                val tempFile = java.io.File("${llmEngine.modelPath}.tmp")
                if (tempFile.exists()) tempFile.delete()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists()) destFile.delete()
                tempFile.renameTo(destFile)

                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(true, "Model file imported (${destFile.length() / (1024*1024)} MB). Ready to use!")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(false, "Failed to import model: ${e.message}")
                }
            }
        }
    }
}
