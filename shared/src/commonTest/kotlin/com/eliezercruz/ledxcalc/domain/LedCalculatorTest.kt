package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        assertEquals(13, ModuleCatalog.forCategory(ModulePhysicalCategory.OTHER_FORMATS).size)
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
}
