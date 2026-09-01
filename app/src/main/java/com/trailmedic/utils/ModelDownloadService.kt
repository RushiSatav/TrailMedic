package com.trailmedic.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.trailmedic.MainActivity
import com.trailmedic.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ModelDownloadService : Service() {

    @Inject
    lateinit var downloadManager: ModelDownloadManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "trailmedic_model_download"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            downloadManager.cancelDownload()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification("Preparing model download...", 0, true))

        serviceScope.launch {
            launch {
                downloadManager.downloadState.collectLatest { state ->
                    when (state) {
                        is ModelDownloadManager.DownloadState.Downloading -> {
                            val percent = (state.progress * 100).toInt()
                            val content = "Downloading: $percent% (${state.speedMBps}) - ${state.downloadedMB} / ${state.totalMB}"
                            updateNotification(content, percent, false)
                        }
                        is ModelDownloadManager.DownloadState.Verifying -> {
                            updateNotification("Verifying offline model...", 100, true)
                        }
                        is ModelDownloadManager.DownloadState.Complete -> {
                            updateNotification("Model downloaded successfully! Offline AI ready.", 100, false)
                            stopForeground(STOP_FOREGROUND_DETACH)
                            stopSelf()
                        }
                        is ModelDownloadManager.DownloadState.Error -> {
                            updateNotification("Download failed: ${state.message}", 0, false)
                            stopForeground(STOP_FOREGROUND_DETACH)
                            stopSelf()
                        }
                        is ModelDownloadManager.DownloadState.Idle -> {
                            // No-op
                        }
                    }
                }
            }

            downloadManager.downloadModel()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress for downloading the Gemma 2B offline model"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String, progress: Int, indeterminate: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, ModelDownloadService::class.java).apply {
            action = ACTION_CANCEL
        }
        val pendingCancel = PendingIntent.getService(
            this, 1, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TrailMedic Offline AI")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpenApp)
            .setProgress(100, progress, indeterminate)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", pendingCancel)
            .build()
    }

    private fun updateNotification(contentText: String, progress: Int, indeterminate: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(contentText, progress, indeterminate))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        const val ACTION_CANCEL = "com.trailmedic.action.CANCEL_DOWNLOAD"
    }
}
