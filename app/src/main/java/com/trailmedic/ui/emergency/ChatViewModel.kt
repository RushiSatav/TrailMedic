package com.trailmedic.ui.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trailmedic.domain.ai.WildernessClinicalAIReasoner
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Message
import com.trailmedic.domain.model.Session
import com.trailmedic.domain.usecase.RunEmergencyInterviewUseCase
import com.trailmedic.domain.usecase.SaveSessionUseCase
import com.trailmedic.utils.SettingsManager
import com.trailmedic.utils.TTSManager
import com.trailmedic.utils.VoiceInputManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val runEmergencyInterviewUseCase: RunEmergencyInterviewUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val aiReasoner: WildernessClinicalAIReasoner,
    private val ttsManager: TTSManager,
    private val voiceInputManager: VoiceInputManager,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private val _phase = MutableStateFlow(ConversationPhase.INTERVIEWING)
    val phase = _phase.asStateFlow()

    private val _isTTSEnabled = MutableStateFlow(true)
    val isTTSEnabled = _isTTSEnabled.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private val _isVoiceListening = voiceInputManager.isListening
    val isVoiceListening = _isVoiceListening

    val voiceErrorMessage = voiceInputManager.errorMessage

    fun clearVoiceError() {
        voiceInputManager.clearError()
    }

    private val _showPhaseTransitionBanner = MutableStateFlow(false)
    val showPhaseTransitionBanner = _showPhaseTransitionBanner.asStateFlow()

    private val _isSessionCompleted = MutableStateFlow(false)
    val isSessionCompleted = _isSessionCompleted.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    private var currentCategory: EmergencyCategory = EmergencyCategory.GENERAL
    private var sessionStartTime = System.currentTimeMillis()
    private var userResponseCount = 0
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            _isTTSEnabled.value = settingsManager.isTTSEnabled.first()
        }
    }

    fun startSession(category: EmergencyCategory) {
        if (_messages.value.isNotEmpty()) return // Already started

        currentCategory = category
        sessionStartTime = System.currentTimeMillis()
        userResponseCount = 0
        _phase.value = ConversationPhase.INTERVIEWING

        startTimer()

        val openingText = "I'm TrailMedic, your offline emergency first aid assistant. I'm here to help right now.\n\n" +
                "Briefly describe what happened with ${category.label.lowercase()}:"

        val openingMessage = Message(
            id = java.util.UUID.randomUUID().toString(),
            content = openingText,
            isUser = false
        )
        _messages.value = listOf(openingMessage)

        if (_isTTSEnabled.value) {
            ttsManager.speak(openingText)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value = (System.currentTimeMillis() - sessionStartTime) / 1000
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isTyping.value) return

        val userMessage = Message(
            id = java.util.UUID.randomUUID().toString(),
            content = userText.trim(),
            isUser = true
        )
        _messages.value = _messages.value + userMessage
        userResponseCount++

        // Transition to DIAGNOSING phase after 4 user responses
        if (userResponseCount >= 4 && _phase.value == ConversationPhase.INTERVIEWING) {
            _phase.value = ConversationPhase.DIAGNOSING
            _showPhaseTransitionBanner.value = true
            viewModelScope.launch {
                delay(2500)
                _showPhaseTransitionBanner.value = false
            }
        }

        _isTyping.value = true
        val streamingMsgId = java.util.UUID.randomUUID().toString()
        val streamingMsg = Message(id = streamingMsgId, content = "", isUser = false)
        _messages.value = _messages.value + streamingMsg

        viewModelScope.launch {
            val accumulatedText = StringBuilder()
            var lastUpdateTime = 0L
            try {
                val finalResponse = runEmergencyInterviewUseCase(
                    category = currentCategory,
                    messages = _messages.value.dropLast(1),
                    phase = _phase.value,
                    questionIndex = userResponseCount - 1,
                    onToken = { token ->
                        accumulatedText.append(token)
                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime > 40 || token.contains("\n")) {
                            lastUpdateTime = now
                            val updatedList = _messages.value.toMutableList()
                            val lastIdx = updatedList.indexOfFirst { it.id == streamingMsgId }
                            if (lastIdx != -1) {
                                updatedList[lastIdx] = streamingMsg.copy(content = accumulatedText.toString())
                                _messages.value = updatedList
                            }
                        }
                    }
                )

                val finalOutput = if (accumulatedText.isNotBlank()) accumulatedText.toString() else finalResponse
                val updatedList = _messages.value.toMutableList()
                val lastIdx = updatedList.indexOfFirst { it.id == streamingMsgId }
                if (lastIdx != -1) {
                    updatedList[lastIdx] = streamingMsg.copy(content = finalOutput)
                    _messages.value = updatedList
                }

                if (_isTTSEnabled.value) {
                    ttsManager.speak(finalOutput)
                }

                if (_phase.value == ConversationPhase.DIAGNOSING) {
                    _isSessionCompleted.value = true
                    autoSaveSession()
                }
            } catch (e: Exception) {
                val errorFallback = "Emergency first aid guidance: Ensure scene safety, immobilize injuries, keep the patient calm and warm, and signal for rescue."
                val updatedList = _messages.value.toMutableList()
                val lastIdx = updatedList.indexOfFirst { it.id == streamingMsgId }
                if (lastIdx != -1) {
                    updatedList[lastIdx] = streamingMsg.copy(content = errorFallback)
                    _messages.value = updatedList
                }
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun startVoiceInput(onResult: (String) -> Unit) {
        voiceInputManager.startListening { result ->
            onResult(result)
        }
    }

    fun stopVoiceInput() {
        voiceInputManager.stopListening()
    }

    fun toggleTTS() {
        _isTTSEnabled.value = !_isTTSEnabled.value
        if (!_isTTSEnabled.value) {
            ttsManager.stop()
        }
        viewModelScope.launch {
            settingsManager.setTTSEnabled(_isTTSEnabled.value)
        }
    }

    fun forceDiagnoseNow() {
        if (_isTyping.value) return
        _phase.value = ConversationPhase.DIAGNOSING
        _showPhaseTransitionBanner.value = true
        viewModelScope.launch {
            delay(2000)
            _showPhaseTransitionBanner.value = false
        }
        sendMessage("Please give me the final first aid assessment and step-by-step instructions immediately.")
    }

    private fun autoSaveSession() {
        viewModelScope.launch {
            val summary = _messages.value.lastOrNull { !it.isUser && it.content.isNotBlank() }?.content ?: ""
            val session = Session(
                emergencyType = currentCategory.label,
                messages = _messages.value,
                firstAidSummary = summary,
                timestamp = sessionStartTime,
                durationSeconds = _elapsedSeconds.value
            )
            _currentSessionId.value = session.id
            saveSessionUseCase(session)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        voiceInputManager.stopListening()
        ttsManager.stop()
    }
}
