package com.trailmedic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.data.llm.LLMInferenceEngine
import com.trailmedic.domain.ai.WildernessClinicalAIReasoner
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.SymptomEmergencyData
import com.trailmedic.domain.repository.SymptomTreeRepository
import com.trailmedic.utils.SettingsManager
import com.trailmedic.utils.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val ttsManager: TTSManager,
    private val symptomTreeRepository: SymptomTreeRepository,
    private val aiReasoner: WildernessClinicalAIReasoner
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

    val modelFileName: String
        get() = llmEngine.modelFileName

    val modelFormat: String
        get() = llmEngine.modelFormat

    val modelSizeMB: Long
        get() = llmEngine.getModelFileSizeMB()

    val datasetConditionsCount: Int
        get() = symptomTreeRepository.getAllEmergencies().size + symptomTreeRepository.getFirstAidIntents().size

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

    fun testModel(customPrompt: String? = null) {
        viewModelScope.launch {
            _isTestingModel.value = true
            val query = customPrompt?.trim()?.ifBlank { null } ?: "What should I do if someone gets a cut or scrape?"
            _testResponse.value = "🔍 Extracting dataset protocols for: '$query'..."

            try {
                val extract = symptomTreeRepository.extractKnowledge(query)
                val messages = listOf(Message(content = query, isUser = true))

                if (isModelReady && useLLM.value) {
                    val clinicalData = if (extract != null) {
                        SymptomEmergencyData(
                            id = extract.conditionTag.lowercase().replace(" ", "_"),
                            name = extract.conditionName,
                            triggerKeywords = listOf(extract.conditionTag),
                            questions = listOf(extract.triageQuestion),
                            firstAidSteps = extract.firstAidSteps,
                            warningSigns = extract.warningSigns,
                            evacuationNote = extract.evacuationNote
                        )
                    } else {
                        symptomTreeRepository.getCategoryFallback(EmergencyCategory.GENERAL)
                    }

                    _testResponse.value = "✨ Matched Condition: ${extract?.conditionName ?: "General"}\n[Local AI Generating...]\n"
                    val response = llmEngine.generateResponse(
                        conversationHistory = messages,
                        phase = ConversationPhase.INTERVIEWING,
                        clinicalData = clinicalData,
                        onToken = { token ->
                            _testResponse.value = (_testResponse.value ?: "") + token
                        }
                    )
                    _testResponse.value = "✨ Matched Condition: ${extract?.conditionName ?: "General"}\n\n$response"
                } else {
                    val response = aiReasoner.generateDynamicResponse(
                        category = EmergencyCategory.GENERAL,
                        messages = messages,
                        phase = ConversationPhase.INTERVIEWING,
                        questionIndex = 0
                    )
                    _testResponse.value = "✨ Matched Condition: ${extract?.conditionName ?: "Clinical Protocol"}\nConfidence: ${String.format("%.0f", (extract?.confidence ?: 0.9f) * 100)}%\n\n$response"
                }
            } catch (t: Throwable) {
                _testResponse.value = "Offline Knowledge Engine Status: Active ($datasetConditionsCount conditions loaded).\n(Note: ${t.message ?: t.javaClass.simpleName})"
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

                var originalName = "model.gguf"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        originalName = cursor.getString(nameIdx)
                    }
                }

                val destFile = java.io.File(dir, originalName)
                val tempFile = java.io.File(dir, "$originalName.tmp")
                if (tempFile.exists()) tempFile.delete()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Delete older models to free up device storage
                dir.listFiles()?.forEach { file ->
                    if (file.name != originalName && !file.name.endsWith(".tmp")) {
                        file.delete()
                    }
                }

                if (destFile.exists()) destFile.delete()
                tempFile.renameTo(destFile)

                // Re-initialize engine with the newly imported model
                llmEngine.release()
                llmEngine.initialize()

                val sizeMb = destFile.length() / (1024 * 1024)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(true, "Model '$originalName' ($sizeMb MB) imported and loaded successfully!")
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onComplete(false, "Failed to import model: ${e.message}")
                }
            }
        }
    }
}

