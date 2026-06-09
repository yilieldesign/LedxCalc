package com.eliezercruz.ledxcalc.domain

/**
 * Distribución horizontal de bases y posiciones de escaleras (vista frontal).
 */
object BasesLayout {
    data class BaseUnit(val span: Int, val startCol: Int)

    data class Layout(
        val baseUnits: List<BaseUnit>,
        /** Columnas donde van escaleras: borde izquierdo, entre bases y borde derecho. */
        val stairColumns: List<Int>,
        /** Bloques verticales por escalera (4, 3 o 2 módulos de alto). */
        val stairBlocks: List<Int>,
        val stair4PerPosition: Int,
        val stair3PerPosition: Int,
        val stair2PerPosition: Int
    )

    fun compute(modulesAcross: Int, modulesHigh: Int, support: SupportCalculation): Layout? {
        if (modulesAcross <= 0 || modulesHigh <= 0) return null

        val baseUnits = buildList {
            var col = 0
            repeat(support.base4) {
                add(BaseUnit(span = 4, startCol = col))
                col += 4
            }
            repeat(support.base3) {
                add(BaseUnit(span = 3, startCol = col))
                col += 3
            }
            repeat(support.base2) {
                add(BaseUnit(span = 2, startCol = col))
                col += 2
            }
        }

        val (stair4, stair3, stair2) = LedCalculator.calculateStairsForHeight(modulesHigh)
        val stairBlocks = buildList {
            repeat(stair4) { add(4) }
            repeat(stair3) { add(3) }
            repeat(stair2) { add(2) }
        }

        val stairColumns = buildList {
            add(0)
            var edge = 0
            baseUnits.forEach { unit ->
                edge += unit.span
                add(edge.coerceAtMost(modulesAcross))
            }
        }.distinct()

        return Layout(
            baseUnits = baseUnits,
            stairColumns = stairColumns,
            stairBlocks = stairBlocks,
            stair4PerPosition = stair4,
            stair3PerPosition = stair3,
            stair2PerPosition = stair2
        )
    }
}
