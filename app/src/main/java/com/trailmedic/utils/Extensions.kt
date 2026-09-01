package com.trailmedic.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.trailmedic.domain.model.Session
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatAsDateTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy · hh:mm a", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.formatAsFileDate(): String {
    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.formatAsTimerString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun Long.formatAsDurationSummary(): String {
    val minutes = this / 60
    val seconds = this % 60
    return if (minutes > 0) {
        "$minutes min ${if (seconds > 0) "$seconds sec" else ""}"
    } else {
        "$seconds sec"
    }
}

fun Session.exportAsFormattedText(): String {
    val dateStr = timestamp.formatAsDateTime()
    val durationStr = durationSeconds.formatAsDurationSummary()

    val transcript = StringBuilder()
    messages.forEach { msg ->
        val sender = if (msg.isUser) "TREKKER / HELPER" else "TRAILMEDIC AI"
        val time = msg.timestamp.formatAsDateTime()
        transcript.append("[$time] $sender:\n${msg.content}\n\n")
    }

    return """
========================================
       TRAILMEDIC SESSION REPORT
========================================
Date: $dateStr
Emergency Type: $emergencyType
Duration: $durationStr
Outcome Note: ${outcomeNote.ifBlank { "None recorded" }}

----------------------------------------
CONVERSATION TRANSCRIPT:
----------------------------------------
$transcript
----------------------------------------
FIRST AID GIVEN / PROTOCOL:
----------------------------------------
$firstAidSummary

========================================
Generated offline by TrailMedic App
========================================
    """.trimIndent()
}

fun Context.saveSessionReportToDownloads(session: Session): Boolean {
    val filename = "TrailMedic_${session.emergencyType.replace(" ", "_")}_${session.timestamp.formatAsFileDate()}.txt"
    val content = session.exportAsFormattedText()

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TrailMedic")
            }
            val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                Toast.makeText(this, "Saved to Downloads/TrailMedic/$filename", Toast.LENGTH_LONG).show()
                true
            } else {
                false
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val trailMedicDir = File(downloadsDir, "TrailMedic")
            if (!trailMedicDir.exists()) trailMedicDir.mkdirs()
            val file = File(trailMedicDir, filename)
            FileOutputStream(file).use { out ->
                out.write(content.toByteArray())
            }
            Toast.makeText(this, "Saved to Downloads/TrailMedic/$filename", Toast.LENGTH_LONG).show()
            true
        }
    } catch (e: Exception) {
        Toast.makeText(this, "Failed to export report: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        false
    }
}

fun Context.shareSessionReport(session: Session) {
    try {
        val filename = "TrailMedic_${session.emergencyType.replace(" ", "_")}_${session.timestamp.formatAsFileDate()}.txt"
        val cacheFile = File(cacheDir, filename)
        FileOutputStream(cacheFile).use { out ->
            out.write(session.exportAsFormattedText().toByteArray())
        }

        val contentUri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            cacheFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "TrailMedic Emergency Report - ${session.emergencyType}")
            putExtra(Intent.EXTRA_TEXT, session.firstAidSummary)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share TrailMedic Report via"))
    } catch (e: Exception) {
        Toast.makeText(this, "Unable to share report: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
