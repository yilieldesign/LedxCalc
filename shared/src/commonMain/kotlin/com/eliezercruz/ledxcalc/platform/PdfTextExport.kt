package com.eliezercruz.ledxcalc.platform

import com.eliezercruz.ledxcalc.domain.BasesDistribution
import com.eliezercruz.ledxcalc.domain.PdfExportData
import com.eliezercruz.ledxcalc.domain.SketchKind
import com.eliezercruz.ledxcalc.domain.StructureMounting
import com.eliezercruz.ledxcalc.util.formatDouble

fun buildPdfTextSummary(data: PdfExportData): String = buildString {
    appendLine("BOCETO DE PANTALLA LED — LedxCalc")
    if (data.inputWidthMeters != null && data.inputHeightMeters != null) {
        appendLine(
            "Tamaño: ${formatDouble(data.inputWidthMeters, 2)} × ${formatDouble(data.inputHeightMeters, 2)} metros"
        )
    } else {
        appendLine("Tamaño: ${data.displayWidth} × ${data.displayHeight} ${data.unitLabel}")
    }
    appendLine()
    appendLine("── ESPECIFICACIONES TÉCNICAS ──")
    appendLine("Módulo: ${data.moduleSpec.title}")
    appendLine("Columnas: ${data.columns}  |  Filas: ${data.rows}  |  Total: ${data.totalModules}")
    appendLine("Resolución: ${data.widthPixels} × ${data.heightPixels} px")
    appendLine("Cobertura: ${data.displayWidth} × ${data.displayHeight} ${data.unitLabel}")
    appendLine("Líneas de señal: ${data.signalLines} (máx ${data.groupSize}/línea)")
    appendLine("Hueco pantalla: ${data.holeWidthFormatted} × ${data.holeHeightFormatted} ft")
    appendLine()
    appendLine("── ESTRUCTURA ──")
    when (data.structureMounting) {
        StructureMounting.FLOOR_BASES -> {
            appendLine("Montaje: Bases de piso")
            val s = data.supportCalc
            BasesDistribution.baseLines(s).forEach { appendLine(it) }
            BasesDistribution.stairLines(s).forEach { appendLine(it) }
        }
        StructureMounting.TRUSS -> {
            appendLine("Montaje: Truss / colgado")
            appendLine("Truss: ${data.trussWidthFeet} × ${data.trussHeightFeet} ft")
        }
    }
    if (SketchKind.ELECTRICAL in data.selectedSketches) {
        appendLine()
        appendLine("── ELÉCTRICO ──")
        appendLine("Potencia prom: ${data.electrical.totalWattsProm.toInt()} W")
        appendLine("Potencia máx: ${data.electrical.totalWattsMax.toInt()} W")
        appendLine("Amperaje (${data.electrical.selectedVoltage.label}): ${data.electrical.loadResult.amperajeMaxFormatted} A")
    }
    appendLine()
    appendLine("Generado por LedxCalc — Creado por Eliezer Cruz")
}
