package com.eliezercruz.ledxcalc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElectricalLoadCalculatorTest {
    @Test
    fun safeLoadWithin80Percent() {
        // 5 gabinetes indoor 500x500 (300W max) @ 110V, breaker 20A → 5×300/110 = 13.64A < 16A
        val result = ElectricalLoadCalculator.calcularCargaElectrica(
            wattsMaxPorGabinete = 300.0,
            wattsPromedioPorGabinete = 120.0,
            cantidad = 5,
            voltaje = 110,
            amperajeBreaker = 20
        )
        assertFalse(result.sobrepasaLimite)
        assertEquals(16.0, result.limiteSeguridadBreakerA)
        assertEquals(5, result.gabinetesPermitidosPorCircuito)
        assertTrue(result.mensajeAlerta.contains("segura"))
    }

    @Test
    fun overloadTriggersAlert() {
        // 16 gabinetes × 300W @ 110V = 43.64A > 16A (80% of 20A)
        val result = ElectricalLoadCalculator.calcularCargaElectrica(
            wattsMaxPorGabinete = 300.0,
            wattsPromedioPorGabinete = 120.0,
            cantidad = 16,
            voltaje = 110,
            amperajeBreaker = 20
        )
        assertTrue(result.sobrepasaLimite)
        assertEquals(5, result.gabinetesPermitidosPorCircuito)
        assertEquals(4, result.circuitosRecomendados)
        assertTrue(result.mensajeAlerta.contains("SOBRECARGA"))
    }

    @Test
    fun distributionMessageForMultipleCircuits() {
        val result = ElectricalLoadCalculator.calcularCargaElectrica(
            wattsMaxPorGabinete = 300.0,
            wattsPromedioPorGabinete = 120.0,
            cantidad = 16,
            voltaje = 110,
            amperajeBreaker = 20
        )
        assertTrue(result.mensajeDistribucion.contains("4 circuito"))
        assertTrue(result.mensajeDistribucion.contains("5 gabinete"))
    }

    @Test
    fun higherBreakerAllowsMoreCabinetsPerCircuit() {
        val result20 = ElectricalLoadCalculator.calcularCargaElectrica(300.0, 120.0, 16, 110, 20)
        val result60 = ElectricalLoadCalculator.calcularCargaElectrica(300.0, 120.0, 16, 110, 60)
        assertTrue(result60.gabinetesPermitidosPorCircuito > result20.gabinetesPermitidosPorCircuito)
        assertFalse(result60.sobrepasaLimite)
    }
}
