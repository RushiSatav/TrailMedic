package com.trailmedic.utils

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Model URL — Gemma 2B IT INT4 MediaPipe bundle
    val MODEL_URL = "https://storage.googleapis.com/mediapipe-models/llm_inference/gemma-2b-it-gpu-int4/float16/1/gemma-2b-it-gpu-int4.bin"
    val MODEL_DIR: String get() = "${context.filesDir}/models"
    val MODEL_FILE: String get() = "$MODEL_DIR/gemma-2b-it.bin"

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState = _downloadState.asStateFlow()

    private var activeCall: Call? = null

    sealed class DownloadState {
        data object Idle : DownloadState()
        data class Downloading(
            val progress: Float,
            val speedMBps: String,
            val downloadedMB: String,
            val totalMB: String
        ) : DownloadState()
        data object Verifying : DownloadState()
        data object Complete : DownloadState()
        data class Error(val message: String) : DownloadState()
    }

    fun isModelDownloaded(): Boolean = File(MODEL_FILE).exists() && File(MODEL_FILE).length() > 0

    fun getAvailableStorageGB(): Float {
        return try {
            val stat = StatFs(context.filesDir.path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes / (1024f * 1024f * 1024f)
        } catch (e: Exception) {
            0f
        }
    }

    suspend fun downloadModel() = withContext(Dispatchers.IO) {
        try {
            val dir = File(MODEL_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            _downloadProgress.value = 0f
            _downloadState.value = DownloadState.Downloading(0f, "0.0 MB/s", "0 MB", "1.5 GB")

            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // Infinite read timeout for large stream
                .build()

            val request = Request.Builder()
                .url(MODEL_URL)
                .header("User-Agent", "TrailMedic-Android-App")
                .build()
            val call = client.newCall(request)
            activeCall = call

            val response = call.execute()
            if (!response.isSuccessful) {
                throw Exception("Server returned HTTP ${response.code}: ${response.message}")
            }

            val body = response.body ?: throw Exception("Empty response body from server")
            val totalBytes = body.contentLength().let { if (it > 0) it else 1500L * 1024 * 1024 }

            val tempFile = File("$MODEL_FILE.tmp")
            if (tempFile.exists()) tempFile.delete()

            val totalMBFormatted = String.format(Locale.US, "%.1f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))

            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime

            tempFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(32768)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= 250) { // update UI 4 times per second
                            lastUpdateTime = currentTime
                            val progress = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
                            val elapsedSec = (currentTime - startTime) / 1000.0
                            val speed = if (elapsedSec > 0) (downloadedBytes / (1024.0 * 1024.0 * elapsedSec)) else 0.0
                            val downloadedMBFormatted = String.format(Locale.US, "%.0f MB", downloadedBytes / (1024.0 * 1024.0))
                            val speedFormatted = String.format(Locale.US, "%.1f MB/s", speed)

                            _downloadProgress.value = progress
                            _downloadState.value = DownloadState.Downloading(
                                progress = progress,
                                speedMBps = speedFormatted,
                                downloadedMB = downloadedMBFormatted,
                                totalMB = totalMBFormatted
                            )
                        }
                    }
                }
            }

            _downloadState.value = DownloadState.Verifying
            val finalFile = File(MODEL_FILE)
            if (finalFile.exists()) finalFile.delete()
            tempFile.renameTo(finalFile)

            _downloadProgress.value = 1f
            _downloadState.value = DownloadState.Complete

        } catch (e: Exception) {
            val isCanceled = activeCall?.isCanceled() == true
            if (isCanceled) {
                _downloadState.value = DownloadState.Idle
            } else {
                _downloadState.value = DownloadState.Error(e.message ?: "Download encountered an error")
            }
        } finally {
            activeCall = null
        }
    }

    fun cancelDownload() {
        activeCall?.cancel()
        val tempFile = File("$MODEL_FILE.tmp")
        if (tempFile.exists()) tempFile.delete()
        _downloadState.value = DownloadState.Idle
        _downloadProgress.value = 0f
    }
}
