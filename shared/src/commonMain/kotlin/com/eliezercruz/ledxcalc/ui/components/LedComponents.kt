package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.ui.theme.LedColors

/** Rejilla de píxeles LED de fondo (sutil). */
@Composable
fun LedPixelBackground(modifier: Modifier = Modifier, cellSize: Dp = 28.dp) {
    Canvas(modifier = modifier) {
        val step = cellSize.toPx()
        val cols = (size.width / step).toInt() + 1
        val rows = (size.height / step).toInt() + 1
        for (c in 0..cols) {
            val x = c * step
            drawLine(
                color = LedColors.GridLine.copy(alpha = 0.45f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.6f
            )
        }
        for (r in 0..rows) {
            val y = r * step
            drawLine(
                color = LedColors.GridLine.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.6f
            )
        }
        for (c in 0 until cols step 3) {
            for (r in 0 until rows step 3) {
                drawCircle(
                    color = LedColors.GridDot.copy(alpha = 0.08f),
                    radius = 1.2f,
                    center = Offset(c * step + step / 2f, r * step + step / 2f)
                )
            }
        }
    }
}

/** Panel con borde neón y fondo oscuro — bloque estándar de la UI. */
@Composable
fun LedPanel(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    glowElevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(glowElevation, spotColor = accentColor.copy(alpha = 0.35f), shape = MaterialTheme.shapes.medium)
            .border(1.dp, accentColor.copy(alpha = 0.45f), MaterialTheme.shapes.medium)
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        LedColors.PanelElevated.copy(alpha = 0.96f),
                        LedColors.Panel.copy(alpha = 0.94f)
                    )
                ),
                shape = MaterialTheme.shapes.medium
            )
            .drawBehind {
                drawLine(
                    color = accentColor.copy(alpha = 0.25f),
                    start = Offset(8f, 1f),
                    end = Offset(size.width - 8f, 1f),
                    strokeWidth = 2f
                )
            }
            .padding(14.dp),
        content = content
    )
}

@Composable
fun LedTitleBar(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = accentColor,
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, spotColor = accentColor.copy(alpha = 0.5f))
            .background(LedColors.Panel.copy(alpha = 0.95f), MaterialTheme.shapes.medium)
            .border(1.dp, accentColor.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LedSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LedColors.NeonCyan
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}
