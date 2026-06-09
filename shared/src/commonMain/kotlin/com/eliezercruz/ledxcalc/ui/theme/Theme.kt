package com.eliezercruz.ledxcalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LedDarkColorScheme = darkColorScheme(
    primary = LedColors.NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003344),
    onPrimaryContainer = LedColors.NeonCyan,
    secondary = LedColors.NeonGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0A2E0A),
    onSecondaryContainer = LedColors.NeonGreen,
    tertiary = LedColors.NeonPink,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF3D0A2A),
    onTertiaryContainer = LedColors.NeonPink,
    background = LedColors.Black,
    onBackground = LedColors.TextPrimary,
    surface = LedColors.Panel,
    onSurface = LedColors.TextPrimary,
    surfaceVariant = LedColors.PanelElevated,
    onSurfaceVariant = LedColors.TextSecondary,
    outline = Color(0xFF0066FF).copy(alpha = 0.35f),
    outlineVariant = LedColors.GridLine,
    error = LedColors.ElectricWarn,
    onError = Color.White
)

/** Esquinas más rectas — estilo panel LED modular. */
private val LedShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(10.dp)
)

@Composable
fun LedxCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // La app siempre usa tema LED oscuro (pantalla digital)
    @Suppress("UNUSED_VARIABLE")
    val useDark = forceDarkTheme ?: darkTheme
    MaterialTheme(
        colorScheme = LedDarkColorScheme,
        typography = Typography,
        shapes = LedShapes,
        content = content
    )
}
