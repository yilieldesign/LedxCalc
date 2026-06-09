package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StructureRecommendationsTest {
    @Test
    fun floorBasesRecommendationIncludesDistribution() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P3.91")!!
        val result = LedCalculator.calculate(spec, 3.0, 2.0, MeasurementUnit.METERS)!!
        val rec = StructureRecommendations.forFloorBases(result)
        assertEquals("Distribución de bases", rec.primaryLabel)
        assertTrue(rec.summaryLines.any { it.startsWith("Bases de") })
        assertTrue(rec.detailLines.any { it.contains("columnas") })
    }

    @Test
    fun trussRecommendationIncludesTrussDimensions() {
        val spec = ModuleCatalog.findByModel(ModulePhysicalCategory.SIZE_500x500, "P3.91")!!
        val result = LedCalculator.calculate(spec, 3.0, 2.0, MeasurementUnit.METERS)!!
        val rec = StructureRecommendations.forTruss(result)
        assertEquals("Truss recomendado", rec.primaryLabel)
        assertEquals("${result.trussWidthFeet} × ${result.trussHeightFeet} ft", rec.primaryValue)
    }
}
