package com.novavpn.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Brand Colors ────────────────────────────────────────────────────────────

val NovaBlack      = Color(0xFF080C14)
val NovaDarkBlue   = Color(0xFF0D1526)
val NovaMidBlue    = Color(0xFF0F2040)
val NovaAccent     = Color(0xFF00E5FF)   // Cyan glow
val NovaAccentDim  = Color(0xFF0097A7)
val NovaGreen      = Color(0xFF00E676)   // Connected state
val NovaRed        = Color(0xFFFF1744)   // Error state
val NovaOrange     = Color(0xFFFF6D00)   // Connecting state
val NovaWhite      = Color(0xFFECF0F1)
val NovaGray       = Color(0xFF607080)
val NovaSurface    = Color(0xFF111C2E)
val NovaCard       = Color(0xFF162035)

// ─── Color Scheme ────────────────────────────────────────────────────────────

private val NovaDarkColorScheme = darkColorScheme(
    primary         = NovaAccent,
    onPrimary       = NovaBlack,
    primaryContainer = NovaMidBlue,
    secondary       = NovaGreen,
    background      = NovaBlack,
    surface         = NovaSurface,
    surfaceVariant  = NovaCard,
    onBackground    = NovaWhite,
    onSurface       = NovaWhite,
    error           = NovaRed,
    outline         = NovaGray,
)

// ─── Typography ───────────────────────────────────────────────────────────────

// Using default system font family with custom weights
// In production, replace with a custom font like "Rajdhani" or "Orbitron"
// by adding TTF files to res/font/ and referencing via FontFamily(Font(...))
val NovaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.W800,
        fontSize   = 57.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.W700,
        fontSize   = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize   = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize   = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize   = 16.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize   = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.W400,
        fontSize   = 14.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize   = 14.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize   = 11.sp,
        letterSpacing = 1.5.sp
    )
)

// ─── Theme Composable ─────────────────────────────────────────────────────────

@Composable
fun NovaVpnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography  = NovaTypography,
        content     = content
    )
}
