package com.eliezercruz.ledxcalc.ui.sketches

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.ui.drawing.drawCanvasText
import com.eliezercruz.ledxcalc.util.formatDouble
import kotlin.math.min

internal object TrussSketchColors {
    val canvasBg = Color(0xFFF0F2F5)
    val trussFill = Color(0xFFB0BEC5)
    val trussStroke = Color(0xFF78909C)
    val trussInner = Color(0xFF90A4AE)
    val baseFill = Color(0xFF212121)
    val baseStroke = Color(0xFF000000)
    val screenFill = Color(0xFF111111)
    val screenGrid = Color(0xFF37474F)
    val screenStroke = Color(0xFF263238)
    val rigging = Color(0xFF546E7A)
    val labelDark = Color(0xFF37474F)
    val labelLight = Color(0xFFECEFF1)
}

/** Segmento de truss tipo caja con celosía (diagonal + travesaños). */
private fun DrawScope.drawBoxTrussSegment(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    fill: Color = TrussSketchColors.trussFill,
    stroke: Color = TrussSketchColors.trussStroke
) {
    if (width <= 0f || height <= 0f) return
    drawRect(fill, Offset(left, top), Size(width, height))
    drawRect(stroke, Offset(left, top), Size(width, height), style = Stroke(2.dp.toPx()))

    val sw = 1.2.dp.toPx()
    drawLine(stroke, Offset(left, top), Offset(left + width, top + height), sw)
    drawLine(stroke, Offset(left + width, top), Offset(left, top + height), sw)

    val rows = (height / 18.dp.toPx()).toInt().coerceIn(2, 8)
    for (i in 1 until rows) {
        val y = top + i * height / rows
        drawLine(TrussSketchColors.trussInner, Offset(left, y), Offset(left + width, y), sw * 0.8f)
    }
    if (width > height * 1.2f) {
        val cols = (width / 22.dp.toPx()).toInt().coerceIn(2, 10)
        for (i in 1 until cols) {
            val x = left + i * width / cols
            drawLine(TrussSketchColors.trussInner, Offset(x, top), Offset(x, top + height), sw * 0.8f)
        }
    }
}

private fun DrawScope.drawFloorBase(centerX: Float, bottomY: Float, size: Float) {
    val half = size / 2f
    drawRect(TrussSketchColors.baseFill, Offset(centerX - half, bottomY - size), Size(size, size))
    drawRect(
        TrussSketchColors.baseStroke,
        Offset(centerX - half, bottomY - size),
        Size(size, size),
        style = Stroke(1.5.dp.toPx())
    )
}

private fun DrawScope.drawHangingScreen(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    modulesAcross: Int,
    modulesHigh: Int,
    beamY: Float,
    riggingPoints: List<Float>
) {
    riggingPoints.forEach { rx ->
        drawLine(TrussSketchColors.rigging, Offset(rx, beamY), Offset(rx, top), 1.5.dp.toPx())
    }

    drawRect(TrussSketchColors.screenFill, Offset(left, top), Size(width, height))
    drawRect(TrussSketchColors.screenStroke, Offset(left, top), Size(width, height), style = Stroke(2.dp.toPx()))

    val cols = modulesAcross.coerceAtLeast(1)
    val rows = modulesHigh.coerceAtLeast(1)
    for (c in 1 until cols) {
        val x = left + c * width / cols
        drawLine(TrussSketchColors.screenGrid, Offset(x, top), Offset(x, top + height), 0.8.dp.toPx())
    }
    for (r in 1 until rows) {
        val y = top + r * height / rows
        drawLine(TrussSketchColors.screenGrid, Offset(left, y), Offset(left + width, y), 0.8.dp.toPx())
    }

    drawCanvasText(
        text = "Pantalla LED",
        x = left + width / 2f,
        y = top + height / 2f,
        textSizePx = min(width * 0.12f, 11.dp.toPx()),
        color = TrussSketchColors.labelLight,
        bold = true,
        centerAlign = true
    )
}

internal fun DrawScope.drawTrussSketchContent(
    trussWidthFeet: Int,
    trussHeightFeet: Int,
    screenWidthFeet: Double,
    screenHeightFeet: Double,
    modulesAcross: Int,
    modulesHigh: Int
) {
    val pad = 14.dp.toPx()
    val titleH = 20.dp.toPx()
    val footerH = 52.dp.toPx()

    drawRect(TrussSketchColors.canvasBg, Offset(0f, 0f), Size(size.width, size.height))

    drawCanvasText(
        text = "Truss goalpost — pantalla colgada",
        x = size.width / 2f,
        y = pad,
        textSizePx = 12.dp.toPx(),
        color = TrussSketchColors.labelDark,
        bold = true,
        centerAlign = true
    )

    val usableW = size.width - pad * 2
    val usableH = size.height - pad * 2 - titleH - footerH
    val scale = min(usableW / trussWidthFeet, usableH / trussHeightFeet)
    val tw = trussWidthFeet * scale
    val th = trussHeightFeet * scale
    val ox = (size.width - tw) / 2f
    val oy = pad + titleH

    val pillarW = min(tw * 0.09f, 28.dp.toPx())
    val beamH = min(pillarW * 0.85f, 22.dp.toPx())
    val baseSize = min(pillarW * 1.35f, 34.dp.toPx())
    val floorY = oy + th

    val leftPillarX = ox
    val rightPillarX = ox + tw - pillarW
    val pillarTop = oy + beamH
    val pillarH = floorY - baseSize - pillarTop

    // Viga horizontal superior
    drawBoxTrussSegment(ox, oy, tw, beamH)

    // Torres verticales
    drawBoxTrussSegment(leftPillarX, pillarTop, pillarW, pillarH)
    drawBoxTrussSegment(rightPillarX, pillarTop, pillarW, pillarH)

    // Bases de piso
    drawFloorBase(leftPillarX + pillarW / 2f, floorY, baseSize)
    drawFloorBase(rightPillarX + pillarW / 2f, floorY, baseSize)

    // Línea de piso
    drawLine(
        TrussSketchColors.trussStroke,
        Offset(ox - 8.dp.toPx(), floorY),
        Offset(ox + tw + 8.dp.toPx(), floorY),
        strokeWidth = 2.dp.toPx()
    )

    // Pantalla colgada (centrada, con holgura respecto a torres)
    val sw = screenWidthFeet.toFloat() * scale
    val sh = screenHeightFeet.toFloat() * scale
    val sx = ox + (tw - sw) / 2f
    val gapBelowBeam = beamH * 0.35f
    val sy = pillarTop + gapBelowBeam
    val riggingXs = listOf(
        sx + sw * 0.2f,
        sx + sw * 0.5f,
        sx + sw * 0.8f
    )
    drawHangingScreen(
        left = sx,
        top = sy,
        width = sw,
        height = sh,
        modulesAcross = modulesAcross,
        modulesHigh = modulesHigh,
        beamY = oy + beamH,
        riggingPoints = riggingXs
    )

    val footerTop = size.height - footerH + 6.dp.toPx()
    val footerBoxH = footerH - 8.dp.toPx()
    val trussDims = "${trussWidthFeet} × ${trussHeightFeet} ft"
    val screenDims = "Pantalla ${formatDouble(screenWidthFeet, 1)} × ${formatDouble(screenHeightFeet, 1)} ft"

    drawRect(
        Color(0xFFE0E0E0),
        Offset(pad, footerTop),
        Size(size.width - pad * 2, footerBoxH)
    )
    drawRect(
        TrussSketchColors.trussStroke,
        Offset(pad, footerTop),
        Size(size.width - pad * 2, footerBoxH),
        style = Stroke(1.dp.toPx())
    )

    val labelY = footerTop + footerBoxH * 0.28f
    val dimsY = footerTop + footerBoxH * 0.58f
    val screenY = footerTop + footerBoxH * 0.86f

    drawCanvasText(
        text = "Truss recomendado",
        x = size.width / 2f,
        y = labelY,
        textSizePx = 11.dp.toPx(),
        color = TrussSketchColors.labelDark,
        bold = true,
        centerAlign = true,
        withShadow = false
    )
    drawCanvasText(
        text = trussDims,
        x = size.width / 2f,
        y = dimsY,
        textSizePx = 15.dp.toPx(),
        color = Color(0xFF0277BD),
        bold = true,
        centerAlign = true,
        withShadow = false
    )
    drawCanvasText(
        text = screenDims,
        x = size.width / 2f,
        y = screenY,
        textSizePx = 10.dp.toPx(),
        color = Color(0xFF546E7A),
        centerAlign = true,
        withShadow = false
    )
}

internal fun trussSketchHeight(): Dp = 320.dp
