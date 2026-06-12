package com.eliezercruz.ledxcalc.ui.sketches

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.BasesLayout
import com.eliezercruz.ledxcalc.domain.SupportCalculation
import com.eliezercruz.ledxcalc.ui.drawing.drawCanvasText
import kotlin.math.min

internal object BasesSketchColors {
    val canvasBg = Color(0xFFE3F2FD)
    val screenOutline = Color(0xFF78909C)
    val structureFill = Color(0xFF212121)
    val structureStroke = Color(0xFF000000)
    val rungLine = Color(0xFF616161)
    val unitDivider = Color(0xFF9E9E9E)
    val labelDark = Color(0xFF37474F)
    val labelLight = Color(0xFFECEFF1)
    /** Relleno zona módulo(s) fantasma — distinto del fondo azul y de la pantalla LED. */
    val ghostFill = Color(0xFFE8D5F5)
    val ghostFillStripe = Color(0xFFD4B8EB)
    val ghostOutline = Color(0xFF7B1FA2)
    val ghostDivider = Color(0xFF9C27B0).copy(alpha = 0.45f)
}

private data class IsoDepth(val dx: Float, val dy: Float)

/** Etiqueta legible sobre fondo claro u oscuro: pastilla clara + texto oscuro. */
private fun DrawScope.drawSketchLabel(
    text: String,
    x: Float,
    y: Float,
    textSizePx: Float,
    bold: Boolean = true,
    centerAlign: Boolean = true
) {
    val padH = 6.dp.toPx()
    val padV = 4.dp.toPx()
    val approxW = text.length * textSizePx * 0.52f + padH * 2f
    val approxH = textSizePx * 1.35f + padV * 2f
    val left = if (centerAlign) x - approxW / 2f else x - padH
    val top = y - approxH / 2f
    drawRoundRect(
        color = Color(0xFFF5FAFF).copy(alpha = 0.96f),
        topLeft = Offset(left, top),
        size = Size(approxW, approxH),
        cornerRadius = CornerRadius(4.dp.toPx())
    )
    drawRoundRect(
        color = BasesSketchColors.labelDark.copy(alpha = 0.25f),
        topLeft = Offset(left, top),
        size = Size(approxW, approxH),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(1.dp.toPx())
    )
    drawCanvasText(
        text = text,
        x = x,
        y = y + textSizePx * 0.35f,
        textSizePx = textSizePx,
        color = BasesSketchColors.labelDark,
        bold = bold,
        centerAlign = centerAlign,
        withShadow = false
    )
}

private fun DrawScope.drawQuad(points: List<Offset>, fill: Color, stroke: Color? = null, strokeW: Float = 1.5f) {
    if (points.size != 4) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        lineTo(points[1].x, points[1].y)
        lineTo(points[2].x, points[2].y)
        lineTo(points[3].x, points[3].y)
        close()
    }
    drawPath(path, fill, style = Fill)
    if (stroke != null) {
        drawPath(path, stroke, style = Stroke(strokeW))
    }
}

/** Base en perspectiva (paralelogramo) unida, negra, con divisiones por módulo. */
private fun DrawScope.drawBaseUnitIso(
    frontLeft: Float,
    frontRight: Float,
    frontY: Float,
    depth: IsoDepth,
    cellW: Float,
    span: Int,
    label: String
) {
    val fl = Offset(frontLeft, frontY)
    val fr = Offset(frontRight, frontY)
    val bl = Offset(frontLeft + depth.dx, frontY - depth.dy)
    val br = Offset(frontRight + depth.dx, frontY - depth.dy)
    drawQuad(listOf(fl, fr, br, bl), BasesSketchColors.structureFill, BasesSketchColors.structureStroke, 2.dp.toPx())

    for (c in 1 until span) {
        val x = frontLeft + c * cellW
        drawLine(
            BasesSketchColors.rungLine,
            Offset(x, frontY),
            Offset(x + depth.dx, frontY - depth.dy),
            strokeWidth = 1.dp.toPx()
        )
    }

    drawSketchLabel(
        text = label,
        x = (frontLeft + frontRight) / 2f + depth.dx * 0.35f,
        y = frontY - depth.dy * 0.45f,
        textSizePx = min(cellW * 0.34f, 12.dp.toPx()).coerceAtLeast(10.dp.toPx())
    )
}

/** Contorno ligero de la pantalla (sin relleno azul). */
private fun DrawScope.drawScreenOutline(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    cellW: Float,
    cellH: Float,
    cols: Int,
    rows: Int
) {
    drawRect(
        BasesSketchColors.screenOutline,
        Offset(left, top),
        Size(width, height),
        style = Stroke(1.dp.toPx())
    )
    for (c in 1 until cols) {
        val x = left + c * cellW
        drawLine(BasesSketchColors.screenOutline.copy(alpha = 0.45f), Offset(x, top), Offset(x, top + height), 0.6.dp.toPx())
    }
    for (r in 1 until rows) {
        val y = top + r * cellH
        drawLine(BasesSketchColors.screenOutline.copy(alpha = 0.45f), Offset(left, y), Offset(left + width, y), 0.6.dp.toPx())
    }
}

/**
 * Escalera vertical: dos rieles + peldaños horizontales, una división por módulo.
 * Bloques apilados con escalera de 4 abajo.
 */
private fun DrawScope.drawVerticalLadder(
    centerX: Float,
    blockTopY: Float,
    blockHeightModules: Int,
    cellH: Float,
    ladderW: Float,
    depth: IsoDepth
) {
    val modules = blockHeightModules.coerceAtLeast(1)
    val blockH = modules * cellH
    val bottomY = blockTopY + blockH
    val halfW = ladderW / 2f
    val leftX = centerX - halfW
    val rightX = centerX + halfW
    val railDx = depth.dx * 0.18f
    val railDy = depth.dy * 0.18f

    drawRect(BasesSketchColors.structureFill, Offset(leftX, blockTopY), Size(ladderW, blockH))

    for (i in 1 until modules) {
        val rungY = blockTopY + i * cellH
        drawLine(
            BasesSketchColors.rungLine,
            Offset(leftX, rungY),
            Offset(rightX, rungY),
            strokeWidth = 1.4.dp.toPx()
        )
    }

    val strokeW = 2.5.dp.toPx()
    drawLine(BasesSketchColors.structureStroke, Offset(leftX, blockTopY), Offset(leftX, bottomY), strokeW)
    drawLine(
        BasesSketchColors.structureStroke,
        Offset(rightX, blockTopY),
        Offset(rightX + railDx, blockTopY - railDy),
        strokeW
    )
    drawLine(
        BasesSketchColors.structureStroke,
        Offset(rightX, bottomY),
        Offset(rightX + railDx, bottomY - railDy),
        strokeW
    )
    drawLine(
        BasesSketchColors.structureStroke,
        Offset(rightX + railDx, blockTopY - railDy),
        Offset(rightX + railDx, bottomY - railDy),
        strokeW
    )
    drawLine(BasesSketchColors.structureStroke, Offset(leftX, blockTopY), Offset(rightX, blockTopY), strokeW)
    drawLine(
        BasesSketchColors.structureStroke,
        Offset(leftX, bottomY),
        Offset(rightX, bottomY),
        strokeW
    )
}

internal fun DrawScope.drawBasesSketchContent(
    modulesAcross: Int,
    structureModulesHigh: Int,
    ledModulesHigh: Int,
    ghostModules: Int,
    support: SupportCalculation
) {
    val layout = BasesLayout.compute(modulesAcross, structureModulesHigh, support) ?: return

    val pad = 12.dp.toPx()
    val titleH = 18.dp.toPx()
    val baseFrontExtra = 14.dp.toPx()
    val baseLabelH = 28.dp.toPx()
    val gridTop = pad + titleH + 6.dp.toPx()

    drawRect(BasesSketchColors.canvasBg, Offset(0f, 0f), Size(size.width, size.height))

    val usableW = size.width - pad * 2
    val cellW = usableW / modulesAcross.coerceAtLeast(1)
    val depth = IsoDepth(
        dx = min(cellW * 0.42f, 28.dp.toPx()),
        dy = min(cellW * 0.28f, 18.dp.toPx())
    )
    val baseDepthY = depth.dy + baseFrontExtra

    val fixedBelow = baseDepthY + baseLabelH + pad + 6.dp.toPx()
    val availableGridH = (size.height - gridTop - fixedBelow).coerceAtLeast(56.dp.toPx())
    val cellH = availableGridH / structureModulesHigh.coerceAtLeast(1)
    val gridH = cellH * structureModulesHigh
    val ledGridH = cellH * ledModulesHigh.coerceAtLeast(1)
    val gridLeft = pad
    val gridRight = gridLeft + modulesAcross * cellW
    val screenBottom = gridTop + gridH
    val baseFrontY = screenBottom + 10.dp.toPx()
    val baseBackY = baseFrontY - depth.dy
    val ladderW = min(cellW * 0.62f, 22.dp.toPx())

    drawCanvasText(
        text = "Bases de piso y escaleras",
        x = size.width / 2f,
        y = pad,
        textSizePx = 13.dp.toPx(),
        color = BasesSketchColors.labelDark,
        bold = true,
        centerAlign = true,
        withShadow = false
    )

    // 1. Contorno pantalla LED (filas superiores)
    drawScreenOutline(gridLeft, gridTop, modulesAcross * cellW, ledGridH, cellW, cellH, modulesAcross, ledModulesHigh)
    drawCanvasText(
        text = "Pantalla LED",
        x = gridLeft + modulesAcross * cellW / 2f,
        y = gridTop - 4.dp.toPx(),
        textSizePx = 12.dp.toPx(),
        color = BasesSketchColors.labelDark,
        bold = true,
        centerAlign = true,
        withShadow = false
    )

    // 2. Zona fantasma bajo la pantalla (espacio vacío de gabinete)
    if (ghostModules > 0) {
        val ghostTop = gridTop + ledGridH
        val ghostH = cellH * ghostModules
        val ghostW = modulesAcross * cellW
        drawRect(
            BasesSketchColors.ghostFill,
            Offset(gridLeft, ghostTop),
            Size(ghostW, ghostH)
        )
        // Franjas horizontales suaves para marcar gabinetes vacíos
        var stripeY = ghostTop
        var stripeIdx = 0
        while (stripeY < ghostTop + ghostH) {
            if (stripeIdx % 2 == 1) {
                drawRect(
                    BasesSketchColors.ghostFillStripe.copy(alpha = 0.55f),
                    Offset(gridLeft, stripeY),
                    Size(ghostW, min(cellH, ghostTop + ghostH - stripeY))
                )
            }
            stripeY += cellH
            stripeIdx++
        }
        for (c in 1 until modulesAcross) {
            val x = gridLeft + c * cellW
            drawLine(
                BasesSketchColors.ghostDivider,
                Offset(x, ghostTop),
                Offset(x, ghostTop + ghostH),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (r in 1 until ghostModules) {
            val y = ghostTop + r * cellH
            drawLine(
                BasesSketchColors.ghostDivider,
                Offset(gridLeft, y),
                Offset(gridRight, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        drawRect(
            BasesSketchColors.ghostOutline,
            Offset(gridLeft, ghostTop),
            Size(ghostW, ghostH),
            style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
        )
        val ghostLabel = if (ghostModules == 1) "Módulo fantasma" else "Módulos fantasma ($ghostModules)"
        drawSketchLabel(
            text = ghostLabel,
            x = gridLeft + modulesAcross * cellW / 2f,
            y = ghostTop + ghostH / 2f,
            textSizePx = min(cellW * 0.34f, 11.dp.toPx()).coerceAtLeast(9.dp.toPx())
        )
    }

    // 3. Escaleras (altura total estructura)
    layout.stairColumns.forEach { col ->
        val anchorX = gridLeft + col * cellW + depth.dx * 0.5f
        var blockBottom = baseBackY
        layout.stairBlocks.forEach { blockHeight ->
            val blockH = blockHeight * cellH
            val blockTop = blockBottom - blockH
            drawVerticalLadder(
                centerX = anchorX,
                blockTopY = blockTop,
                blockHeightModules = blockHeight,
                cellH = cellH,
                ladderW = ladderW,
                depth = depth
            )
            val labelText = if (ladderW < 36.dp.toPx()) "Esc. $blockHeight" else "Escalera $blockHeight"
            drawSketchLabel(
                text = labelText,
                x = anchorX,
                y = blockTop + blockH / 2f,
                textSizePx = min(ladderW * 0.48f, 10.dp.toPx()).coerceAtLeast(9.dp.toPx())
            )
            blockBottom = blockTop
        }
    }

    // 4. Bases negras (delante)
    layout.baseUnits.forEach { unit ->
        val x0 = gridLeft + unit.startCol * cellW
        val x1 = gridLeft + (unit.startCol + unit.span) * cellW
        drawBaseUnitIso(
            frontLeft = x0,
            frontRight = x1,
            frontY = baseFrontY,
            depth = depth,
            cellW = cellW,
            span = unit.span,
            label = "Base de piso ${unit.span}"
        )
    }

    drawSketchLabel(
        text = "BASES DE PISO",
        x = gridLeft + modulesAcross * cellW / 2f + depth.dx * 0.2f,
        y = baseFrontY + baseFrontExtra + 10.dp.toPx(),
        textSizePx = 13.dp.toPx()
    )

    var edge = 0
    layout.baseUnits.forEach { unit ->
        edge += unit.span
        if (edge < modulesAcross) {
            val x = gridLeft + edge * cellW
            drawLine(
                BasesSketchColors.unitDivider,
                Offset(x, baseFrontY),
                Offset(x + depth.dx, baseBackY),
                strokeWidth = 2.5.dp.toPx()
            )
        }
    }

    drawLine(
        BasesSketchColors.structureStroke,
        Offset(gridLeft, screenBottom),
        Offset(gridRight, screenBottom),
        strokeWidth = 1.5.dp.toPx()
    )
}

internal fun basesSketchHeight(modulesHigh: Int, stairBlocks: Int): Dp =
    (300 + modulesHigh * 24 + stairBlocks * 8).coerceAtMost(440).dp
