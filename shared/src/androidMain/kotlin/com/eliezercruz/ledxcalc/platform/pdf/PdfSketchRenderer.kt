package com.eliezercruz.ledxcalc.platform.pdf

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.eliezercruz.ledxcalc.domain.BasesDistribution
import com.eliezercruz.ledxcalc.domain.ModuleSignalLayout
import com.eliezercruz.ledxcalc.domain.PdfExportData
import com.eliezercruz.ledxcalc.domain.SketchKind
import com.eliezercruz.ledxcalc.domain.StructureMounting
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal object PdfSketchRenderer {
    private const val PAGE_HEIGHT = 792f
    private const val CREDIT_RESERVE = 28f
    /** Espacio reservado abajo para specs (más alto = boceto más chico). */
    private const val SPECS_RESERVE = 310f
    private const val GRID_HEIGHT_SCALE = 0.72f

    private val specWhite = Color.White
    private val specLabelBlue = Color(0xFF00E5FF)
    private val specLabelGreen = Color(0xFF69F0AE)
    private val titleTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    private val bodyTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    private val groupColors = listOf(
        Color(0xFF0066FF),
        Color(0xFF00FF66),
        Color(0xFFFF6600),
        Color(0xFFFF00FF),
        Color(0xFFFFFF00),
        Color(0xFF00FFFF)
    )

    /** Una sola hoja: boceto de pantalla + especificaciones abajo (formato referencia). */
    fun drawSinglePage(
        canvas: Canvas,
        data: PdfExportData,
        paint: Paint,
        pageWidth: Int,
        pageHeight: Int = PAGE_HEIGHT.toInt()
    ) {
        val pageH = pageHeight.toFloat()
        fillBlack(canvas, pageWidth, pageHeight)

        paint.textAlign = Paint.Align.CENTER
        applyPlainText(paint, titleTypeface, 17f, specWhite)
        canvas.drawText("BOCETO DE PANTALLA LED", pageWidth / 2f, 38f, paint)

        applyPlainText(paint, bodyTypeface, 14f, specWhite)
        val sizeLine = if (data.inputWidthMeters != null && data.inputHeightMeters != null) {
            "Tamaño de pantalla: ${formatMeters(data.inputWidthMeters)} × ${formatMeters(data.inputHeightMeters)} metros"
        } else {
            "Tamaño de pantalla: ${data.displayWidth} × ${data.displayHeight} ${data.unitLabel}"
        }
        canvas.drawText(sizeLine, pageWidth / 2f, 58f, paint)

        var specsStartY = pageH - CREDIT_RESERVE - SPECS_RESERVE
        if (data.columns > 0 && data.rows > 0) {
            val regionTop = 72f
            val regionBottom = pageH - CREDIT_RESERVE - SPECS_RESERVE - 18f
            val margin = 36f
            val maxGridW = pageWidth - margin * 2
            val maxGridH = (regionBottom - regionTop) * GRID_HEIGHT_SCALE
            val cellSize = min(maxGridW / data.columns, maxGridH / data.rows)
            val gridH = cellSize * data.rows
            val gridW = cellSize * data.columns
            val gridLeft = (pageWidth - gridW) / 2f
            val gridTop = regionTop + (regionBottom - regionTop - gridH) / 2f

            drawModuleGridStyled(
                canvas, data, paint, gridLeft, gridTop, cellSize, cellSize,
                drawBorder = true, showOrderBadges = true, showLineLabels = true, showArrows = true
            )
            specsStartY = regionBottom + 10f
        }

        drawSpecsFooter(canvas, data, paint, pageWidth, specsStartY)

        paint.textAlign = Paint.Align.CENTER
        applyPlainText(paint, bodyTypeface, 11f, specWhite)
        canvas.drawText(
            "Generado por LedxCalc — Creado por Eliezer Cruz",
            pageWidth / 2f,
            pageH - 14f,
            paint
        )
    }

    private fun drawSpecsFooter(
        canvas: Canvas,
        data: PdfExportData,
        paint: Paint,
        pageWidth: Int,
        startY: Float
    ) {
        val colGap = 20f
        val colW = (pageWidth - 48f - colGap) / 2f
        val leftX = 24f
        val rightX = leftX + colW + colGap
        val lineH = 21f
        val valueXLeft = leftX + 148f
        val valueXRight = rightX + 148f
        val bodySize = 14.5f
        val headerSize = 16f

        paint.textAlign = Paint.Align.LEFT
        applyPlainText(paint, bodyTypeface, headerSize, specLabelBlue)
        canvas.drawText("ESPECIFICACIONES TÉCNICAS", leftX, startY, paint)
        applyPlainText(paint, bodyTypeface, headerSize, specLabelGreen)
        canvas.drawText("ESTRUCTURA", rightX, startY, paint)

        val leftLines = buildList {
            add("Columnas:" to "${data.columns}")
            add("Filas LED:" to "${data.rows}")
            if (data.ghostModules > 0) {
                add("Módulos fantasma:" to "${data.ghostModules}")
                add("Estructura montaje:" to "${data.columns} × ${data.structureModulesHigh} gab.")
            }
            add("Total módulos:" to "${data.totalModules}")
            add("Resolución:" to "${data.widthPixels} × ${data.heightPixels} px")
            add("Cobertura:" to "${data.displayWidth} × ${data.displayHeight} ${data.unitLabel}")
            add("Líneas de señal:" to "${data.signalLines} (máx ${data.groupSize}/línea)")
            add("Hueco pantalla:" to "${data.holeWidthFormatted} × ${data.holeHeightFormatted} ft")
            add("Módulo:" to data.moduleSpec.title)
            if (SketchKind.ELECTRICAL in data.selectedSketches) {
                val load = data.electrical.loadResult
                val v = data.electrical.selectedVoltage.label
                add("Amperaje prom ($v):" to "${load.amperajePromedioFormatted} A")
                add("Amperaje máx ($v):" to "${load.amperajeMaxFormatted} A")
            }
        }

        val rightLines = buildList {
            when (data.structureMounting) {
                StructureMounting.FLOOR_BASES -> {
                    add("Montaje:" to "Bases de piso")
                    val s = data.supportCalc
                    BasesDistribution.baseLines(s).forEach { line ->
                        val parts = line.split(": ", limit = 2)
                        add(parts[0] + ":" to parts.getOrElse(1) { "" })
                    }
                    BasesDistribution.stairLines(s).forEach { line ->
                        val parts = line.split(": ", limit = 2)
                        add(parts[0] + ":" to parts.getOrElse(1) { "" })
                    }
                }
                StructureMounting.TRUSS -> {
                    add("Montaje:" to "Truss / colgado")
                    add("Truss:" to "${data.trussWidthFeet} × ${data.trussHeightFeet} ft")
                }
            }
        }

        var yLeft = startY + 22f
        leftLines.forEach { (label, value) ->
            applyPlainText(paint, bodyTypeface, bodySize, specLabelBlue)
            canvas.drawText(label, leftX, yLeft, paint)
            applyPlainText(paint, bodyTypeface, bodySize, specWhite)
            canvas.drawText(value, valueXLeft, yLeft, paint)
            yLeft += lineH
        }

        var yRight = startY + 22f
        rightLines.forEach { (label, value) ->
            applyPlainText(paint, bodyTypeface, bodySize, specLabelGreen)
            canvas.drawText(label, rightX, yRight, paint)
            applyPlainText(paint, bodyTypeface, bodySize, specWhite)
            canvas.drawText(value, valueXRight, yRight, paint)
            yRight += lineH
        }
    }

    /** Misma familia que el título, sin negrita, sin trazo ni sombra. */
    private fun applyPlainText(paint: Paint, typeface: Typeface, textSize: Float, color: Color) {
        paint.typeface = typeface
        paint.textSize = textSize
        paint.color = color.toArgb()
        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = false
        paint.strokeWidth = 0f
        paint.clearShadowLayer()
    }

    private fun drawModuleGridStyled(
        canvas: Canvas,
        data: PdfExportData,
        paint: Paint,
        left: Float,
        top: Float,
        cellW: Float,
        cellH: Float,
        drawBorder: Boolean,
        showOrderBadges: Boolean,
        showLineLabels: Boolean,
        showArrows: Boolean
    ) {
        if (data.columns <= 0 || data.rows <= 0) return
        val layout = ModuleSignalLayout.compute(data.columns, data.rows, data.groupSize) ?: return

        fun cellCenter(col: Int, row: Int): Pair<Float, Float> = Pair(
            left + col * cellW + cellW / 2f,
            top + row * cellH + cellH / 2f
        )

        for (c in 0 until data.columns) {
            for (r in 0 until data.rows) {
                val l = left + c * cellW
                val t = top + r * cellH
                val groupIdx = layout.moduleGroupIndex[Pair(c, r)] ?: 0
                val base = groupColors[groupIdx % groupColors.size]
                paint.style = Paint.Style.FILL
                paint.color = base.toArgb()
                canvas.drawRect(l + 1.5f, t + 1.5f, l + cellW - 1.5f, t + cellH - 1.5f, paint)
                paint.color = base.copy(alpha = 0.45f).toArgb()
                canvas.drawRect(l + 2f, t + 2f, l + cellW * 0.38f, t + cellH * 0.18f, paint)
            }
        }

        paint.color = Color.White.toArgb()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.8f
        paint.strokeCap = Paint.Cap.ROUND
        layout.signalPaths.forEach { path ->
            for (i in 0 until path.size - 1) {
                val (x1, y1) = cellCenter(path[i].first, path[i].second)
                val (x2, y2) = cellCenter(path[i + 1].first, path[i + 1].second)
                canvas.drawLine(x1, y1, x2, y2, paint)
                if (showArrows) drawArrowHead(canvas, paint, x1, y1, x2, y2)
            }
        }

        if (showLineLabels) {
            layout.signalPaths.forEachIndexed { groupIdx, path ->
                val first = path.firstOrNull() ?: return@forEachIndexed
                val (fx, fy) = cellCenter(first.first, first.second)
                paint.style = Paint.Style.FILL
                paint.color = Color(0xFF00FF66).toArgb()
                val r = min(cellW, cellH) * 0.22f
                canvas.drawCircle(fx, fy, r, paint)
                paint.color = Color.White.toArgb()
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = r * 1.1f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText((groupIdx + 1).toString(), fx, fy + r * 0.35f, paint)
            }
        }

        if (showOrderBadges) {
            paint.textAlign = Paint.Align.CENTER
            layout.signalPaths.forEach { path ->
                path.forEachIndexed { orderIdx, (col, row) ->
                    val (cx, cy) = cellCenter(col, row)
                    paint.style = Paint.Style.FILL
                    paint.color = Color.Black.copy(alpha = 0.75f).toArgb()
                    val badgeR = min(cellW, cellH) * 0.16f
                    canvas.drawCircle(cx, cy, badgeR, paint)
                    paint.color = Color.White.toArgb()
                    paint.textSize = badgeR * 1f
                    canvas.drawText((orderIdx + 1).toString(), cx, cy + badgeR * 0.32f, paint)
                }
            }
        }

        if (drawBorder) {
            paint.color = Color(0xFF1A1A1A).toArgb()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            for (c in 0..data.columns) {
                val x = left + c * cellW
                canvas.drawLine(x, top, x, top + data.rows * cellH, paint)
            }
            for (r in 0..data.rows) {
                val y = top + r * cellH
                canvas.drawLine(left, y, left + data.columns * cellW, y, paint)
            }
        }
    }

    private fun drawArrowHead(canvas: Canvas, paint: Paint, x1: Float, y1: Float, x2: Float, y2: Float) {
        val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        val headLen = 7f
        val path = Path().apply {
            moveTo(x2, y2)
            lineTo(
                x2 - headLen * cos(angle - Math.PI / 6).toFloat(),
                y2 - headLen * sin(angle - Math.PI / 6).toFloat()
            )
            lineTo(
                x2 - headLen * cos(angle + Math.PI / 6).toFloat(),
                y2 - headLen * sin(angle + Math.PI / 6).toFloat()
            )
            close()
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.White.toArgb()
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun fillBlack(canvas: Canvas, pageWidth: Int, pageHeight: Int) {
        val bg = Paint().apply { color = Color.Black.toArgb() }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bg)
    }

    private fun formatMeters(value: Double): String =
        String.format(java.util.Locale.US, "%.2f", value)
}
