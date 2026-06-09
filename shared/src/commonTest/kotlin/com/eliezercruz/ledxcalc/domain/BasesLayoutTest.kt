package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class BasesLayoutTest {
    @Test
    fun sixByFourLayout() {
        val support = LedCalculator.calculateSupportStructures(6, 4)
        val layout = BasesLayout.compute(6, 4, support)!!
        assertEquals(listOf(4, 2), layout.baseUnits.map { it.span })
        assertEquals(listOf(0, 4), layout.baseUnits.map { it.startCol })
        assertEquals(listOf(0, 4, 6), layout.stairColumns)
        assertEquals(listOf(4), layout.stairBlocks)
    }

    @Test
    fun stairPositionsMatchBaseCount() {
        val support = LedCalculator.calculateSupportStructures(10, 3)
        val layout = BasesLayout.compute(10, 3, support)!!
        assertEquals(support.base4 + support.base3 + support.base2 + 1, layout.stairColumns.size)
    }
}
