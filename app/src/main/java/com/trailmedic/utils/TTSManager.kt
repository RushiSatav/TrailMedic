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

    private var currentSpeechRate = 0.95f

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

    /**
     * Speaks concise, essential emergency first aid instructions without reading lengthy paragraphs.
     */
    fun speak(text: String) {
        if (_isReady.value && text.isNotBlank()) {
            val conciseText = formatForConciseSpeech(text)
            if (conciseText.isNotBlank()) {
                tts?.speak(conciseText, TextToSpeech.QUEUE_FLUSH, null, "TRAILMEDIC_TTS_${System.currentTimeMillis()}")
            }
        }
    }

    /**
     * Extracts only the top 2 actionable points and the triage question for brief, clear spoken voice.
     */
    private fun formatForConciseSpeech(text: String): String {
        // Clean markdown, XML tags, and emojis
        val cleaned = text
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("[*#_`~]"), "")
            .replace(Regex("[⚠️⏱⚡●▶🗑↑]"), "")
            .trim()

        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotBlank() }
        val speechParts = mutableListOf<String>()

        var actionCount = 0
        var foundQuestion = false

        for (line in lines) {
            val lower = line.lowercase()

            // Skip large section headers
            if (lower.startsWith("immediate first aid actions") ||
                lower.startsWith("action protocol") ||
                lower.startsWith("emergency assessment") ||
                lower.startsWith("triage assessment") ||
                lower.startsWith("critical warning signs") ||
                lower.startsWith("evacuation")
            ) {
                continue
            }

            // Capture top 2 numbered action points or bullet points
            if ((line.matches(Regex("^\\d+\\..*")) || line.startsWith("•") || line.startsWith("-")) && actionCount < 2) {
                val cleanLine = line.replace(Regex("^\\d+\\.\\s*|^[•\\-]\\s*"), "")
                speechParts.add(cleanLine)
                actionCount++
                continue
            }

            // Capture triage question
            if (line.contains("?") && !foundQuestion) {
                speechParts.add(line)
                foundQuestion = true
            }
        }

        // If structured parsing didn't find points (e.g. short greeting or direct answer), take first 2 sentences max
        if (speechParts.isEmpty()) {
            val sentences = cleaned.split(Regex("(?<=[.!?])\\s+")).take(2)
            return sentences.joinToString(" ").take(180)
        }

        // Limit concise speech to essential points (under ~200 characters)
        return speechParts.joinToString(". ").take(220)
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
