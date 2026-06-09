package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BasesDistributionTest {
    @Test
    fun onlyNonZeroBasesListed() {
        val support = SupportCalculation(base4 = 1, base3 = 0, base2 = 2, stair4 = 3, stair3 = 0, stair2 = 0)
        assertEquals(listOf("Bases de piso de 4: 1", "Bases de piso de 2: 2"), BasesDistribution.baseLines(support))
        assertEquals(listOf("Escaleras de 4: 3"), BasesDistribution.stairLines(support))
    }

    @Test
    fun emptyWhenAllZero() {
        val support = SupportCalculation(base4 = 0, base3 = 0, base2 = 0, stair4 = 0, stair3 = 0, stair2 = 0)
        assertTrue(BasesDistribution.baseLines(support).isEmpty())
        assertTrue(BasesDistribution.stairLines(support).isEmpty())
    }
}
