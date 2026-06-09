package com.eliezercruz.ledxcalc.domain

import com.eliezercruz.ledxcalc.util.formatDouble

data class StructureRecommendation(
    val mounting: StructureMounting,
    val headline: String,
    val primaryLabel: String,
    val primaryValue: String,
    /** Líneas verticales (ej. bases de 4, 3, 2) sin viñeta. */
    val summaryLines: List<String> = emptyList(),
    val detailLines: List<String>
)

object StructureRecommendations {
    fun forFloorBases(result: LedCalculationResult): StructureRecommendation {
        val s = result.supportCalc
        return StructureRecommendation(
            mounting = StructureMounting.FLOOR_BASES,
            headline = "Bases de piso y escaleras",
            primaryLabel = "Distribución de bases",
            primaryValue = "",
            summaryLines = BasesDistribution.baseLines(s),
            detailLines = buildList {
                addAll(BasesDistribution.stairLines(s))
                add("${result.modulesAcross} columnas × ${result.modulesHigh} filas de gabinetes")
            }
        )
    }

    fun forTruss(result: LedCalculationResult): StructureRecommendation =
        StructureRecommendation(
            mounting = StructureMounting.TRUSS,
            headline = "Estructura truss / colgado",
            primaryLabel = "Truss recomendado",
            primaryValue = "${result.trussWidthFeet} × ${result.trussHeightFeet} ft",
            detailLines = listOf(
                "Ancho truss = ancho pantalla + 2 ft (${formatDouble(result.screenWidthFeet, 1)} ft + margen)",
                "Alto truss = alto pantalla + 1 ft (${formatDouble(result.screenHeightFeet, 1)} ft + margen)",
                "Pantalla colgada dentro del marco truss"
            )
        )

    fun forResult(result: LedCalculationResult, mounting: StructureMounting): StructureRecommendation =
        when (mounting) {
            StructureMounting.FLOOR_BASES -> forFloorBases(result)
            StructureMounting.TRUSS -> forTruss(result)
        }
}
