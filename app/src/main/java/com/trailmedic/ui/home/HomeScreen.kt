package com.trailmedic.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PestControl
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Session
import com.trailmedic.ui.components.EmergencyBanner
import com.trailmedic.ui.components.OfflineBadge
import com.trailmedic.ui.components.SOSButton
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
import com.trailmedic.utils.formatAsDateTime

@Composable
fun HomeScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSessionDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentSessions by viewModel.recentSessions.collectAsState()
    val batteryPercent by viewModel.batteryPercent.collectAsState()
    val isBatteryUnder15 by viewModel.isBatteryUnder15.collectAsState()
    val isBatteryUnder5 by viewModel.isBatteryUnder5.collectAsState()
    val isLowMemoryDevice by viewModel.isLowMemoryDevice.collectAsState()
    val isModelReady by viewModel.isModelReady.collectAsState()
    val isReloading by viewModel.isReloadingModel.collectAsState()

    val categories = listOf(
        CategoryItem(EmergencyCategory.FRACTURE, "Fracture / Fall", Icons.Default.Healing, MediEmergencyYellow),
        CategoryItem(EmergencyCategory.BREATHING, "Breathing / Altitude", Icons.Default.Air, Color(0xFF0288D1)),
        CategoryItem(EmergencyCategory.BLEEDING, "Bleeding / Wound", Icons.Default.WaterDrop, MediEmergencyRed),
        CategoryItem(EmergencyCategory.HYPOTHERMIA, "Hypothermia / Cold", Icons.Default.AcUnit, Color(0xFF0097A7)),
        CategoryItem(EmergencyCategory.BITE, "Snake / Insect Bite", Icons.Default.PestControl, MediPrimaryGreen),
        CategoryItem(EmergencyCategory.CARDIAC, "Cardiac / Heart", Icons.Default.Favorite, Color(0xFFE91E63)),
        CategoryItem(EmergencyCategory.HEAD, "Head Injury", Icons.Default.Psychology, Color(0xFFF57C00)),
        CategoryItem(EmergencyCategory.GENERAL, "Other Emergency", Icons.Default.MedicalServices, Color(0xFF5C6BC0))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MediBackground),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // TOP APP BAR
        item {
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
                        .border(width = 1.dp, color = MediBorder)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo + Brand Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MediPrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = MediTextPrimary, fontWeight = FontWeight.Black)) {
                                    append("Medi")
                                }
                                withStyle(style = SpanStyle(color = MediPrimaryGreen, fontWeight = FontWeight.Black)) {
                                    append("Trail")
                                }
                            },
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 22.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Status Pill + Settings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OfflineBadge(
                            label = if (isModelReady) "Offline AI Ready" else "Offline Tree Ready",
                            isReady = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MediTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // CRITICAL BATTERY < 5% BANNER
        if (isBatteryUnder5) {
            item {
                EmergencyBanner(
                    text = "⚡ CRITICAL: Battery < 5% ($batteryPercent%) — Save active session now!",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundColor = MediEmergencyRedSoft,
                    contentColor = MediEmergencyRed,
                    icon = Icons.Default.Warning
                )
            }
        } else if (isBatteryUnder15) {
            // BATTERY < 15% BANNER
            item {
                EmergencyBanner(
                    text = "⚡ Low Battery ($batteryPercent%) — LLM tokens reduced to 512 to conserve power.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundColor = MediSoftYellow,
                    contentColor = MediTextPrimary,
                    icon = Icons.Default.Warning
                )
            }
        }

        // LOW RAM WARNING BANNER (RAM < 3GB)
        if (isLowMemoryDevice) {
            item {
                EmergencyBanner(
                    text = "⚠️ Device RAM < 3GB detected. Running in lightweight memory mode with fallback support.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundColor = MediSecondarySurface,
                    contentColor = MediTextPrimary,
                    icon = Icons.Default.Memory
                )
            }
        }

        // MAIN EMERGENCY BUTTON SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SOSButton(
                    onClick = { onNavigateToChat(EmergencyCategory.GENERAL.name) }
                )
            }
        }

        // QUICK SELECT EMERGENCY GRID (Min 56dp touch targets)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Quick Select Emergency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MediTextPrimary,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 2-column responsive layout
                categories.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { category ->
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryCard(
                                    item = category,
                                    onClick = { onNavigateToChat(category.category.name) }
                                )
                            }
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // RECENT SESSIONS SECTION
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Sessions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MediTextPrimary,
                            fontSize = 16.sp
                        )
                    )

                    if (recentSessions.isNotEmpty()) {
                        TextButton(onClick = onNavigateToHistory) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MediPrimaryGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MediPrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MediSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No past emergency sessions yet.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MediTextMuted)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentSessions.take(3).forEach { session ->
                            RecentSessionCard(
                                session = session,
                                onClick = { onNavigateToSessionDetail(session.id) }
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM MODEL STATUS BAR
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MediSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isModelReady) MediPrimaryGreen else MediEmergencyYellow)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isModelReady) "Gemma 2B · On-Device Ready" else "Model Not Loaded · Symptom Engine Active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MediTextPrimary
                            )
                        )
                    }

                    if (!isModelReady) {
                        IconButton(
                            onClick = { viewModel.reloadModel() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isReloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MediEmergencyYellow,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload model",
                                    tint = MediEmergencyYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CategoryItem(
    val category: EmergencyCategory,
    val title: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
private fun CategoryCard(
    item: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Minimum 56dp+ touch target
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MediSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MediBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MediTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentSessionCard(
    session: Session,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // Minimum 56dp+ touch target
            .border(1.dp, MediBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MediSurface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.emergencyType,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MediTextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = session.timestamp.formatAsDateTime(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MediTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MediTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
