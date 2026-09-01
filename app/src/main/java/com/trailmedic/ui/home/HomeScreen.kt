package com.trailmedic.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MedicalServices
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trailmedic.domain.model.EmergencyCategory
import com.trailmedic.domain.model.Session
import com.trailmedic.ui.components.EmergencyBanner
import com.trailmedic.ui.components.OfflineBadge
import com.trailmedic.ui.components.SOSButton
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
        CategoryItem(EmergencyCategory.FRACTURE, "Fracture / Fall", Icons.Default.Healing, WarningOrange),
        CategoryItem(EmergencyCategory.BREATHING, "Breathing / Altitude", Icons.Default.Air, Color(0xFF4CC9F0)),
        CategoryItem(EmergencyCategory.BLEEDING, "Bleeding / Wound", Icons.Default.WaterDrop, EmergencyRed),
        CategoryItem(EmergencyCategory.HYPOTHERMIA, "Hypothermia / Cold", Icons.Default.AcUnit, Color(0xFF72EFDD)),
        CategoryItem(EmergencyCategory.BITE, "Snake / Insect Bite", Icons.Default.PestControl, Color(0xFF90BE6D)),
        CategoryItem(EmergencyCategory.CARDIAC, "Cardiac / Heart", Icons.Default.Favorite, Color(0xFFF72585)),
        CategoryItem(EmergencyCategory.HEAD, "Head Injury", Icons.Default.Psychology, Color(0xFFFFB703)),
        CategoryItem(EmergencyCategory.GENERAL, "Other Emergency", Icons.Default.MedicalServices, Color(0xFF48CAE4))
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // TOP APP BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmergencyRed),
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
                        text = "TrailMedic",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OfflineBadge(
                        label = if (isModelReady) "Offline AI Ready" else "Offline Tree Ready",
                        isReady = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // CRITICAL BATTERY < 5% BANNER
        if (isBatteryUnder5) {
            item {
                EmergencyBanner(
                    text = "⚡ CRITICAL: Battery < 5% ($batteryPercent%) — Save your active session now!",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    backgroundColor = EmergencyRed,
                    contentColor = Color.White,
                    icon = Icons.Default.Warning
                )
            }
        } else if (isBatteryUnder15) {
            // BATTERY < 15% BANNER
            item {
                EmergencyBanner(
                    text = "⚡ Low Battery ($batteryPercent%) — LLM tokens reduced to 512 to conserve power.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    backgroundColor = WarningOrange,
                    contentColor = DeepNavy,
                    icon = Icons.Default.Warning
                )
            }
        }

        // LOW RAM WARNING BANNER (RAM < 3GB)
        if (isLowMemoryDevice) {
            item {
                EmergencyBanner(
                    text = "⚠️ Device RAM < 3GB detected. Running in lightweight memory mode with fallback support.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    backgroundColor = CardDarkElevated,
                    contentColor = TextPrimary,
                    icon = Icons.Default.Memory
                )
            }
        }

        // SOS BUTTON SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SOSButton(
                    onClick = { onNavigateToChat(EmergencyCategory.GENERAL.name) }
                )
            }
        }

        // QUICK SELECT EMERGENCY SECTION (Min 56dp touch targets)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Quick Select Emergency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Dynamically chunked 2-column layout for 100% responsiveness on any screen size
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
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )

                    if (recentSessions.isNotEmpty()) {
                        TextButton(onClick = onNavigateToHistory) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = EmergencyRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = EmergencyRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentSessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.5f)),
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
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
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
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isModelReady) SafeGreen else EmergencyRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isModelReady) "Gemma 2B · On-Device Ready" else "Model Not Loaded · Symptom Engine Active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
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
                                    color = WarningOrange,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload model",
                                    tint = WarningOrange,
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
            .height(58.dp) // Minimum 56dp+ touch target
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(item.iconTint.copy(alpha = 0.15f)),
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
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 15.sp
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
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
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
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = session.timestamp.formatAsDateTime(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
