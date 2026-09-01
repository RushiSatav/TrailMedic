package com.trailmedic.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trailmedic.ui.theme.CardDark
import com.trailmedic.ui.theme.CardDarkElevated
import com.trailmedic.ui.theme.DeepNavy
import com.trailmedic.ui.theme.EmergencyRed
import com.trailmedic.ui.theme.SafeGreen
import com.trailmedic.ui.theme.TextMuted
import com.trailmedic.ui.theme.TextPrimary
import com.trailmedic.ui.theme.TextSecondary
import com.trailmedic.ui.theme.WarningOrange
import com.trailmedic.utils.ModelDownloadManager
import java.util.Locale

@Composable
fun ModelDownloadScreen(
    onDownloadComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val progress by viewModel.downloadProgress.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Icon Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CardDarkElevated)
                        .border(1.5.dp, EmergencyRed.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (downloadState) {
                            is ModelDownloadManager.DownloadState.Complete -> Icons.Default.CheckCircle
                            else -> Icons.Default.CloudDownload
                        },
                        contentDescription = null,
                        tint = when (downloadState) {
                            is ModelDownloadManager.DownloadState.Complete -> SafeGreen
                            else -> EmergencyRed
                        },
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (downloadState) {
                        is ModelDownloadManager.DownloadState.Complete -> "TrailMedic is Ready!"
                        else -> "Download AI Model"
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (downloadState) {
                        is ModelDownloadManager.DownloadState.Complete ->
                            "Gemma-2B-IT local model installed successfully. 100% offline intelligence is active."
                        else ->
                            "~1.5 GB · Required for on-device AI. Download once over Wi-Fi, use forever in the wilderness."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Storage and Preferences Info Cards
                if (downloadState !is ModelDownloadManager.DownloadState.Downloading &&
                    downloadState !is ModelDownloadManager.DownloadState.Complete
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Storage Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = SafeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Device Storage",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f GB available (1.5 GB required)", viewModel.availableStorageGB),
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // WiFi Only Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Download over Wi-Fi only",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                Switch(
                                    checked = wifiOnly,
                                    onCheckedChange = { viewModel.setWifiOnly(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = EmergencyRed
                                    )
                                )
                            }
                        }
                    }
                }

                // Download In Progress Section
                AnimatedVisibility(
                    visible = downloadState is ModelDownloadManager.DownloadState.Downloading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val state = downloadState as? ModelDownloadManager.DownloadState.Downloading
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(100.dp),
                                    color = EmergencyRed,
                                    strokeWidth = 8.dp,
                                    trackColor = CardDarkElevated,
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmergencyRed,
                                trackColor = CardDarkElevated,
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state?.speedMBps ?: "0.0 MB/s",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SafeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${state?.downloadedMB ?: "0 MB"} / ${state?.totalMB ?: "1.5 GB"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { viewModel.cancelDownload() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Cancel Download", color = TextSecondary)
                            }
                        }
                    }
                }

                // Verifying State
                AnimatedVisibility(visible = downloadState is ModelDownloadManager.DownloadState.Verifying) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = WarningOrange,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Verifying offline neural weights...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                        }
                    }
                }

                // Error State
                AnimatedVisibility(visible = downloadState is ModelDownloadManager.DownloadState.Error) {
                    val errorState = downloadState as? ModelDownloadManager.DownloadState.Error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Download Notice",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = EmergencyRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorState?.message ?: "Unable to download full model.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You can retry or continue with the offline symptom engine (full offline coverage for all 8 major trail emergencies).",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (downloadState) {
                    is ModelDownloadManager.DownloadState.Complete -> {
                        Button(
                            onClick = {
                                viewModel.completeOnboarding()
                                onDownloadComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                        ) {
                            Text(
                                text = "Start Using TrailMedic",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    is ModelDownloadManager.DownloadState.Downloading -> {
                        // Progress shown above
                    }

                    else -> {
                        Button(
                            onClick = { viewModel.startDownload() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download Now (1.5 GB)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = {
                                viewModel.completeOnboarding()
                                onDownloadComplete()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Skip for now (Use Offline Symptom Engine)",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}
