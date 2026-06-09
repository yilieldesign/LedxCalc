package com.eliezercruz.ledxcalc.domain

import kotlin.math.ceil
import kotlin.math.floor
import com.eliezercruz.ledxcalc.util.formatDouble
import com.eliezercruz.ledxcalc.util.trimTrailingZeros

object LedCalculator {
    fun convertToMeters(unit: MeasurementUnit, value: Double): Double = when (unit) {
        MeasurementUnit.METERS -> value
        MeasurementUnit.FEET -> value * 0.3048
    }

    fun decimalSanitized(input: String): String {
        val normalized = input.replace(',', '.')
        var hasDecimal = false
        val builder = StringBuilder()
        normalized.forEach { char ->
            when {
                char.isDigit() -> builder.append(char)
                char == '.' && !hasDecimal -> {
                    builder.append('.')
                    hasDecimal = true
                }
            }
        }
        return builder.toString()
    }

    fun formatMeters(value: Double): String = formatDouble(value, 2)

    fun formatFeet(value: Double): String = formatDouble(value, 2)

    fun calculateStairsForHeight(modulesVertical: Int): Triple<Int, Int, Int> {
        var remainingVertical = modulesVertical
        var stair4 = remainingVertical / 4
        remainingVertical %= 4
        var stair3 = remainingVertical / 3
        remainingVertical %= 3
        var stair2 = remainingVertical / 2
        remainingVertical %= 2
        if (remainingVertical == 1) stair2++
        return Triple(stair4, stair3, stair2)
    }

    fun calculateSupportStructures(modulesHorizontal: Int, modulesVertical: Int): SupportCalculation {
        var remainingHorizontal = modulesHorizontal
        var base4 = remainingHorizontal / 4
        remainingHorizontal %= 4
        var base3 = remainingHorizontal / 3
        remainingHorizontal %= 3
        var base2 = remainingHorizontal / 2
        remainingHorizontal %= 2
        if (remainingHorizontal == 1) base2++

        val totalBases = base4 + base3 + base2
        val (stair4PerPosition, stair3PerPosition, stair2PerPosition) = calculateStairsForHeight(modulesVertical)
        val numberOfStairPositions = totalBases + 1

        return SupportCalculation(
            base4 = base4,
            base3 = base3,
            base2 = base2,
            stair4 = stair4PerPosition * numberOfStairPositions,
            stair3 = stair3PerPosition * numberOfStairPositions,
            stair2 = stair2PerPosition * numberOfStairPositions
        )
    }

    fun calculateHoleFeet(coveredFeet: Double): Double {
        return if (coveredFeet % 1.0 < 0.6) {
            floor(coveredFeet) - 0.5
        } else {
            floor(coveredFeet)
        }
    }

    fun formatHoleFeet(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            trimTrailingZeros(value, 1)
        }
    }

    fun calculate(
        moduleSpec: ModuleSpec,
        widthMeters: Double?,
        heightMeters: Double?,
        inputUnit: MeasurementUnit,
        environment: CabinetEnvironment = CabinetEnvironment.INDOOR
    ): LedCalculationResult? {
        val modulesAcross = widthMeters?.let { ceil(it / moduleSpec.widthMeters).toInt() } ?: return null
        val modulesHigh = heightMeters?.let { ceil(it / moduleSpec.heightMeters).toInt() } ?: return null
        if (modulesAcross <= 0 || modulesHigh <= 0) return null

        val totalModules = modulesAcross * modulesHigh
        val widthPixels = moduleSpec.widthPx * modulesAcross
        val heightPixels = moduleSpec.heightPx * modulesHigh
        val coveredWidthMeters = modulesAcross * moduleSpec.widthMeters
        val coveredHeightMeters = modulesHigh * moduleSpec.heightMeters
        val groupSizeForSpec = moduleSpec.modulesPerSignalLine
        val maxColumnsPerLine = (groupSizeForSpec / modulesHigh).coerceAtLeast(1)
        val signalLinesNeeded = ceil(modulesAcross.toFloat() / maxColumnsPerLine).toInt()

        val electrical = ElectricalCatalog.forModule(moduleSpec, environment)

        val totalWattsProm = totalModules * electrical.wattsPromedio
        val totalWattsMax = totalModules * electrical.wattsMax
        val amps110Prom = totalModules * electrical.amps110vPromedio
        val amps220Prom = totalModules * electrical.amps220vPromedio
        val amps110Max = totalModules * electrical.amps110vMax
        val amps220Max = totalModules * electrical.amps220vMax

        val amperes110V = formatDouble(amps110Prom, 2)
        val amperes220V = formatDouble(amps220Prom, 2)
        val amperes110VMax = formatDouble(amps110Max, 2)
        val amperes220VMax = formatDouble(amps220Max, 2)

        val (displayWidth, displayHeight, displayUnitLabel) = if (inputUnit == MeasurementUnit.METERS) {
            Triple(
                formatFeet(coveredWidthMeters * 3.28084),
                formatFeet(coveredHeightMeters * 3.28084),
                "ft"
            )
        } else {
            Triple(
                formatMeters(coveredWidthMeters),
                formatMeters(coveredHeightMeters),
                "m"
            )
        }

        val screenWidthFeet = coveredWidthMeters * 3.28084
        val screenHeightFeet = coveredHeightMeters * 3.28084
        val holeWidthFeet = calculateHoleFeet(screenWidthFeet)
        val holeHeightFeet = calculateHoleFeet(screenHeightFeet)

        val supportCalc = calculateSupportStructures(modulesAcross, modulesHigh)

        return LedCalculationResult(
            modulesAcross = modulesAcross,
            modulesHigh = modulesHigh,
            totalModules = totalModules,
            widthPixels = widthPixels,
            heightPixels = heightPixels,
            coveredWidthMeters = coveredWidthMeters,
            coveredHeightMeters = coveredHeightMeters,
            signalLinesNeeded = signalLinesNeeded,
            groupSizeForSpec = groupSizeForSpec,
            amperes110V = amperes110V,
            amperes220V = amperes220V,
            amperes110VMax = amperes110VMax,
            amperes220VMax = amperes220VMax,
            totalWatts = totalWattsProm,
            totalWattsMax = totalWattsMax,
            wattsPerModule = electrical.wattsPromedio,
            wattsPerModuleMax = electrical.wattsMax,
            electricalNote = electrical.nota,
            environment = environment,
            displayWidth = displayWidth,
            displayHeight = displayHeight,
            displayUnitLabel = displayUnitLabel,
            holeWidthFeet = holeWidthFeet,
            holeHeightFeet = holeHeightFeet,
            holeWidthFormatted = formatHoleFeet(holeWidthFeet),
            holeHeightFormatted = formatHoleFeet(holeHeightFeet),
            supportCalc = supportCalc,
            trussWidthFeet = ceil(screenWidthFeet).toInt() + 2,
            trussHeightFeet = ceil(screenHeightFeet).toInt() + 1,
            screenWidthFeet = screenWidthFeet,
            screenHeightFeet = screenHeightFeet
        )
    }

    fun toPdfExportData(
        moduleSpec: ModuleSpec,
        result: LedCalculationResult,
        widthMeters: Double?,
        heightMeters: Double?,
        voltage: SupplyVoltage = SupplyVoltage.V110,
        breaker: BreakerRating = BreakerRating.A20,
        sketchSelection: SketchSelection = SketchSelection.initial()
    ): PdfExportData {
        val load = ElectricalLoadCalculator.calcularCargaElectrica(
            wattsMaxPorGabinete = result.wattsPerModuleMax,
            wattsPromedioPorGabinete = result.wattsPerModule,
            cantidad = result.totalModules,
            voltaje = voltage,
            amperajeBreaker = breaker
        )
        val electrical = PdfElectricalSection(
            model = moduleSpec.model.ifBlank { moduleSpec.title },
            pitch = moduleSpec.pitch,
            environment = result.environment,
            wattsPerCabinetProm = result.wattsPerModule,
            wattsPerCabinetMax = result.wattsPerModuleMax,
            totalWattsProm = result.totalWatts ?: 0.0,
            totalWattsMax = result.totalWattsMax ?: 0.0,
            amperes110Prom = result.amperes110V,
            amperes110Max = result.amperes110VMax,
            amperes220Prom = result.amperes220V,
            amperes220Max = result.amperes220VMax,
            electricalNote = result.electricalNote,
            selectedVoltage = voltage,
            selectedBreaker = breaker,
            loadResult = load
        )
        return PdfExportData(
            moduleSpec = moduleSpec,
            columns = result.modulesAcross,
            rows = result.modulesHigh,
            totalModules = result.totalModules,
            widthPixels = result.widthPixels,
            heightPixels = result.heightPixels,
            displayWidth = result.displayWidth,
            displayHeight = result.displayHeight,
            unitLabel = result.displayUnitLabel,
            groupSize = result.groupSizeForSpec,
            signalLines = result.signalLinesNeeded,
            inputWidthMeters = widthMeters,
            inputHeightMeters = heightMeters,
            trussWidthFeet = result.trussWidthFeet,
            trussHeightFeet = result.trussHeightFeet,
            holeWidthFeet = result.holeWidthFeet,
            holeHeightFeet = result.holeHeightFeet,
            electrical = electrical,
            structureMounting = sketchSelection.mounting,
            selectedSketches = sketchSelection.sketchKindsForPdf,
            supportCalc = result.supportCalc,
            screenWidthFeet = result.screenWidthFeet,
            screenHeightFeet = result.screenHeightFeet,
            holeWidthFormatted = result.holeWidthFormatted,
            holeHeightFormatted = result.holeHeightFormatted
        )
    }
}
