package com.eliezercruz.ledxcalc.domain

import com.eliezercruz.ledxcalc.util.formatDouble
import kotlin.math.ceil
import kotlin.math.floor

enum class SupplyVoltage(val volts: Int, val label: String) {
    V110(110, "110V"),
    V220(220, "220V")
}

enum class BreakerRating(val amps: Int, val label: String) {
    A15(15, "15A"),
    A20(20, "20A"),
    A30(30, "30A"),
    A32(32, "32A"),
    A40(40, "40A"),
    A50(50, "50A"),
    A60(60, "60A");

    companion object {
        val commonOptions = listOf(A20, A30, A32, A40, A50, A60)
    }
}

data class ElectricalLoadResult(
    val wattsTotalesMax: Double,
    val wattsTotalesPromedio: Double,
    val amperajeMax: Double,
    val amperajePromedio: Double,
    val limiteSeguridadBreakerA: Double,
    val sobrepasaLimite: Boolean,
    val gabinetesPermitidosPorCircuito: Int,
    val circuitosRecomendados: Int,
    val mensajeAlerta: String,
    val mensajeDistribucion: String
) {
    val amperajeMaxFormatted: String get() = formatA(amperajeMax)
    val amperajePromedioFormatted: String get() = formatA(amperajePromedio)
    val limiteSeguridadFormatted: String get() = formatA(limiteSeguridadBreakerA)

    private fun formatA(value: Double): String = formatDouble(value, 2)
}

object ElectricalLoadCalculator {
    /** Margen de seguridad continuo: 80 % de la capacidad nominal del breaker. */
    const val SAFETY_MARGIN = 0.8

    fun calcularCargaElectrica(
        wattsMaxPorGabinete: Double,
        wattsPromedioPorGabinete: Double,
        cantidad: Int,
        voltaje: SupplyVoltage,
        amperajeBreaker: BreakerRating
    ): ElectricalLoadResult = calcularCargaElectrica(
        wattsMaxPorGabinete = wattsMaxPorGabinete,
        wattsPromedioPorGabinete = wattsPromedioPorGabinete,
        cantidad = cantidad,
        voltaje = voltaje.volts,
        amperajeBreaker = amperajeBreaker.amps
    )

    fun calcularCargaElectrica(
        wattsMaxPorGabinete: Double,
        wattsPromedioPorGabinete: Double,
        cantidad: Int,
        voltaje: Int,
        amperajeBreaker: Int
    ): ElectricalLoadResult {
        require(cantidad >= 0) { "cantidad must be non-negative" }
        require(voltaje > 0) { "voltaje must be positive" }
        require(amperajeBreaker > 0) { "amperajeBreaker must be positive" }

        val wattsTotalesMax = wattsMaxPorGabinete * cantidad
        val wattsTotalesPromedio = wattsPromedioPorGabinete * cantidad

        val amperajeMax = wattsTotalesMax / voltaje
        val amperajePromedio = wattsTotalesPromedio / voltaje

        val limiteSeguridadBreakerA = amperajeBreaker * SAFETY_MARGIN
        val sobrepasaLimite = amperajeMax > limiteSeguridadBreakerA

        val ampsPorGabineteMax = wattsMaxPorGabinete / voltaje
        val gabinetesPermitidosPorCircuito = if (ampsPorGabineteMax > 0.0) {
            floor(limiteSeguridadBreakerA / ampsPorGabineteMax).toInt().coerceAtLeast(0)
        } else {
            0
        }

        val circuitosRecomendados = if (gabinetesPermitidosPorCircuito > 0 && cantidad > 0) {
            ceil(cantidad.toDouble() / gabinetesPermitidosPorCircuito).toInt()
        } else {
            0
        }

        val mensajeAlerta = if (sobrepasaLimite) {
            "⚠️ ¡PELIGRO DE SOBRECARGA! Este circuito demanda ${formatDouble(amperajeMax, 2)} A " +
                "en pico blanco. El límite continuo seguro para un breaker de ${amperajeBreaker}A " +
                "es de ${formatDouble(limiteSeguridadBreakerA, 2)} A (regla del 80%)."
        } else {
            "✅ Carga eléctrica segura dentro del límite continuo (80%)."
        }

        val mensajeDistribucion = when {
            gabinetesPermitidosPorCircuito <= 0 ->
                "No es posible calcular la distribución con estos parámetros."
            cantidad <= gabinetesPermitidosPorCircuito ->
                "Un solo breaker de ${amperajeBreaker}A puede alimentar los $cantidad gabinetes " +
                    "(máx. $gabinetesPermitidosPorCircuito por circuito al 80%)."
            else ->
                "Divide la pantalla en $circuitosRecomendados circuito(s) de máximo " +
                    "$gabinetesPermitidosPorCircuito gabinete(s) por cada breaker de ${amperajeBreaker}A."
        }

        return ElectricalLoadResult(
            wattsTotalesMax = wattsTotalesMax,
            wattsTotalesPromedio = wattsTotalesPromedio,
            amperajeMax = amperajeMax,
            amperajePromedio = amperajePromedio,
            limiteSeguridadBreakerA = limiteSeguridadBreakerA,
            sobrepasaLimite = sobrepasaLimite,
            gabinetesPermitidosPorCircuito = gabinetesPermitidosPorCircuito,
            circuitosRecomendados = circuitosRecomendados,
            mensajeAlerta = mensajeAlerta,
            mensajeDistribucion = mensajeDistribucion
        )
    }
}
