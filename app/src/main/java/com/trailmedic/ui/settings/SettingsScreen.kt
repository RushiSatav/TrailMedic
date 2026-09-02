package com.trailmedic.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trailmedic.ui.theme.MediBackground
import com.trailmedic.ui.theme.MediBorder
import com.trailmedic.ui.theme.MediDarkGreen
import com.trailmedic.ui.theme.MediEmergencyRed
import com.trailmedic.ui.theme.MediEmergencyYellow
import com.trailmedic.ui.theme.MediLightGreen
import com.trailmedic.ui.theme.MediPrimaryGreen
import com.trailmedic.ui.theme.MediSecondarySurface
import com.trailmedic.ui.theme.MediSoftYellow
import com.trailmedic.ui.theme.MediSurface
import com.trailmedic.ui.theme.MediTextMuted
import com.trailmedic.ui.theme.MediTextPrimary
import com.trailmedic.ui.theme.MediTextSecondary

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
            viewModel.importModelFromUri(context, uri) { _, msg ->
                isImportingModel = false
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val scrollState = rememberScrollState()

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MediTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MediTextPrimary
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

                // SECTION 1: AI & KNOWLEDGE
                SectionHeader(title = "AI & Knowledge", icon = Icons.Default.Psychology)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Model: ${viewModel.modelDisplayName}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MediTextPrimary
                                    )
                                )
                                Text(
                                    text = "Format: ${viewModel.modelFormat} • ${if (viewModel.isModelReady) "~${viewModel.modelSizeMB} MB" else "Not Loaded"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary)
                                )
                                if (viewModel.modelFileName.isNotBlank() && viewModel.modelFileName != "No model imported") {
                                    Text(
                                        text = "File: ${viewModel.modelFileName}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MediTextMuted, fontSize = 11.sp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (viewModel.isModelReady) MediLightGreen else MediSoftYellow)
                                    .border(1.dp, if (viewModel.isModelReady) MediPrimaryGreen.copy(alpha = 0.3f) else MediEmergencyYellow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (viewModel.isModelReady) "MODEL ACTIVE" else "CLINICAL RAG READY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (viewModel.isModelReady) MediDarkGreen else MediTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Knowledge Status Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MediLightGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediPrimaryGreen.copy(alpha = 0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MediPrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Embedded Knowledge: ${viewModel.datasetConditionsCount} Medical Protocols Active (Dual Datasets)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MediDarkGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Local AI Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Local Neural LLM Inference",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MediTextPrimary
                                    )
                                )
                                Text(
                                    text = if (useLLM) "Active: Neural model generates free-form answers" else "Active: Clinical Knowledge Tree (Fast & 100% Reliable)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (useLLM) MediEmergencyYellow else MediPrimaryGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Switch(
                                checked = useLLM,
                                onCheckedChange = { viewModel.setUseLLM(it) },
                                enabled = viewModel.isModelReady,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MediPrimaryGreen,
                                    uncheckedThumbColor = MediTextMuted,
                                    uncheckedTrackColor = MediSecondarySurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Import Button: ↑ Import Model (.GGUF / .BIN / .TASK)
                        OutlinedButton(
                            onClick = {
                                modelPickerLauncher.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediPrimaryGreen),
                            enabled = !isImportingModel
                        ) {
                            if (isImportingModel) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MediPrimaryGreen, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Importing Model File...", fontSize = 13.sp, color = MediPrimaryGreen)
                            } else {
                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = MediPrimaryGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "↑ Import Model", fontSize = 13.sp, color = MediPrimaryGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "(.GGUF / .BIN / .TASK)", fontSize = 11.sp, color = MediTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Diagnostic Prompt Test Section
                        Text(
                            text = "Test Prompt Extraction & Reasoning",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MediTextPrimary,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        var promptInput by remember { mutableStateOf("How do you treat Cuts?") }
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = { Text("Ask a first aid question (e.g. CPR steps, snake bite, cuts)...", fontSize = 12.sp, color = MediTextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediPrimaryGreen,
                                unfocusedBorderColor = MediBorder,
                                focusedTextColor = MediTextPrimary,
                                unfocusedTextColor = MediTextPrimary,
                                focusedContainerColor = MediSecondarySurface,
                                unfocusedContainerColor = MediSecondarySurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick prompt chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cuts", "Snake Bite", "CPR", "Sprains").forEach { tag ->
                                FilterChip(
                                    selected = promptInput.contains(tag, ignoreCase = true),
                                    onClick = { promptInput = "What to do for $tag?" },
                                    label = { Text(tag, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MediLightGreen,
                                        selectedLabelColor = MediDarkGreen,
                                        containerColor = MediSecondarySurface,
                                        labelColor = MediTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (promptInput.contains(tag, ignoreCase = true)) MediPrimaryGreen else MediBorder,
                                        enabled = true,
                                        selected = promptInput.contains(tag, ignoreCase = true)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.testModel(promptInput) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MediPrimaryGreen),
                                enabled = !isTestingModel
                            ) {
                                if (isTestingModel) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "▶ Extract & Test", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (viewModel.isModelReady) {
                                OutlinedButton(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MediEmergencyRed.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MediEmergencyRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "🗑 Delete", color = MediEmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (testResponse != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MediSecondarySurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder)
                            ) {
                                Text(
                                    text = testResponse ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MediTextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 2: VOICE & AUDIO GUIDANCE
                SectionHeader(title = "Voice & Audio Guidance", icon = Icons.Default.RecordVoiceOver)
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
                            Text(
                                text = "Speak first aid steps aloud (TTS)",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediTextPrimary, fontWeight = FontWeight.SemiBold)
                            )
                            Switch(
                                checked = isTTSEnabled,
                                onCheckedChange = { viewModel.setTTSEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MediPrimaryGreen,
                                    uncheckedThumbColor = MediTextMuted,
                                    uncheckedTrackColor = MediSecondarySurface
                                )
                            )
                        }

                        if (isTTSEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Speech Rate: ${String.format("%.2f", ttsSpeechRate)}x",
                                style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary)
                            )
                            Slider(
                                value = ttsSpeechRate,
                                onValueChange = { viewModel.setTTSSpeechRate(it) },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MediPrimaryGreen,
                                    activeTrackColor = MediPrimaryGreen,
                                    inactiveTrackColor = MediSecondarySurface
                                )
                            )

                            Button(
                                onClick = { viewModel.testTTS() },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MediSecondarySurface)
                            ) {
                                Text(text = "Test Voice Speech", fontSize = 11.sp, color = MediTextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 3: DISPLAY
                SectionHeader(title = "Display", icon = Icons.Default.Smartphone)
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
                            Text(
                                text = "Keep screen awake during emergency",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediTextPrimary, fontWeight = FontWeight.SemiBold)
                            )
                            Switch(
                                checked = keepScreenOn,
                                onCheckedChange = { viewModel.setKeepScreenOn(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MediPrimaryGreen,
                                    uncheckedThumbColor = MediTextMuted,
                                    uncheckedTrackColor = MediSecondarySurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Text Size Scale",
                            style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val sizeOptions = listOf(
                                "Normal" to "A-",
                                "Large" to "A",
                                "Extra Large" to "A+"
                            )
                            sizeOptions.forEach { (sizeKey, sizeLabel) ->
                                val isSelected = textSize == sizeKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setTextSize(sizeKey) },
                                    label = { Text(text = "$sizeLabel ($sizeKey)", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MediLightGreen,
                                        selectedLabelColor = MediDarkGreen,
                                        containerColor = MediSecondarySurface,
                                        labelColor = MediTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) MediPrimaryGreen else MediBorder,
                                        enabled = true,
                                        selected = isSelected
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
                    colors = CardDefaults.cardColors(containerColor = MediSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Configured contact is displayed on result screens for 1-tap dialing when satellite/cell signal is available.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary, lineHeight = 16.sp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = contactNameInput,
                            onValueChange = {
                                contactNameInput = it
                                viewModel.setEmergencyContact(it, contactPhoneInput)
                            },
                            label = { Text("Contact Name / Group Leader", color = MediTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediPrimaryGreen,
                                unfocusedBorderColor = MediBorder,
                                focusedTextColor = MediTextPrimary,
                                unfocusedTextColor = MediTextPrimary,
                                focusedContainerColor = MediSecondarySurface,
                                unfocusedContainerColor = MediSecondarySurface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = contactPhoneInput,
                            onValueChange = {
                                contactPhoneInput = it
                                viewModel.setEmergencyContact(contactNameInput, it)
                            },
                            label = { Text("Phone Number / Satphone Number", color = MediTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediPrimaryGreen,
                                unfocusedBorderColor = MediBorder,
                                focusedTextColor = MediTextPrimary,
                                unfocusedTextColor = MediTextPrimary,
                                focusedContainerColor = MediSecondarySurface,
                                unfocusedContainerColor = MediSecondarySurface
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
                    colors = CardDefaults.cardColors(containerColor = MediSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            style = MaterialTheme.typography.bodyMedium.copy(color = MediTextPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Switch(
                            checked = wifiOnly,
                            onCheckedChange = { viewModel.setWifiOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MediPrimaryGreen,
                                uncheckedThumbColor = MediTextMuted,
                                uncheckedTrackColor = MediSecondarySurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SECTION 6: ABOUT & DISCLAIMER
                SectionHeader(title = "About MediTrail", icon = Icons.Default.Info)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MediSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MediTrail v1.0 (Production Release)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MediTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Engineered specifically for backcountry trekkers, mountaineers, and remote wilderness guides.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MediTextSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MediSoftYellow)
                                .border(1.dp, MediEmergencyYellow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "MEDICAL DISCLAIMER: This app provides emergency first aid guidance only for remote situations without internet. It is not a substitute for certified professional medical care or hospital triage. Always seek emergency medical services as soon as possible.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MediTextPrimary,
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
                title = { Text(text = "Delete Offline AI Model?", color = MediTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Deleting the Gemma 2B model will free ~1.5 GB of device storage. The app will continue to operate with the full offline symptom decision engine.",
                        color = MediTextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteModel()
                            showDeleteConfirmDialog = false
                        }
                    ) {
                        Text(text = "Delete", color = MediEmergencyRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(text = "Cancel", color = MediTextSecondary)
                    }
                },
                containerColor = MediSurface
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
            tint = MediPrimaryGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MediTextPrimary,
                fontSize = 14.sp
            )
        )
    }
}
