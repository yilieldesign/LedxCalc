package com.eliezercruz.ledxcalc.ui.theme

import androidx.compose.ui.graphics.Color

/** Paleta LED / pantalla digital — usar en toda la app. */
object LedColors {
    val Black = Color(0xFF050508)
    val Panel = Color(0xFF0A0A14)
    val PanelElevated = Color(0xFF12121E)
    val GridLine = Color(0xFF1A1A2E)
    val GridDot = Color(0xFF0066FF)

    val NeonCyan = Color(0xFF00F5FF)
    val NeonGreen = Color(0xFF39FF14)
    val NeonPink = Color(0xFFFF1493)
    val NeonMagenta = Color(0xFFFF00FF)
    val NeonBlue = Color(0xFF0066FF)
    val NeonGold = Color(0xFFFFD700)
    val NeonOrange = Color(0xFFFF9800)
    val NeonPurple = Color(0xFFBB86FC)
    val NeonTeal = Color(0xFF80DEEA)
    val NeonLime = Color(0xFF00FF88)

    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFFB0BEC5)
    val TextMuted = Color(0xFF8B949E)

    val ElectricOk = Color(0xFF00E676)
    val ElectricWarn = Color(0xFFFF1744)
    val ElectricPanelOk = Color(0xFF0D2818)
    val ElectricPanelWarn = Color(0xFF4A0000)

    val BasesAccent = Color(0xFFCE93D8)
    val TrussAccent = Color(0xFF00BFFF)

    /** Colores semánticos para líneas de resultado. */
    object Result {
        val Default = NeonCyan
        val Input = Color(0xFFE0E0E0)
        val Total = NeonLime
        val Resolution = NeonMagenta
        val Breakdown = NeonPurple
        val Coverage = NeonGold
        val Hole = Color(0xFF4CAF50)
        val Signal = Color(0xFF00BFFF)
        val Note = TextSecondary
        val Footer = NeonGold
    }
}

// Alias para compatibilidad con Theme.kt
val NeonCyan = LedColors.NeonCyan
val NeonGreen = LedColors.NeonGreen
val NeonPink = LedColors.NeonPink
val VibrantTeal = Color(0xFF00CED1)
val VibrantCoral = Color(0xFFFF6F61)
val VibrantViolet = Color(0xFF9400D3)
val DarkSurface = LedColors.Black
val DarkSurfaceVariant = LedColors.Panel
val DarkCard = LedColors.PanelElevated
