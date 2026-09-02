package com.trailmedic.data.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.Message
import com.trailmedic.utils.BatteryAwareManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryAwareManager: BatteryAwareManager
) {
    companion object {
        init {
            try {
                System.loadLibrary("OpenCL")
            } catch (ignored: Throwable) {
            }
        }
    }

    private var llmInference: LlmInference? = null
    val modelDir: String get() = "${context.filesDir}/models"

    val modelFile: File?
        get() {
            val dir = File(modelDir)
            if (!dir.exists()) return null
            return dir.listFiles()?.filter { it.isFile && it.length() > 0 && !it.name.endsWith(".tmp") }
                ?.maxByOrNull { it.lastModified() }
        }

    val modelPath: String get() = modelFile?.absolutePath ?: "$modelDir/gemma-2b-it.bin"

    val modelDisplayName: String get() = modelFile?.nameWithoutExtension ?: "Gemma-2B-IT"

    val modelFileName: String get() = modelFile?.name ?: "No model imported"

    val modelFormat: String
        get() = when {
            modelFile?.name?.endsWith(".gguf", ignoreCase = true) == true -> "GGUF (Quantized)"
            modelFile?.name?.endsWith(".task", ignoreCase = true) == true -> "MediaPipe Task"
            modelFile?.name?.endsWith(".bin", ignoreCase = true) == true -> "Binary (GPU/CPU)"
            modelFile != null -> "Custom Model"
            else -> "Not Installed"
        }

    val isModelReady: Boolean
        get() = modelFile?.exists() == true && llmInference != null

    fun isModelDownloaded(): Boolean = modelFile != null && modelFile!!.length() > 0

    fun getModelFileSizeMB(): Long {
        val file = modelFile
        return if (file != null && file.exists()) file.length() / (1024 * 1024) else 0L
    }

    /**
     * Checks whether the current device/emulator architecture supports MediaPipe GenAI native binaries.
     * MediaPipe LLM inference requires arm64-v8a or armeabi-v7a.
     */
    fun isArchitectureSupported(): Boolean {
        val supportedAbis = android.os.Build.SUPPORTED_ABIS
        return supportedAbis.any { it.contains("arm64") || it.contains("arm") }
    }

    fun initialize(): Result<Unit> = try {
        if (!isModelDownloaded()) {
            Result.failure(IllegalStateException("Model file not found at $modelPath"))
        } else if (!isArchitectureSupported() && android.os.Build.SUPPORTED_ABIS.none { it.contains("arm") }) {
            Result.failure(UnsupportedOperationException("MediaPipe LLM requires ARM architecture (arm64-v8a). Current ABI is ${android.os.Build.SUPPORTED_ABIS.joinToString()}"))
        } else {
            release()
            val maxTokens = batteryAwareManager.getRecommendedMaxTokens()
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(maxTokens)
                .build()
            llmInference = LlmInference.createFromOptions(context, options)
            Result.success(Unit)
        }
    } catch (t: Throwable) {
        release()
        Result.failure(t)
    }

    suspend fun generateResponse(
        conversationHistory: List<Message>,
        phase: ConversationPhase,
        clinicalData: com.trailmedic.domain.model.SymptomEmergencyData? = null,
        onToken: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val prompt = ConversationManager.buildPrompt(conversationHistory, phase, clinicalData)
        val resultBuilder = StringBuilder()

        try {
            if (llmInference == null) {
                val initRes = initialize()
                if (initRes.isFailure) {
                    throw initRes.exceptionOrNull() ?: IllegalStateException("Failed to initialize model")
                }
            }

            val inference = llmInference ?: throw IllegalStateException("Model instance is null")
            val fullResponse = inference.generateResponse(prompt)
            var cleaned = fullResponse
                .replace("<end_of_turn>", "")
                .replace("<start_of_turn>", "")
                .trim()

            // Strip robotic preambles
            val preambleRegex = Regex("^(Sure[!,.]? (here is|here's|I understand).*?:|Here is the answer:|Here is your answer:|Answer:|Response:)", RegexOption.IGNORE_CASE)
            cleaned = cleaned.replace(preambleRegex, "").trim()

            // Strip disclaimers if generated
            val disclaimerRegex = Regex("^(I am (an AI|not a doctor).*?\\. |I cannot provide (medical|professional) (attention|advice).*?\\. |Please note that I am an AI.*?\\. )", RegexOption.IGNORE_CASE)
            cleaned = cleaned.replace(disclaimerRegex, "").trim()

            // Strip any accidental persona labels the model echoes at the start
            val prefixes = listOf("Medic:", "TrailMedic:", "Assistant:", "Model:", "AI:", "Doctor:", "Responder:")
            for (p in prefixes) {
                if (cleaned.startsWith(p, ignoreCase = true)) {
                    cleaned = cleaned.substring(p.length).trim()
                }
            }

            val words = cleaned.split(" ")
            for (i in words.indices) {
                val piece = if (i == 0) words[i] else " " + words[i]
                resultBuilder.append(piece)
                onToken(piece)
                kotlinx.coroutines.delay(10)
            }
            resultBuilder.toString()
        } catch (t: Throwable) {
            release()
            throw t
        }
    }

    fun deleteModel(): Boolean {
        release()
        val file = File(modelPath)
        return if (file.exists()) file.delete() else true
    }

    fun release() {
        try {
            llmInference?.close()
        } catch (ignored: Throwable) {
        } finally {
            llmInference = null
        }
    }
}
