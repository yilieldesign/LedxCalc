package com.eliezercruz.ledxcalc.domain

import com.eliezercruz.ledxcalc.util.formatDouble
import kotlinx.serialization.Serializable

@Serializable
data class ModuleSpec(
    val id: String = "",
    val title: String,
    val widthPx: Int,
    val heightPx: Int,
    val widthMeters: Double,
    val heightMeters: Double,
    val modulesPerSignalLine: Int = 36,
    val wattsPerModule: Double = 0.0,
    val wattsPerModuleMax: Double = 0.0,
    val wattsOutdoorProm: Double = 0.0,
    val wattsOutdoorMax: Double = 0.0,
    val isCustom: Boolean = false,
    val model: String = "",
    val pitch: Double = 0.0,
    val totalPixels: Int = widthPx * heightPx,
    val widthMm: Int = (widthMeters * 1000).toInt(),
    val heightMm: Double = heightMeters * 1000
) {
    val resolutionLabel: String get() = "${widthPx} × ${heightPx} px"

    val dropdownLabel: String
        get() = if (model.isNotEmpty()) {
            "$model — ${widthPx}×${heightPx} px"
        } else {
            "${widthPx} × ${heightPx} px"
        }

    val physicalLabel: String
        get() = if (heightMm % 1.0 == 0.0) {
            "${widthMm} × ${heightMm.toInt()} mm"
        } else {
            "${widthMm} × ${formatDouble(heightMm, 1)} mm"
        }
}

@Serializable
data class SupportCalculation(
    val base4: Int,
    val base3: Int,
    val base2: Int,
    val stair4: Int,
    val stair3: Int,
    val stair2: Int
)

@Serializable
data class CalculationHistoryEntry(
    val id: String,
    val timestampMillis: Long,
    val widthInput: String,
    val heightInput: String,
    val unit: MeasurementUnit,
    val moduleTitle: String,
    val totalModules: Int,
    val resolution: String
)

@Serializable
enum class MeasurementUnit {
    METERS,
    FEET;

    val shortLabel: String
        get() = when (this) {
            METERS -> "m"
            FEET -> "ft"
        }

    val displayName: String
        get() = when (this) {
            METERS -> "Metros"
            FEET -> "Pies"
        }
}

enum class ResultTab {
    STANDARD_128,
    ALT_128x256,
    ALT_168x336,
    CUSTOM
}

enum class ViewTab {
    DATA,
    SKETCH
}

data class LedCalculationResult(
    val modulesAcross: Int,
    val modulesHigh: Int,
    /** Filas totales para bases/escaleras (LED + fantasma). */
    val structureModulesHigh: Int,
    /** Módulos vacíos bajo la pantalla LED (solo estructura). */
    val ghostModules: Int,
    val totalModules: Int,
    val widthPixels: Int,
    val heightPixels: Int,
    val coveredWidthMeters: Double,
    val coveredHeightMeters: Double,
    val signalLinesNeeded: Int,
    val groupSizeForSpec: Int,
    val amperes110V: String,
    val amperes220V: String,
    val amperes110VMax: String,
    val amperes220VMax: String,
    val totalWatts: Double?,
    val totalWattsMax: Double?,
    val wattsPerModule: Double,
    val wattsPerModuleMax: Double,
    val electricalNote: String,
    val environment: CabinetEnvironment,
    val displayWidth: String,
    val displayHeight: String,
    val displayUnitLabel: String,
    val holeWidthFeet: Double,
    val holeHeightFeet: Double,
    val holeWidthFormatted: String,
    val holeHeightFormatted: String,
    val supportCalc: SupportCalculation,
    val trussWidthFeet: Int,
    val trussHeightFeet: Int,
    val screenWidthFeet: Double,
    val screenHeightFeet: Double,
    /** Altura física total de montaje incluyendo módulos fantasma. */
    val structureHeightMeters: Double
)

data class PdfExportData(
    val moduleSpec: ModuleSpec,
    val columns: Int,
    val rows: Int,
    val totalModules: Int,
    val widthPixels: Int,
    val heightPixels: Int,
    val displayWidth: String,
    val displayHeight: String,
    val unitLabel: String,
    val groupSize: Int,
    val signalLines: Int,
    val inputWidthMeters: Double?,
    val inputHeightMeters: Double?,
    val trussWidthFeet: Int,
    val trussHeightFeet: Int,
    val holeWidthFeet: Double,
    val holeHeightFeet: Double,
    val electrical: PdfElectricalSection,
    val structureMounting: StructureMounting,
    val selectedSketches: Set<SketchKind>,
    val supportCalc: SupportCalculation,
    val screenWidthFeet: Double,
    val screenHeightFeet: Double,
    val holeWidthFormatted: String,
    val holeHeightFormatted: String,
    val ghostModules: Int = 0,
    val structureModulesHigh: Int = rows
)
