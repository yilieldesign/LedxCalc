package com.eliezercruz.ledxcalc.platform.pdf

import com.eliezercruz.ledxcalc.domain.BasesDistribution
import com.eliezercruz.ledxcalc.domain.ModuleSignalLayout
import com.eliezercruz.ledxcalc.domain.PdfExportData
import com.eliezercruz.ledxcalc.domain.SketchKind
import com.eliezercruz.ledxcalc.domain.StructureMounting
import com.eliezercruz.ledxcalc.util.formatDouble
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Genera un PDF a color (carta 612×792) equivalente al boceto Android:
 * fondo negro, rejilla LED por grupos, specs cyan/verde y pie de crédito.
 * Usado en iOS y Web; Android sigue con PdfDocument nativo.
 */
object LedxColoredPdf {
    private const val PAGE_W = 612f
    private const val PAGE_H = 792f
    private const val CREDIT_RESERVE = 28f
    /** Espacio reservado abajo para specs (más alto = boceto más chico). */
    private const val SPECS_RESERVE = 310f
    /** Tope adicional del boceto para dejar aire a la tipografía. */
    private const val GRID_HEIGHT_SCALE = 0.72f

    private val groupColors = listOf(
        PdfRgb(0f, 0.4f, 1f),
        PdfRgb(0f, 1f, 0.4f),
        PdfRgb(1f, 0.4f, 0f),
        PdfRgb(1f, 0f, 1f),
        PdfRgb(1f, 1f, 0f),
        PdfRgb(0f, 1f, 1f)
    )

    fun build(data: PdfExportData): ByteArray {
        val content = StringBuilder()
        // Fondo negro
        content.append("0 0 0 rg 0 0 $PAGE_W $PAGE_H re f\n")

        // Título
        content.appendTextCentered(PAGE_W / 2f, PAGE_H - 38f, 17f, PdfRgb.WHITE, "BOCETO DE PANTALLA LED", bold = true)
        val sizeLine = if (data.inputWidthMeters != null && data.inputHeightMeters != null) {
            "Tamaño de pantalla: ${formatDouble(data.inputWidthMeters, 2)} × ${formatDouble(data.inputHeightMeters, 2)} metros"
        } else {
            "Tamaño de pantalla: ${data.displayWidth} × ${data.displayHeight} ${data.unitLabel}"
        }
        content.appendTextCentered(PAGE_W / 2f, PAGE_H - 58f, 12f, PdfRgb.WHITE, sizeLine)

        var specsStartY = CREDIT_RESERVE + SPECS_RESERVE
        if (data.columns > 0 && data.rows > 0) {
            val gridTopFromBottom = PAGE_H - 72f
            val maxSpecsBottom = CREDIT_RESERVE + SPECS_RESERVE
            val margin = 36f
            val maxGridW = PAGE_W - margin * 2
            val maxGridH = (gridTopFromBottom - maxSpecsBottom - 18f) * GRID_HEIGHT_SCALE
            val cellSize = min(maxGridW / data.columns, maxGridH / data.rows)
            val gridH = cellSize * data.rows
            val gridW = cellSize * data.columns
            val gridLeft = (PAGE_W - gridW) / 2f
            // Centrar el boceto en la franja superior disponible
            val availableTop = gridTopFromBottom
            val availableBottom = maxSpecsBottom + 18f
            val gridBottom = availableBottom + (availableTop - availableBottom - gridH) / 2f

            drawModuleGrid(content, data, gridLeft, gridBottom, cellSize, cellSize)
            specsStartY = availableBottom - 8f
        }

        drawSpecsFooter(content, data, specsStartY)

        content.appendTextCentered(
            PAGE_W / 2f,
            14f,
            10f,
            PdfRgb.WHITE,
            "Generado por LedxCalc — Creado por Eliezer Cruz"
        )

        return assemblePdf(content.toString())
    }

    fun fileName(data: PdfExportData): String {
        val size = if (data.inputWidthMeters != null && data.inputHeightMeters != null) {
            "${formatDouble(data.inputWidthMeters, 2)}x${formatDouble(data.inputHeightMeters, 2)}"
        } else {
            "pantalla"
        }
        return "LedxCalc_${size}.pdf"
    }

    private fun drawModuleGrid(
        content: StringBuilder,
        data: PdfExportData,
        left: Float,
        bottom: Float,
        cellW: Float,
        cellH: Float
    ) {
        val layout = ModuleSignalLayout.compute(data.columns, data.rows, data.groupSize) ?: return
        val top = bottom + data.rows * cellH

        fun cellCenter(col: Int, row: Int): Pair<Float, Float> {
            // row 0 = top row visually (same as Android)
            val cx = left + col * cellW + cellW / 2f
            val cy = top - row * cellH - cellH / 2f
            return cx to cy
        }

        for (c in 0 until data.columns) {
            for (r in 0 until data.rows) {
                val l = left + c * cellW
                val cellBottom = top - (r + 1) * cellH
                val groupIdx = layout.moduleGroupIndex[Pair(c, r)] ?: 0
                val base = groupColors[groupIdx % groupColors.size]
                content.appendRect(l + 1.5f, cellBottom + 1.5f, cellW - 3f, cellH - 3f, base, fill = true)
                content.appendRect(
                    l + 2f,
                    cellBottom + cellH - cellH * 0.18f - 2f,
                    cellW * 0.38f,
                    cellH * 0.18f,
                    base.copy(alpha = 0.45f),
                    fill = true
                )
            }
        }

        // Signal paths
        content.append("1 w 1 1 1 RG 1 J\n")
        layout.signalPaths.forEach { path ->
            for (i in 0 until path.size - 1) {
                val (x1, y1) = cellCenter(path[i].first, path[i].second)
                val (x2, y2) = cellCenter(path[i + 1].first, path[i + 1].second)
                content.append("$x1 $y1 m $x2 $y2 l S\n")
                drawArrowHead(content, x1, y1, x2, y2)
            }
        }

        // Line start badges
        layout.signalPaths.forEachIndexed { groupIdx, path ->
            val first = path.firstOrNull() ?: return@forEachIndexed
            val (fx, fy) = cellCenter(first.first, first.second)
            val r = min(cellW, cellH) * 0.22f
            content.appendCircle(fx, fy, r, PdfRgb(0f, 1f, 0.4f), fill = true)
            content.appendTextCentered(fx, fy - r * 0.35f, r * 1.1f, PdfRgb.WHITE, "${groupIdx + 1}", bold = true)
        }

        // Order badges
        layout.signalPaths.forEach { path ->
            path.forEachIndexed { orderIdx, (col, row) ->
                val (cx, cy) = cellCenter(col, row)
                val badgeR = min(cellW, cellH) * 0.16f
                content.appendCircle(cx, cy, badgeR, PdfRgb(0f, 0f, 0f, 0.75f), fill = true)
                content.appendTextCentered(cx, cy - badgeR * 0.32f, badgeR, PdfRgb.WHITE, "${orderIdx + 1}")
            }
        }

        // Grid lines
        content.append("1.5 w 0.1 0.1 0.1 RG\n")
        for (c in 0..data.columns) {
            val x = left + c * cellW
            content.append("$x $bottom m $x $top l S\n")
        }
        for (r in 0..data.rows) {
            val y = bottom + r * cellH
            content.append("$left $y m ${left + data.columns * cellW} $y l S\n")
        }
    }

    private fun drawArrowHead(content: StringBuilder, x1: Float, y1: Float, x2: Float, y2: Float) {
        val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        val headLen = 7.0
        val ax = x2 - headLen * cos(angle - PI / 6)
        val ay = y2 - headLen * sin(angle - PI / 6)
        val bx = x2 - headLen * cos(angle + PI / 6)
        val by = y2 - headLen * sin(angle + PI / 6)
        content.append("1 1 1 rg $x2 $y2 m $ax $ay l $bx $by l h f\n")
    }

    private fun drawSpecsFooter(content: StringBuilder, data: PdfExportData, startYFromBottom: Float) {
        // startYFromBottom is PDF Y of the header baseline area (approx top of specs block)
        val includeStructure = data.includeStructure
        val colGap = 20f
        val leftX = 24f
        val rightX = leftX + (PAGE_W - 48f - colGap) / 2f + colGap
        val valueXLeft = leftX + if (includeStructure) 148f else 168f
        val valueXRight = rightX + 148f
        val lineH = if (includeStructure) 19f else 20f
        val bodySize = if (includeStructure) 12.5f else 13.5f
        val headerSize = if (includeStructure) 14.5f else 15.5f
        val headerY = startYFromBottom

        content.appendText(leftX, headerY, headerSize, PdfRgb.CYAN, "ESPECIFICACIONES TÉCNICAS", bold = true)
        if (includeStructure) {
            content.appendText(rightX, headerY, headerSize, PdfRgb.GREEN, "ESTRUCTURA", bold = true)
        }

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
            add("Módulo:" to data.moduleSpec.title.take(if (includeStructure) 42 else 56))
            if (SketchKind.ELECTRICAL in data.selectedSketches) {
                val load = data.electrical.loadResult
                val v = data.electrical.selectedVoltage.label
                add("Amperaje prom ($v):" to "${load.amperajePromedioFormatted} A")
                add("Amperaje máx ($v):" to "${load.amperajeMaxFormatted} A")
            }
        }

        var yLeft = headerY - 22f
        leftLines.forEach { (label, value) ->
            content.appendText(leftX, yLeft, bodySize, PdfRgb.CYAN, label)
            content.appendText(valueXLeft, yLeft, bodySize, PdfRgb.WHITE, value)
            yLeft -= lineH
        }

        if (!includeStructure) return

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

        var yRight = headerY - 22f
        rightLines.forEach { (label, value) ->
            content.appendText(rightX, yRight, bodySize, PdfRgb.GREEN, label)
            content.appendText(valueXRight, yRight, bodySize, PdfRgb.WHITE, value)
            yRight -= lineH
        }
    }

    private fun assemblePdf(contentStream: String): ByteArray {
        val objects = mutableListOf<String>()
        objects += "1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n"
        objects += "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n"
        objects += "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${PAGE_W.toInt()} ${PAGE_H.toInt()}] " +
            "/Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>endobj\n"
        // PDF offsets must match byte length; use Latin-1 (1 char = 1 byte), never UTF-8.
        val streamLen = contentStream.length
        objects += "4 0 obj<< /Length $streamLen >>stream\n$contentStream\nendstream\nendobj\n"
        objects += "5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>endobj\n"
        objects += "6 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>endobj\n"

        val header = "%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n"
        val builder = StringBuilder(header)
        val offsets = IntArray(objects.size + 1)
        offsets[0] = 0
        for (i in objects.indices) {
            offsets[i + 1] = builder.length
            builder.append(objects[i])
        }
        val xrefPos = builder.length
        builder.append("xref\n0 ${objects.size + 1}\n")
        builder.append("0000000000 65535 f \n")
        for (i in 1..objects.size) {
            builder.append(offsets[i].toString().padStart(10, '0'))
            builder.append(" 00000 n \n")
        }
        builder.append("trailer<< /Size ${objects.size + 1} /Root 1 0 R >>\n")
        builder.append("startxref\n$xrefPos\n%%EOF\n")
        return builder.toString().toLatin1Bytes()
    }
}

/** One Kotlin Char → one PDF byte (WinAnsi / binary marker safe). */
private fun String.toLatin1Bytes(): ByteArray {
    val out = ByteArray(length)
    for (i in indices) {
        val code = this[i].code
        out[i] = (if (code <= 0xFF) code else '?'.code).toByte()
    }
    return out
}

private data class PdfRgb(val r: Float, val g: Float, val b: Float, val alpha: Float = 1f) {
    fun pdfFill(): String = "${r * alpha} ${g * alpha} ${b * alpha} rg"
    fun pdfStroke(): String = "$r $g $b RG"

    companion object {
        val WHITE = PdfRgb(1f, 1f, 1f)
        val CYAN = PdfRgb(0f, 0.898f, 1f)
        val GREEN = PdfRgb(0.412f, 0.941f, 0.682f)
    }
}

private fun StringBuilder.appendRect(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    color: PdfRgb,
    fill: Boolean
) {
    append(color.pdfFill())
    append(" ")
    append("$x $y $w $h re ")
    append(if (fill) "f\n" else "S\n")
}

private fun StringBuilder.appendCircle(cx: Float, cy: Float, r: Float, color: PdfRgb, fill: Boolean) {
    // Bézier circle approximation
    val k = 0.5522847498f * r
    append(color.pdfFill())
    append(" ")
    append("${cx - r} $cy m ")
    append("${cx - r} ${cy + k} ${cx - k} ${cy + r} $cx ${cy + r} c ")
    append("${cx + k} ${cy + r} ${cx + r} ${cy + k} ${cx + r} $cy c ")
    append("${cx + r} ${cy - k} ${cx + k} ${cy - r} $cx ${cy - r} c ")
    append("${cx - k} ${cy - r} ${cx - r} ${cy - k} ${cx - r} $cy c ")
    append(if (fill) "f\n" else "S\n")
}

private fun StringBuilder.appendText(
    x: Float,
    y: Float,
    size: Float,
    color: PdfRgb,
    text: String,
    bold: Boolean = false
) {
    val font = if (bold) "/F2" else "/F1"
    append("BT $font $size Tf ${color.pdfFill()} 1 0 0 1 $x $y Tm (${escapePdfString(text)}) Tj ET\n")
}

private fun StringBuilder.appendTextCentered(
    x: Float,
    y: Float,
    size: Float,
    color: PdfRgb,
    text: String,
    bold: Boolean = false
) {
    // Approximate center with Helvetica width ~0.5*size*chars
    val approxW = text.length * size * 0.48f
    appendText(x - approxW / 2f, y, size, color, text, bold)
}

private fun escapePdfString(text: String): String {
    val sb = StringBuilder()
    for (ch in text) {
        when (ch) {
            '\\' -> sb.append("\\\\")
            '(' -> sb.append("\\(")
            ')' -> sb.append("\\)")
            '\r' -> sb.append("\\r")
            '\n' -> sb.append("\\n")
            // WinAnsi common Spanish / symbols
            'á' -> sb.append("\\341")
            'é' -> sb.append("\\351")
            'í' -> sb.append("\\355")
            'ó' -> sb.append("\\363")
            'ú' -> sb.append("\\372")
            'ñ' -> sb.append("\\361")
            'Á' -> sb.append("\\301")
            'É' -> sb.append("\\311")
            'Í' -> sb.append("\\315")
            'Ó' -> sb.append("\\323")
            'Ú' -> sb.append("\\332")
            'Ñ' -> sb.append("\\321")
            'ü' -> sb.append("\\374")
            '×' -> sb.append("\\327")
            '—' -> sb.append("---")
            '–' -> sb.append("-")
            '·' -> sb.append("\\267")
            else -> if (ch.code in 32..126) sb.append(ch) else sb.append('?')
        }
    }
    return sb.toString()
}
