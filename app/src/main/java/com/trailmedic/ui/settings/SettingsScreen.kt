package com.trailmedic.ui.settings

import androidx.compose.foundation.background
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trailmedic.ui.theme.CardDark
import com.trailmedic.ui.theme.CardDarkElevated
import com.trailmedic.ui.theme.DeepNavy
import com.trailmedic.ui.theme.EmergencyRed
import com.trailmedic.ui.theme.SafeGreen
import com.trailmedic.ui.theme.SurfaceDark
import com.trailmedic.ui.theme.TextMuted
import com.trailmedic.ui.theme.TextPrimary
import com.trailmedic.ui.theme.TextSecondary
import com.trailmedic.ui.theme.WarningOrange

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToModelDownload: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val emergencyContactName by viewModel.emergencyContactName.collectAsState()
    val emergencyContactPhone by viewModel.emergencyContactPhone.collectAsState()
    val isTTSEnabled by viewModel.isTTSEnabled.collectAsState()
    val ttsSpeechRate by viewModel.ttsSpeechRate.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val useLLM by viewModel.useLLM.collectAsState()
    val testResponse by viewModel.testResponse.collectAsState()
    val isTestingModel by viewModel.isTestingModel.collectAsState()

    var contactNameInput by remember(emergencyContactName) { mutableStateOf(emergencyContactName) }
    var contactPhoneInput by remember(emergencyContactPhone) { mutableStateOf(emergencyContactPhone) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isImportingModel by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val modelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isImportingModel = true
            Toast.makeText(context, "Importing model file... Please wait.", Toast.LENGTH_SHORT).show()
            viewModel.importModelFromUri(context, uri) { success, msg ->
                isImportingModel = false
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = SurfaceDark,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: AI MODEL
                SectionHeader(title = "AI Model Management", icon = Icons.Default.Psychology)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Model: ${viewModel.modelDisplayName}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = if (viewModel.isModelReady) "Storage used: ~${viewModel.modelSizeMB} MB" else "Not downloaded",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (viewModel.isModelReady) SafeGreen.copy(alpha = 0.2f) else EmergencyRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (viewModel.isModelReady) "READY" else "NOT READY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (viewModel.isModelReady) SafeGreen else EmergencyRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Gemma 2B Neural LLM",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = if (useLLM) "Active: Neural model generates free-form answers" else "Active: Clinical Knowledge Tree (Fast & 100% Reliable)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (useLLM) WarningOrange else SafeGreen,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Switch(
                                checked = useLLM,
                                onCheckedChange = { viewModel.setUseLLM(it) },
                                enabled = viewModel.isModelReady,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SafeGreen,
                                    checkedTrackColor = SafeGreen.copy(alpha = 0.5f),
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = CardDarkElevated
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.testModel() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CardDarkElevated),
                                enabled = !isTestingModel
                            ) {
                                if (isTestingModel) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Test Model", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (viewModel.isModelReady) {
                                        showDeleteConfirmDialog = true
                                    } else {
                                        onNavigateToModelDownload()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isModelReady) Icons.Default.Delete else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (viewModel.isModelReady) EmergencyRed else SafeGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (viewModel.isModelReady) "Delete" else "Download",
                                    color = if (viewModel.isModelReady) EmergencyRed else SafeGreen,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                modelPickerLauncher.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isImportingModel
                        ) {
                            if (isImportingModel) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SafeGreen, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Importing Model File...", fontSize = 12.sp, color = SafeGreen)
                            } else {
                                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Import Model from Phone (.task / .bin)", fontSize = 12.sp, color = SafeGreen)
                            }
                        }

                        if (testResponse != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardDarkElevated)
                            ) {
                                Text(
                                    text = testResponse ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 2: VOICE & AUDIO
                SectionHeader(title = "Voice & Audio Guidance", icon = Icons.Default.RecordVoiceOver)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Speak first aid steps aloud (TTS)",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Switch(
                                checked = isTTSEnabled,
                                onCheckedChange = { viewModel.setTTSEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SafeGreen
                                )
                            )
                        }

                        if (isTTSEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Speech Rate: ${String.format("%.2f", ttsSpeechRate)}x",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Slider(
                                value = ttsSpeechRate,
                                onValueChange = { viewModel.setTTSSpeechRate(it) },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = SafeGreen,
                                    activeTrackColor = SafeGreen,
                                    inactiveTrackColor = CardDarkElevated
                                )
                            )

                            Button(
                                onClick = { viewModel.testTTS() },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CardDarkElevated)
                            ) {
                                Text(text = "Test Voice Speech", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 3: DISPLAY & BATTERY
                SectionHeader(title = "Display & Screen", icon = Icons.Default.Smartphone)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Keep screen awake during emergency",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Switch(
                                checked = keepScreenOn,
                                onCheckedChange = { viewModel.setKeepScreenOn(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SafeGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Text Size Scale",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Normal", "Large", "Extra Large").forEach { sizeOption ->
                                FilterChip(
                                    selected = textSize == sizeOption,
                                    onClick = { viewModel.setTextSize(sizeOption) },
                                    label = { Text(text = sizeOption, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmergencyRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 4: EMERGENCY CONTACT
                SectionHeader(title = "Emergency Contact", icon = Icons.Default.Sos)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Configured contact is displayed on result screens for 1-tap dialing when satellite/cell signal is available.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = contactNameInput,
                            onValueChange = {
                                contactNameInput = it
                                viewModel.setEmergencyContact(it, contactPhoneInput)
                            },
                            label = { Text("Contact Name / Group Leader", color = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafeGreen,
                                unfocusedBorderColor = CardDarkElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CardDarkElevated,
                                unfocusedContainerColor = CardDarkElevated
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = contactPhoneInput,
                            onValueChange = {
                                contactPhoneInput = it
                                viewModel.setEmergencyContact(contactNameInput, it)
                            },
                            label = { Text("Phone Number / Satphone Number", color = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafeGreen,
                                unfocusedBorderColor = CardDarkElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CardDarkElevated,
                                unfocusedContainerColor = CardDarkElevated
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 5: DOWNLOAD PREFERENCES
                SectionHeader(title = "Download Preferences", icon = Icons.Default.Wifi)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Download model via Wi-Fi only",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                        Switch(
                            checked = wifiOnly,
                            onCheckedChange = { viewModel.setWifiOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SafeGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 6: ABOUT & DISCLAIMER
                SectionHeader(title = "About TrailMedic", icon = Icons.Default.Info)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TrailMedic v1.0 (Production Release)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Engineered specifically for backcountry trekkers, mountaineers, and remote wilderness guides.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningOrange.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "MEDICAL DISCLAIMER: This app provides emergency first aid guidance only for remote situations without internet. It is not a substitute for certified professional medical care or hospital triage. Always seek emergency medical services as soon as possible.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WarningOrange,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Confirmation dialog for deleting model
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(text = "Delete Offline AI Model?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Deleting the Gemma 2B model will free ~1.5 GB of device storage. The app will continue to operate with the full offline symptom decision engine.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteModel()
                            showDeleteConfirmDialog = false
                        }
                    ) {
                        Text(text = "Delete", color = EmergencyRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(text = "Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SafeGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp
            )
        )
    }
}
