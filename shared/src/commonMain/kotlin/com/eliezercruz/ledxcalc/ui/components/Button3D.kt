package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.ui.formatUiText
import com.eliezercruz.ledxcalc.ui.theme.LedColors

@Composable
fun Button3D(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = if (isSelected) MaterialTheme.colorScheme.primary else LedColors.TextMuted
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 14.dp else 4.dp,
                shape = MaterialTheme.shapes.small,
                spotColor = if (isSelected) accent else Color.Black
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accent.copy(alpha = 0.8f) else LedColors.GridLine,
                shape = MaterialTheme.shapes.small
            )
            .background(
                brush = if (isSelected) {
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.85f),
                            accent.copy(alpha = 0.35f),
                            LedColors.PanelElevated
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A2A38), Color(0xFF14141C))
                    )
                },
                shape = MaterialTheme.shapes.small
            )
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = if (isSelected) 0.55f else 0.2f),
                    start = Offset(4f, 2f),
                    end = Offset(size.width - 4f, 2f),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
                if (isSelected) {
                    drawLine(
                        color = accent.copy(alpha = 0.3f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = formatUiText(text),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.Black else LedColors.TextPrimary
        )
    }
}
