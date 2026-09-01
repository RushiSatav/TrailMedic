package com.trailmedic.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private var currentSpeechRate = 0.85f

    init {
        initTTS()
    }

    private fun initTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                tts?.setSpeechRate(currentSpeechRate)
                _isReady.value = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    @Suppress("DEPRECATION")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 1.5f)
        tts?.setSpeechRate(currentSpeechRate)
    }

    fun speak(text: String) {
        if (_isReady.value && text.isNotBlank()) {
            // Clean markdown syntax, XML tags, and turn headers for clean speech output
            val cleaned = text
                .replace(Regex("<[^>]*>"), "")
                .replace(Regex("\\*\\*|\\*|_"), "")
                .replace("ASSESSMENT:", "Assessment: ")
                .replace("STEPS:", "First Aid Steps: ")
                .replace("WARNING SIGNS:", "Warning Signs: ")
                .replace("NEXT:", "Next Steps: ")
                .trim()

            tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "TRAILMEDIC_TTS_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
        _isSpeaking.value = false
    }
}
