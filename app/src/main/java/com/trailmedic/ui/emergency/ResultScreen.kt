package com.trailmedic.ui.emergency

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trailmedic.domain.model.Session
import com.trailmedic.ui.history.HistoryViewModel
import com.trailmedic.ui.theme.EmergencyInstructionTextStyle
import com.trailmedic.ui.theme.MediBackground
import com.trailmedic.ui.theme.MediBorder
import com.trailmedic.ui.theme.MediDarkGreen
import com.trailmedic.ui.theme.MediEmergencyRed
import com.trailmedic.ui.theme.MediEmergencyRedSoft
import com.trailmedic.ui.theme.MediEmergencyYellow
import com.trailmedic.ui.theme.MediLightGreen
import com.trailmedic.ui.theme.MediPrimaryGreen
import com.trailmedic.ui.theme.MediSecondarySurface
import com.trailmedic.ui.theme.MediSoftYellow
import com.trailmedic.ui.theme.MediSurface
import com.trailmedic.ui.theme.MediTextMuted
import com.trailmedic.ui.theme.MediTextPrimary
import com.trailmedic.ui.theme.MediTextSecondary
import com.trailmedic.utils.formatAsDateTime
import com.trailmedic.utils.formatAsDurationSummary
import com.trailmedic.utils.saveSessionReportToDownloads
import com.trailmedic.utils.shareSessionReport

@Composable
fun ResultScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var session by remember { mutableStateOf<Session?>(null) }
    val scrollState = rememberScrollState()
    val checkedSteps = remember { mutableStateMapOf<Int, Boolean>() }

    val emergencyContactPhone by viewModel.emergencyContactPhone.collectAsState()
    val emergencyContactName by viewModel.emergencyContactName.collectAsState()

    LaunchedEffect(sessionId) {
        session = viewModel.getSessionById(sessionId)
    }

    val currentSession = session

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MediBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MediSurface,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MediBorder)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MediTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "First Aid Protocol",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MediTextPrimary
                            )
                        )
                    }

                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MediTextSecondary
                        )
                    }
                }
            }

            if (currentSession == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading protocol...", color = MediTextSecondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1: Emergency Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MediSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MediLightGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalHospital,
                                            contentDescription = null,
                                            tint = MediPrimaryGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = currentSession.emergencyType,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MediTextPrimary,
                                            fontSize = 17.sp
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(MediLightGreen)
                                        .border(1.dp, MediPrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                    Text(
                                        text = "TRIAGE READY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MediDarkGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentSession.timestamp.formatAsDateTime(),
                                    style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary)
                                )
                                Text(
                                    text = "Duration: ${currentSession.durationSeconds.formatAsDurationSummary()}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: Recommended Actions (Action Protocol Checklist)
                    val steps = parseSteps(currentSession.firstAidSummary)
                    Text(
                        text = "Recommended Actions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MediTextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MediSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (steps.isNotEmpty()) {
                                steps.forEachIndexed { index, step ->
                                    val isChecked = checkedSteps[index] ?: false
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checkedSteps[index] = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MediPrimaryGreen,
                                                uncheckedColor = MediBorder,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = step,
                                            style = EmergencyInstructionTextStyle.copy(
                                                fontSize = 17.sp, // Minimum 17-18sp emergency instruction
                                                lineHeight = 24.sp,
                                                color = if (isChecked) MediTextMuted else MediTextPrimary,
                                                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                            ),
                                            modifier = Modifier.padding(top = 10.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = currentSession.firstAidSummary,
                                    style = EmergencyInstructionTextStyle.copy(
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp,
                                        color = MediTextPrimary
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 3: Important Warnings (Red-Flag Warning Signs)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MediEmergencyRedSoft),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MediEmergencyRed.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MediEmergencyRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Important Warnings & Red Flags",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MediEmergencyRed
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• Rapid drop in consciousness, confusion, or speech slurring\n" +
                                        "• Loss of distal pulse, extremity turning pale/blue/cold\n" +
                                        "• Uncontrolled arterial bleeding or suspected cervical spine trauma\n" +
                                        "• Persistent projectile vomiting or worsening systemic shock",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MediTextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 4: Evacuation & Satellite SOS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MediSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sos,
                                    contentDescription = null,
                                    tint = MediPrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Evacuation & Satellite Signaling",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MediTextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "1. Activate Garmin inReach, Apple Emergency SOS, or 406MHz PLB.\n" +
                                        "2. Audio signal: 3 sharp whistle blasts repeated every minute.\n" +
                                        "3. Visual signal: Mirror flash in sets of 3 towards rescue aircraft.\n" +
                                        "4. Keep patient insulated from the cold ground with sleeping pads.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MediTextSecondary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 5: Emergency Contact Action
                    if (emergencyContactPhone.isNotBlank()) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$emergencyContactPhone"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MediEmergencyYellow)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = MediTextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call: $emergencyContactName ($emergencyContactPhone)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MediTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Export & Share Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                context.saveSessionReportToDownloads(currentSession)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MediPrimaryGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Save .txt", color = MediTextPrimary, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                context.shareSessionReport(currentSession)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MediTextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Share", color = MediTextPrimary, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

private fun parseSteps(summary: String): List<String> {
    val lines = summary.lines()
    val steps = mutableListOf<String>()
    var inStepsSection = false

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("STEPS:", ignoreCase = true) ||
            trimmed.startsWith("ACTION PROTOCOL:", ignoreCase = true) ||
            trimmed.startsWith("IMMEDIATE FIRST AID ACTIONS:", ignoreCase = true) ||
            trimmed.startsWith("ACTIONS:", ignoreCase = true) ||
            trimmed.contains("FIRST AID PROTOCOL", ignoreCase = true)
        ) {
            inStepsSection = true
            continue
        }
        if (inStepsSection && (
            trimmed.startsWith("CRITICAL WARNING", ignoreCase = true) ||
            trimmed.startsWith("WARNING", ignoreCase = true) ||
            trimmed.startsWith("EVACUATION", ignoreCase = true) ||
            trimmed.startsWith("TRIAGE", ignoreCase = true) ||
            trimmed.startsWith("NEXT", ignoreCase = true)
        )) {
            break
        }
        if (inStepsSection && (
            trimmed.matches(Regex("^\\d+\\..*")) ||
            trimmed.startsWith("•") ||
            trimmed.startsWith("-") ||
            trimmed.startsWith("*")
        )) {
            steps.add(trimmed)
        }
    }

    if (steps.isEmpty()) {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.matches(Regex("^\\d+\\..*")) ||
                trimmed.startsWith("•") ||
                trimmed.startsWith("-")
            ) {
                steps.add(trimmed)
            }
        }
    }

    return steps
}
