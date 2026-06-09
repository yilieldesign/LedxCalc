package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SketchSelectionTest {
    @Test
    fun floorBasesMountingEnablesBasesNotTruss() {
        val sel = SketchSelection.initial()
        assertTrue(sel.isEnabled(SketchKind.FLOOR_BASES))
        assertFalse(sel.isEnabled(SketchKind.TRUSS))
    }

    @Test
    fun switchingToTrussSwapsStructureSketches() {
        val truss = SketchSelection.initial().withMounting(StructureMounting.TRUSS)
        assertTrue(truss.isEnabled(SketchKind.TRUSS))
        assertFalse(truss.isEnabled(SketchKind.FLOOR_BASES))
    }

    @Test
    fun toggleRemovesSketchFromSelection() {
        val toggled = SketchSelection.initial().toggle(SketchKind.SCREEN_HOLE)
        assertFalse(toggled.isEnabled(SketchKind.SCREEN_HOLE))
    }

    @Test
    fun encodeDecodeRoundTrip() {
        val original = SketchSelection.initial().toggle(SketchKind.FLOOR_BASES)
        val encoded = SketchSelection.encodeEnabled(original.enabled)
        val decoded = SketchSelection.decode(original.mounting.name, encoded)
        assertEquals(original.enabled, decoded.enabled)
    }
}
