package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.roundToInt

class LedCalculatorTest {
    @Test
    fun supportStructuresForSixByFour() {
        val result = LedCalculator.calculateSupportStructures(6, 4)
        assertEquals(1, result.base4)
        assertEquals(0, result.base3)
        assertEquals(1, result.base2)
    }

    @Test
    fun catalogHasExpectedCounts() {
        assertEquals(13, ModuleCatalog.forCategory(ModulePhysicalCategory.SIZE_500x500).size)
        assertEquals(8, ModuleCatalog.forCategory(ModulePhysicalCategory.SIZE_500x1000).size)
        assertEquals(8, ModuleCatalog.forCategory(ModulePhysicalCategory.SIZE_1000x1000).size)
        assertEquals(13, ModuleCatalog.forCategory(ModulePhysicalCategory.OTHER_FORMATS).size)
    }

    @Test
    fun outdoor1000x1000ElectricalValues() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_1000x1000, "P3.91")!!
        assertEquals(256, spec.widthPx)
        assertEquals(256, spec.heightPx)
        assertEquals(1000, spec.widthMm)
        assertEquals(1000.0, spec.heightMm)

        val indoor = ElectricalCatalog.forModel("P3.91", 3.91, 1000, 1000.0, CabinetEnvironment.INDOOR)
        assertEquals(213, indoor.wattsPromedio.roundToInt())
        assertEquals(533, indoor.wattsMax.roundToInt())

        val outdoor = ElectricalCatalog.forModel("P3.91", 3.91, 1000, 1000.0, CabinetEnvironment.OUTDOOR)
        assertEquals(320.0, outdoor.wattsPromedio)
        assertEquals(800.0, outdoor.wattsMax)
        assertEquals(7.27, outdoor.amps110vMax, 0.01)
        assertEquals(3.64, outdoor.amps220vMax, 0.01)
    }

    @Test
    fun indoor500x500ElectricalValues() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P3.91")!!
        assertEquals(120.0, spec.wattsPerModule)
        assertEquals(300.0, spec.wattsPerModuleMax)

        val result = LedCalculator.calculate(spec, 2.0, 2.0, MeasurementUnit.METERS, CabinetEnvironment.INDOOR)
        requireNotNull(result)
        assertEquals(16, result.totalModules)
        assertEquals(16 * 120.0, result.totalWatts)
        assertEquals(16 * 300.0, result.totalWattsMax)
        assertEquals(String.format(java.util.Locale.US, "%.2f", 16 * 1.09), result.amperes110V)
        assertEquals(String.format(java.util.Locale.US, "%.2f", 16 * 2.72), result.amperes110VMax)
    }

    @Test
    fun finerPitchDrawsMorePowerThanCoarsePitch() {
        val fine = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P1.56")!!
        val coarse = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P4.81")!!
        assertTrue(fine.wattsPerModule > coarse.wattsPerModule)
        assertTrue(fine.wattsPerModuleMax > coarse.wattsPerModuleMax)
    }

    @Test
    fun modelSpecificElectricalNoteIncludesModelName() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P2.5")!!
        val result = LedCalculator.calculate(spec, 2.0, 2.0, MeasurementUnit.METERS, CabinetEnvironment.INDOOR)
        requireNotNull(result)
        assertTrue(result.electricalNote.contains("P2.5"))
    }

    @Test
    fun outdoor500x1000ElectricalValues() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x1000, "P3.91")!!
        val result = LedCalculator.calculate(spec, 2.0, 2.0, MeasurementUnit.METERS, CabinetEnvironment.OUTDOOR)
        requireNotNull(result)
        assertEquals(8, result.totalModules)
        assertEquals(8 * 360.0, result.totalWatts)
        assertEquals(8 * 900.0, result.totalWattsMax)
    }

    @Test
    fun calculateResolutionP391_500x500_twoMeters() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P3.91")!!
        val result = LedCalculator.calculate(spec, 2.0, 2.0, MeasurementUnit.METERS)
        requireNotNull(result)
        assertEquals(4, result.modulesAcross)
        assertEquals(4, result.modulesHigh)
        assertEquals(512, result.widthPixels)
        assertEquals(512, result.heightPixels)
    }

    @Test
    fun calculateResolutionP297_500x1000_threeByFourMeters() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x1000, "P2.97")!!
        val result = LedCalculator.calculate(spec, 3.0, 4.0, MeasurementUnit.METERS)
        requireNotNull(result)
        assertEquals(6, result.modulesAcross)
        assertEquals(4, result.modulesHigh)
        assertEquals(1008, result.widthPixels)
        assertEquals(1344, result.heightPixels)
    }

    @Test
    fun modules500x1000_fiveByTwoPointFiveMeters_usesAncho500Alto1000() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x1000, "P3.91")!!
        assertEquals(500, spec.widthMm)
        assertEquals(1000.0, spec.heightMm)
        assertEquals(128, spec.widthPx)
        assertEquals(256, spec.heightPx)

        val result = LedCalculator.calculate(spec, 5.0, 2.5, MeasurementUnit.METERS)
        requireNotNull(result)
        // Ancho pantalla 5 m ÷ 500 mm = 10 columnas (no 5)
        assertEquals(10, result.modulesAcross)
        // Alto pantalla 2,5 m ÷ 1000 mm = 2 filas (medio gabinete exacto, no 3)
        assertEquals(2, result.modulesHigh)
        assertEquals(20, result.totalModules)
        assertEquals(1280, result.widthPixels)
        assertEquals(512, result.heightPixels)
        assertEquals(5.0, result.coveredWidthMeters)
        assertEquals(2.0, result.coveredHeightMeters)
    }

    @Test
    fun modules500x1000_twoPointFiveSquare_fiveColumnsTwoRows() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x1000, "P3.91")!!
        val result = LedCalculator.calculate(spec, 2.5, 2.5, MeasurementUnit.METERS)
        requireNotNull(result)
        assertEquals(5, result.modulesAcross)
        assertEquals(2, result.modulesHigh)
    }

    @Test
    fun modulesForDimension_stillCeilsWhenMoreThanHalfModuleNeeded() {
        assertEquals(3, LedCalculator.modulesForDimension(2.6, 1000.0))
        assertEquals(11, LedCalculator.modulesForDimension(5.1, 500.0))
        assertEquals(10, LedCalculator.modulesForDimension(5.0, 500.0))
        assertEquals(2, LedCalculator.modulesForDimension(2.5, 1000.0))
    }

    @Test
    fun ghostModulesAffectStructureNotLedCount() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P3.91")!!
        val withoutGhost = LedCalculator.calculate(spec, 5.0, 2.0, MeasurementUnit.METERS, ghostModules = 0)
        val withGhost = LedCalculator.calculate(spec, 5.0, 2.0, MeasurementUnit.METERS, ghostModules = 1)
        requireNotNull(withoutGhost)
        requireNotNull(withGhost)

        assertEquals(withoutGhost.modulesAcross, withGhost.modulesAcross)
        assertEquals(withoutGhost.modulesHigh, withGhost.modulesHigh)
        assertEquals(withoutGhost.totalModules, withGhost.totalModules)
        assertEquals(1, withGhost.ghostModules)
        assertEquals(withoutGhost.modulesHigh + 1, withGhost.structureModulesHigh)

        assertEquals("16.40", withGhost.displayWidth)
        assertEquals("8.20", withGhost.displayHeight)

        assertTrue(withGhost.supportCalc.stair4 >= withoutGhost.supportCalc.stair4 ||
            withGhost.supportCalc.stair3 > withoutGhost.supportCalc.stair3 ||
            withGhost.supportCalc.stair2 > withoutGhost.supportCalc.stair2)
    }
}
