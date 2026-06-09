package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElectricalCatalogTest {
    @Test
    fun referencePitchP391KeepsBaseWatts() {
        val spec = ElectricalCatalog.forModel("P3.91", 3.91, 500, 500.0, CabinetEnvironment.INDOOR)
        assertEquals(120.0, spec.wattsPromedio)
        assertEquals(300.0, spec.wattsMax)
    }

    @Test
    fun finerPitchIncreasesWattsWithinCap() {
        val fine = ElectricalCatalog.forModel("P1.56", 1.56, 500, 500.0, CabinetEnvironment.INDOOR)
        val coarse = ElectricalCatalog.forModel("P4.81", 4.81, 500, 500.0, CabinetEnvironment.INDOOR)
        assertTrue(fine.wattsMax > coarse.wattsMax)
        assertTrue(fine.wattsPromedio > coarse.wattsPromedio)
    }

    @Test
    fun pdfExportIncludesElectricalSection() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P3.91")!!
        val result = LedCalculator.calculate(spec, 2.0, 2.0, MeasurementUnit.METERS, CabinetEnvironment.INDOOR)!!
        val pdf = LedCalculator.toPdfExportData(spec, result, 2.0, 2.0, SupplyVoltage.V110, BreakerRating.A20)
        assertEquals("P3.91", pdf.electrical.model)
        assertTrue(PdfElectricalReport.formatText(pdf.electrical).contains("REGLA DEL 80%"))
        assertTrue(PdfElectricalReport.formatText(pdf.electrical).contains("P3.91"))
    }
}
