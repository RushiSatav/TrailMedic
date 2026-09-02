package com.trailmedic.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val MediTrailColorScheme = lightColorScheme(
    primary = MediPrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = MediLightGreen,
    onPrimaryContainer = MediDarkGreen,
    secondary = MediEmergencyYellow,
    onSecondary = MediTextPrimary,
    secondaryContainer = MediSoftYellow,
    onSecondaryContainer = MediTextPrimary,
    tertiary = MediDarkGreen,
    onTertiary = Color.White,
    background = MediBackground,
    onBackground = MediTextPrimary,
    surface = MediSurface,
    onSurface = MediTextPrimary,
    surfaceVariant = MediSecondarySurface,
    onSurfaceVariant = MediTextSecondary,
    error = MediEmergencyRed,
    onError = Color.White,
    errorContainer = MediEmergencyRedSoft,
    onErrorContainer = MediEmergencyRedDark,
    outline = MediBorder,
    outlineVariant = MediBorder
)

// Corner radius: 12dp cards, 24dp large buttons, 50% for pills/badges
val TrailMedicShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(50)
)

val MediTrailShapes = TrailMedicShapes

@Composable
fun TrailMedicTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = MediTrailColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MediSurface.toArgb()
            window.navigationBarColor = MediBackground.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = TrailMedicShapes,
        content = content
    )
}

@Composable
fun MediTrailTheme(
    content: @Composable () -> Unit
) {
    TrailMedicTheme(content = content)
}
