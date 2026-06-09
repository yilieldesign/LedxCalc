package com.eliezercruz.ledxcalc.domain

import com.eliezercruz.ledxcalc.util.formatDouble

data class PdfElectricalSection(
    val model: String,
    val pitch: Double,
    val environment: CabinetEnvironment,
    val wattsPerCabinetProm: Double,
    val wattsPerCabinetMax: Double,
    val totalWattsProm: Double,
    val totalWattsMax: Double,
    val amperes110Prom: String,
    val amperes110Max: String,
    val amperes220Prom: String,
    val amperes220Max: String,
    val electricalNote: String,
    val selectedVoltage: SupplyVoltage,
    val selectedBreaker: BreakerRating,
    val loadResult: ElectricalLoadResult
)

object PdfElectricalReport {
    fun sectionLines(section: PdfElectricalSection): List<String> {
        val load = section.loadResult
        val env = section.environment.label
        val pitch = if (section.pitch > 0) " · Pitch ${section.pitch}" else ""
        return listOf(
            "── POTENCIA ELÉCTRICA ──",
            "Modelo: ${section.model}$pitch · $env",
            "Por gabinete: ${formatW(section.wattsPerCabinetProm)} W prom / ${formatW(section.wattsPerCabinetMax)} W máx",
            "Total pantalla: ${formatW(section.totalWattsProm)} W prom / ${formatW(section.totalWattsMax)} W máx (pico blanco)",
            "Amperaje 110V: ${section.amperes110Prom} A prom · ${section.amperes110Max} A máx",
            "Amperaje 220V: ${section.amperes220Prom} A prom · ${section.amperes220Max} A máx",
            "",
            "── REGLA DEL 80% (${section.selectedBreaker.label} @ ${section.selectedVoltage.label}) ──",
            "Amperaje máx (${section.selectedVoltage.label}): ${load.amperajeMaxFormatted} A",
            "Límite continuo seguro (80%): ${load.limiteSeguridadFormatted} A",
            "Gabinetes máx. por circuito: ${load.gabinetesPermitidosPorCircuito}",
            if (load.circuitosRecomendados > 1) "Circuitos recomendados: ${load.circuitosRecomendados}" else "",
            load.mensajeAlerta.removePrefix("⚠️ ").removePrefix("✅ "),
            load.mensajeDistribucion,
            if (section.electricalNote.isNotBlank()) "Nota: ${section.electricalNote}" else ""
        ).filter { it.isNotBlank() }
    }

    fun formatText(section: PdfElectricalSection): String =
        sectionLines(section).joinToString("\n")

    private fun formatW(value: Double): String = formatDouble(value, 0)
}
