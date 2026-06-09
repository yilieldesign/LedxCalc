package com.eliezercruz.ledxcalc.ui.sketches

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.ModuleSignalLayout
import com.eliezercruz.ledxcalc.ui.drawing.drawCanvasText
import kotlin.math.min

private val groupColors = listOf(
    Color(0xFF0066FF), Color(0xFF00FF66), Color(0xFFFF6600), Color(0xFFFF00FF),
    Color(0xFFFFFF00), Color(0xFF00FFFF), Color(0xFFFF0066), Color(0xFF9900FF)
)

@Composable
fun ModuleSketch(
    columns: Int,
    rows: Int,
    groupSize: Int,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, spotColor = Color(0xFF0066FF).copy(alpha = 0.3f)),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isLarge) 260.dp else 180.dp)
                .background(Color.Black)
                .padding(6.dp)
        ) {
            val layout = ModuleSignalLayout.compute(columns, rows, groupSize) ?: return@Canvas

            val gridPadding = 6.dp.toPx()
            val cellWidth = (size.width - gridPadding * 2) / columns
            val cellHeight = (size.height - gridPadding * 2) / rows
            val startX = gridPadding
            val startY = gridPadding
            val ledMargin = 1.5.dp.toPx()
            val trackingStroke = 2.5.dp.toPx()

            fun cellCenter(col: Int, row: Int): Offset = Offset(
                startX + col * cellWidth + cellWidth / 2f,
                startY + row * cellHeight + cellHeight / 2f
            )

            for (c in 0 until columns) {
                for (r in 0 until rows) {
                    val left = startX + c * cellWidth
                    val top = startY + r * cellHeight
                    drawRect(Color(0xFF0A0A0A), Offset(left, top), Size(cellWidth, cellHeight))
                    val groupIdx = layout.moduleGroupIndex[Pair(c, r)] ?: 0
                    drawRect(
                        groupColors[groupIdx % groupColors.size],
                        Offset(left + ledMargin, top + ledMargin),
                        Size(cellWidth - ledMargin * 2, cellHeight - ledMargin * 2)
                    )
                }
            }

            // Líneas de seguimiento: recorrido serpentina por cada línea de señal
            layout.signalPaths.forEachIndexed { groupIdx, path ->
                val trackColor = Color.White.copy(alpha = 0.92f)
                for (i in 0 until path.size - 1) {
                    drawLine(
                        color = trackColor,
                        start = cellCenter(path[i].first, path[i].second),
                        end = cellCenter(path[i + 1].first, path[i + 1].second),
                        strokeWidth = trackingStroke,
                        cap = StrokeCap.Round
                    )
                }
                path.forEachIndexed { orderIdx, (col, row) ->
                    val center = cellCenter(col, row)
                    val badgeR = min(cellWidth, cellHeight) * 0.18f
                    drawCircle(Color.Black.copy(alpha = 0.75f), badgeR, center)
                    drawCanvasText(
                        text = (orderIdx + 1).toString(),
                        x = center.x,
                        y = center.y + badgeR * 0.35f,
                        textSizePx = badgeR * 1.1f,
                        color = Color.White,
                        bold = true,
                        centerAlign = true
                    )
                }
                val first = path.firstOrNull() ?: return@forEachIndexed
                val firstCenter = cellCenter(first.first, first.second)
                drawCircle(
                    color = groupColors[groupIdx % groupColors.size],
                    radius = min(cellWidth, cellHeight) * 0.28f,
                    center = firstCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCanvasText(
                    text = "L${groupIdx + 1}",
                    x = firstCenter.x,
                    y = firstCenter.y - min(cellWidth, cellHeight) * 0.22f,
                    textSizePx = min(cellWidth, cellHeight) * 0.22f,
                    color = groupColors[groupIdx % groupColors.size],
                    bold = true,
                    centerAlign = true
                )
            }

            for (c in 0..columns) {
                val x = startX + c * cellWidth
                drawLine(Color(0xFF1A1A1A), Offset(x, startY), Offset(x, startY + rows * cellHeight), 2.dp.toPx())
            }
            for (r in 0..rows) {
                val y = startY + r * cellHeight
                drawLine(Color(0xFF1A1A1A), Offset(startX, y), Offset(startX + columns * cellWidth, y), 2.dp.toPx())
            }
        }
    }
}
