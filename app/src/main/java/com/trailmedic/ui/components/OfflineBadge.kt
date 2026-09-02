package com.trailmedic.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trailmedic.ui.theme.MediDarkGreen
import com.trailmedic.ui.theme.MediEmergencyRed
import com.trailmedic.ui.theme.MediEmergencyRedSoft
import com.trailmedic.ui.theme.MediLightGreen
import com.trailmedic.ui.theme.MediPrimaryGreen

@Composable
fun OfflineBadge(
    modifier: Modifier = Modifier,
    label: String = "Offline AI Ready",
    isReady: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    val containerBg = if (isReady) MediLightGreen else MediEmergencyRedSoft
    val dotColor = if (isReady) MediPrimaryGreen else MediEmergencyRed
    val textColor = if (isReady) MediDarkGreen else MediEmergencyRed
    val borderColor = if (isReady) MediPrimaryGreen.copy(alpha = 0.25f) else MediEmergencyRed.copy(alpha = 0.25f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.25f))
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor,
                letterSpacing = 0.2.sp
            )
        )
    }
}
