package com.trailmedic.ui.emergency

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.trailmedic.domain.model.ConversationPhase
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Session
import com.trailmedic.ui.components.ChatBubble
import com.trailmedic.ui.components.EmergencyBanner
import com.trailmedic.ui.components.TypingIndicator
import com.trailmedic.ui.theme.MediBackground
import com.trailmedic.ui.theme.MediBorder
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
import com.trailmedic.utils.formatAsTimerString
import com.trailmedic.utils.saveSessionReportToDownloads
import com.trailmedic.utils.shareSessionReport

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    categoryStr: String,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToResult: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val category = remember(categoryStr) {
        try {
            EmergencyCategory.valueOf(categoryStr.uppercase())
        } catch (e: Exception) {
            EmergencyCategory.fromId(categoryStr)
        }
    }

    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val phase by viewModel.phase.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val isTTSEnabled by viewModel.isTTSEnabled.collectAsState()
    val isVoiceListening by viewModel.isVoiceListening.collectAsState()
    val voiceErrorMessage by viewModel.voiceErrorMessage.collectAsState()
    val showPhaseBanner by viewModel.showPhaseTransitionBanner.collectAsState()
    val isSessionCompleted by viewModel.isSessionCompleted.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    LaunchedEffect(voiceErrorMessage) {
        voiceErrorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearVoiceError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startSession(category)
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isSessionCompleted) {
        if (isSessionCompleted) {
            showBottomSheet = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MediBackground)
            .imePadding()
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
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
                        Column {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MediTextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (phase == ConversationPhase.INTERVIEWING) "Clarifying Questions" else "First Aid Protocol",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (phase == ConversationPhase.INTERVIEWING) MediPrimaryGreen else MediEmergencyYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Fast Diagnose button highlighted in Yellow
                        if (phase == ConversationPhase.INTERVIEWING && !isTyping) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MediSoftYellow)
                                    .border(1.dp, MediEmergencyYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                TextButton(
                                    onClick = { viewModel.forceDiagnoseNow() },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MediTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Diagnose",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = MediTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Elapsed Timer Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MediSecondarySurface)
                                .border(1.dp, MediBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⏱ ${elapsedSeconds.formatAsTimerString()}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MediTextPrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // TTS Audio Toggle in Green
                        IconButton(
                            onClick = { viewModel.toggleTTS() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isTTSEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Toggle Audio Voice",
                                tint = if (isTTSEnabled) MediPrimaryGreen else MediTextMuted
                            )
                        }
                    }
                }
            }

            // PHASE TRANSITION BANNER
            AnimatedVisibility(
                visible = showPhaseBanner,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                EmergencyBanner(
                    text = "Analyzing responses... Generating structured first aid protocol now.",
                    backgroundColor = MediSoftYellow,
                    contentColor = MediTextPrimary
                )
            }

            // CHAT MESSAGES LIST WITH GENEROUS WHITESPACE
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                items(messages) { message ->
                    if (message.content.isNotBlank()) {
                        ChatBubble(message = message)
                    }
                }

                if (isTyping) {
                    item {
                        TypingIndicator()
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // VOICE RECORDING ACTIVE OVERLAY
            if (isVoiceListening) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MediEmergencyRedSoft,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MediEmergencyRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MediEmergencyRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listening offline... Speak your emergency description",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MediEmergencyRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // BOTTOM INPUT ROW: [ Microphone ] [ Describe what happened... ] [ Send ]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MediSurface,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MediBorder)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Microphone: Rounded icon button
                    IconButton(
                        onClick = {
                            if (audioPermissionState.status.isGranted) {
                                if (isVoiceListening) {
                                    viewModel.stopVoiceInput()
                                } else {
                                    viewModel.startVoiceInput { result ->
                                        inputText = result
                                    }
                                }
                            } else {
                                audioPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceListening) MediEmergencyRedSoft else MediSecondarySurface)
                            .border(1.dp, if (isVoiceListening) MediEmergencyRed else MediBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isVoiceListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isVoiceListening) "Stop Listening" else "Start Voice Input",
                            tint = if (isVoiceListening) MediEmergencyRed else MediTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Input: Large rounded pill
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Describe what happened...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediTextMuted)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MediPrimaryGreen,
                            unfocusedBorderColor = MediBorder,
                            focusedTextColor = MediTextPrimary,
                            unfocusedTextColor = MediTextPrimary,
                            cursorColor = MediPrimaryGreen,
                            focusedContainerColor = MediSecondarySurface,
                            unfocusedContainerColor = MediSecondarySurface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send: Green circular button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isTyping) {
                                val textToSend = inputText
                                inputText = ""
                                viewModel.sendMessage(textToSend)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isTyping,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && !isTyping) MediPrimaryGreen else MediSecondarySurface
                            )
                            .border(
                                1.dp,
                                if (inputText.isNotBlank() && !isTyping) MediPrimaryGreen else MediBorder,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isTyping) Color.White else MediTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // SESSION COMPLETE BOTTOM SHEET
        if (showBottomSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MediSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = MediBorder) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MediLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = MediPrimaryGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "First Aid Protocol Ready",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MediTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Step-by-step guidance generated offline. Follow numbered instructions and monitor warning signs.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MediTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // View Full Protocol Button
                    Button(
                        onClick = {
                            showBottomSheet = false
                            val sId = currentSessionId
                            if (sId != null) {
                                onNavigateToResult(sId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MediPrimaryGreen)
                    ) {
                        Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "View Structured Protocol", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export & Share Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val lastSummary = messages.lastOrNull { !it.isUser }?.content ?: ""
                                val currentSession = Session(
                                    emergencyType = category.label,
                                    messages = messages,
                                    firstAidSummary = lastSummary,
                                    timestamp = System.currentTimeMillis(),
                                    durationSeconds = elapsedSeconds
                                )
                                context.saveSessionReportToDownloads(currentSession)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MediPrimaryGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Save .txt", color = MediTextPrimary, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val lastSummary = messages.lastOrNull { !it.isUser }?.content ?: ""
                                val currentSession = Session(
                                    emergencyType = category.label,
                                    messages = messages,
                                    firstAidSummary = lastSummary,
                                    timestamp = System.currentTimeMillis(),
                                    durationSeconds = elapsedSeconds
                                )
                                context.shareSessionReport(currentSession)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MediTextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Share", color = MediTextPrimary, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showBottomSheet = false
                                viewModel.startSession(category)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MediTextSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "New Session", color = MediTextSecondary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showBottomSheet = false
                                onNavigateHome()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MediSecondarySurface)
                        ) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = MediTextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Home", color = MediTextPrimary, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
