package com.trailmedic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Clean Sans-Serif wilderness & medical tech font family
val MediTrailFontFamily = FontFamily.SansSerif
val TrailMedicFontFamily = MediTrailFontFamily

val Typography = Typography(
    // Display: 28sp Bold
    displayLarge = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        color = MediTextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        color = MediTextPrimary
    ),
    // Headline: 22sp SemiBold
    headlineLarge = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = MediTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp,
        color = MediTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = MediTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = MediTextPrimary
    ),
    // Body: 16sp Regular
    bodyLarge = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = MediTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = MediTextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = MediTextSecondary
    ),
    // Label: 14sp Medium
    labelLarge = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = MediTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = MediTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = MediTrailFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = MediTextSecondary
    )
)

// Emergency instructions: minimum 18sp bold
val EmergencyInstructionTextStyle = TextStyle(
    fontFamily = MediTrailFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.2.sp,
    color = MediTextPrimary
)
