package com.eliezercruz.ledxcalc.ui.sketches

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.BasesLayout
import com.eliezercruz.ledxcalc.domain.SupportCalculation
import com.eliezercruz.ledxcalc.ui.formatUiText
import com.eliezercruz.ledxcalc.ui.theme.LedColors
import com.eliezercruz.ledxcalc.ui.drawing.drawCanvasText
import com.eliezercruz.ledxcalc.util.formatDouble
import kotlin.math.min

@Composable
fun SketchSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, spotColor = LedColors.NeonBlue.copy(alpha = 0.35f)),
        colors = CardDefaults.elevatedCardColors(containerColor = LedColors.Panel)
    ) {
        Text(
            text = formatUiText(title),
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
        content()
    }
}

@Composable
fun HoleSketch(
    screenWidthFeet: Double,
    screenHeightFeet: Double,
    holeWidthFeet: Double,
    holeHeightFeet: Double,
    holeWidthLabel: String,
    holeHeightLabel: String,
    modulesAcross: Int,
    modulesHigh: Int,
    modifier: Modifier = Modifier
) {
    SketchCanvas(modifier, height = 200.dp) {
        if (screenWidthFeet <= 0 || screenHeightFeet <= 0) return@SketchCanvas
        val pad = 20.dp.toPx()
        val labelH = 40.dp.toPx()
        val footerH = 22.dp.toPx()
        val availW = size.width - pad * 2
        val availH = size.height - pad * 2 - labelH - footerH
        val scale = min(availW / screenWidthFeet.toFloat(), availH / screenHeightFeet.toFloat())
        val sw = screenWidthFeet.toFloat() * scale
        val sh = screenHeightFeet.toFloat() * scale
        val ox = (size.width - sw) / 2f
        val oy = pad + labelH

        val hw = holeWidthFeet.toFloat() * scale
        val hh = holeHeightFeet.toFloat() * scale
        val hx = ox + (sw - hw) / 2f
        val hy = oy + (sh - hh) / 2f

        // Marco exterior (estructura alrededor del hueco)
        drawRect(Color(0xFF37474F), Offset(ox, oy), Size(sw, sh))
        drawRect(Color(0xFF00E676), Offset(ox, oy), Size(sw, sh), style = Stroke(2.dp.toPx()))

        // Paneles visibles detrás del hueco
        drawHolePanelGrid(
            left = hx,
            top = hy,
            width = hw,
            height = hh,
            columns = modulesAcross,
            rows = modulesHigh
        )

        drawRect(
            Color(0xFFFFD54F),
            Offset(hx, hy),
            Size(hw, hh),
            style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
        )

        drawCanvasText(
            text = "Hueco de pantalla",
            x = size.width / 2f,
            y = pad,
            textSizePx = 12.dp.toPx(),
            color = Color.White,
            bold = true,
            centerAlign = true
        )
        drawCanvasText(
            text = "con paneles",
            x = size.width / 2f,
            y = pad + 14.dp.toPx(),
            textSizePx = 10.dp.toPx(),
            color = Color(0xFF80CBC4),
            centerAlign = true
        )
        drawCanvasText(
            text = "Pantalla ${formatDouble(screenWidthFeet, 1)}×${formatDouble(screenHeightFeet, 1)} ft",
            x = size.width / 2f,
            y = oy - 6.dp.toPx(),
            textSizePx = 9.dp.toPx(),
            color = Color(0xFFB0BEC5),
            centerAlign = true
        )
        drawCanvasText(
            text = "Hueco $holeWidthLabel × $holeHeightLabel ft",
            x = size.width / 2f,
            y = oy + sh + 14.dp.toPx(),
            textSizePx = 12.dp.toPx(),
            color = Color(0xFFFFD54F),
            bold = true,
            centerAlign = true
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHolePanelGrid(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    columns: Int,
    rows: Int
) {
    if (columns <= 0 || rows <= 0) {
        drawRect(Color(0xFF1B5E20), Offset(left, top), Size(width, height))
        return
    }

    val cols = columns.coerceAtLeast(1)
    val rowCount = rows.coerceAtLeast(1)
    val cellW = width / cols
    val cellH = height / rowCount
    val ledMargin = min(cellW, cellH) * 0.08f
    val panelColor = Color(0xFF1565C0)
    val panelBright = Color(0xFF42A5F5)

    for (c in 0 until cols) {
        for (r in 0 until rowCount) {
            val cx = left + c * cellW
            val cy = top + r * cellH
            drawRect(Color(0xFF0D1117), Offset(cx, cy), Size(cellW, cellH))
            drawRect(panelColor, Offset(cx + ledMargin, cy + ledMargin), Size(cellW - ledMargin * 2, cellH - ledMargin * 2))
            drawRect(
                panelBright.copy(alpha = 0.35f),
                Offset(cx + ledMargin * 2, cy + ledMargin * 2),
                Size(cellW - ledMargin * 4, cellH - ledMargin * 4)
            )
        }
    }

    for (c in 0..cols) {
        val x = left + c * cellW
        drawLine(Color(0xFF263238), Offset(x, top), Offset(x, top + height), 1.2.dp.toPx())
    }
    for (r in 0..rowCount) {
        val y = top + r * cellH
        drawLine(Color(0xFF263238), Offset(left, y), Offset(left + width, y), 1.2.dp.toPx())
    }
}

@Composable
fun BasesSketch(
    modulesAcross: Int,
    modulesHigh: Int,
    support: SupportCalculation,
    modifier: Modifier = Modifier
) {
    val stairBlocks = BasesLayout.compute(modulesAcross, modulesHigh, support)?.stairBlocks?.size ?: 1
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(basesSketchHeight(modulesHigh, stairBlocks))
            .background(BasesSketchColors.canvasBg)
    ) {
        drawBasesSketchContent(modulesAcross, modulesHigh, support)
    }
}

@Composable
fun TrussSketch(
    trussWidthFeet: Int,
    trussHeightFeet: Int,
    screenWidthFeet: Double,
    screenHeightFeet: Double,
    modulesAcross: Int,
    modulesHigh: Int,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trussSketchHeight())
            .background(TrussSketchColors.canvasBg)
    ) {
        drawTrussSketchContent(
            trussWidthFeet = trussWidthFeet,
            trussHeightFeet = trussHeightFeet,
            screenWidthFeet = screenWidthFeet,
            screenHeightFeet = screenHeightFeet,
            modulesAcross = modulesAcross,
            modulesHigh = modulesHigh
        )
    }
}

@Composable
private fun SketchCanvas(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 180.dp,
    backgroundColor: Color = Color.Black,
    content: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor)
            .padding(6.dp)
    ) {
        content()
    }
}
