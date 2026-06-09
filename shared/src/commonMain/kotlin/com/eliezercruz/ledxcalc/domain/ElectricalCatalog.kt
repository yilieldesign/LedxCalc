package com.eliezercruz.ledxcalc.domain

import com.eliezercruz.ledxcalc.util.trimTrailingZeros
import kotlin.math.roundToInt

enum class CabinetEnvironment(val label: String) {
    INDOOR("Indoor"),
    OUTDOOR("Outdoor")
}

data class ElectricalSpec(
    val wattsPromedio: Double,
    val wattsMax: Double,
    val amps110vPromedio: Double,
    val amps220vPromedio: Double,
    val amps110vMax: Double,
    val amps220vMax: Double,
    val nota: String = ""
) {
    fun scaled(factor: Double, notaOverride: String = nota): ElectricalSpec = copy(
        wattsPromedio = wattsPromedio * factor,
        wattsMax = wattsMax * factor,
        amps110vPromedio = amps110vPromedio * factor,
        amps220vPromedio = amps220vPromedio * factor,
        amps110vMax = amps110vMax * factor,
        amps220vMax = amps220vMax * factor,
        nota = notaOverride
    )

    companion object {
        fun fromWatts(wattsPromedio: Double, wattsMax: Double, nota: String = ""): ElectricalSpec =
            ElectricalSpec(
                wattsPromedio = wattsPromedio,
                wattsMax = wattsMax,
                amps110vPromedio = wattsPromedio / 110.0,
                amps220vPromedio = wattsPromedio / 220.0,
                amps110vMax = wattsMax / 110.0,
                amps220vMax = wattsMax / 220.0,
                nota = nota
            )
    }
}

object ElectricalCatalog {
    private const val MIN_PITCH_FACTOR = 0.55
    private const val MAX_PITCH_FACTOR = 2.5

    val indoor500x500 = ElectricalSpec(
        wattsPromedio = 120.0,
        wattsMax = 300.0,
        amps110vPromedio = 1.09,
        amps220vPromedio = 0.54,
        amps110vMax = 2.72,
        amps220vMax = 1.36,
        nota = "Referencia P3.91 · consumo promedio 120–150 W por gabinete."
    )

    val indoor500x1000 = ElectricalSpec(
        wattsPromedio = 240.0,
        wattsMax = 600.0,
        amps110vPromedio = 2.18,
        amps220vPromedio = 1.09,
        amps110vMax = 5.45,
        amps220vMax = 2.72,
        nota = "Referencia P3.91 · el doble de área respecto al 500×500."
    )

    val outdoor500x500 = ElectricalSpec(
        wattsPromedio = 180.0,
        wattsMax = 450.0,
        amps110vPromedio = 1.63,
        amps220vPromedio = 0.81,
        amps110vMax = 4.09,
        amps220vMax = 2.05,
        nota = "Referencia P3.91 · alto brillo (> 5000 nits)."
    )

    val outdoor500x1000 = ElectricalSpec(
        wattsPromedio = 360.0,
        wattsMax = 900.0,
        amps110vPromedio = 3.27,
        amps220vPromedio = 1.63,
        amps110vMax = 8.18,
        amps220vMax = 4.09,
        nota = "Referencia P3.91 · alto brillo (> 5000 nits)."
    )

    fun baseForCabinet(widthMm: Int, heightMm: Double, environment: CabinetEnvironment): ElectricalSpec {
        val is500x500 = widthMm == 500 && heightMm == 500.0
        val is500x1000 = widthMm == 500 && heightMm == 1000.0

        if (is500x500) {
            return if (environment == CabinetEnvironment.OUTDOOR) outdoor500x500 else indoor500x500
        }
        if (is500x1000) {
            return if (environment == CabinetEnvironment.OUTDOOR) outdoor500x1000 else indoor500x1000
        }

        val moduleAreaM2 = (widthMm / 1000.0) * (heightMm / 1000.0)
        val baseAreaM2 = 0.5 * 0.5
        val factor = moduleAreaM2 / baseAreaM2
        val base = if (environment == CabinetEnvironment.OUTDOOR) outdoor500x500 else indoor500x500
        return base.scaled(
            factor = factor,
            notaOverride = "Estimado proporcional al área (${widthMm}×${heightMm} mm)."
        )
    }

    fun referencePitch(widthMm: Int, heightMm: Double): Double = when {
        widthMm == 500 && heightMm == 500.0 -> 3.91
        widthMm == 500 && heightMm == 1000.0 -> 3.91
        widthMm == 960 && heightMm == 960.0 -> 4.0
        widthMm == 640 && heightMm == 640.0 -> 2.0
        widthMm == 600 && heightMm == 337.5 -> 1.56
        else -> 3.91
    }

    fun pitchFactor(pitch: Double, widthMm: Int, heightMm: Double): Double {
        if (pitch <= 0.0) return 1.0
        val refPitch = referencePitch(widthMm, heightMm)
        return (refPitch / pitch).coerceIn(MIN_PITCH_FACTOR, MAX_PITCH_FACTOR)
    }

    fun forModel(
        model: String,
        pitch: Double,
        widthMm: Int,
        heightMm: Double,
        environment: CabinetEnvironment
    ): ElectricalSpec {
        val base = baseForCabinet(widthMm, heightMm, environment)
        val factor = pitchFactor(pitch, widthMm, heightMm)
        val spec = base.scaled(factor)
        val pitchLabel = if (pitch > 0.0) "P${formatPitch(pitch)}" else model
        return spec.copy(
            nota = "$model ($pitchLabel) · ${formatWatts(spec.wattsPromedio)} W prom / " +
                "${formatWatts(spec.wattsMax)} W máx por gabinete · ${environment.label}."
        )
    }

    fun forModule(moduleSpec: ModuleSpec, environment: CabinetEnvironment): ElectricalSpec {
        if (moduleSpec.isCustom && moduleSpec.wattsPerModule > 0) {
            val wProm = moduleSpec.wattsPerModule
            val wMax = moduleSpec.wattsPerModuleMax.takeIf { it > 0 } ?: (wProm * 2.5)
            return ElectricalSpec.fromWatts(
                wattsPromedio = wProm,
                wattsMax = wMax,
                nota = "Valores ingresados manualmente."
            )
        }
        return forModel(
            model = moduleSpec.model.ifBlank { moduleSpec.title },
            pitch = moduleSpec.pitch,
            widthMm = moduleSpec.widthMm,
            heightMm = moduleSpec.heightMm,
            environment = environment
        )
    }

    /** @deprecated Use [forModule] for model-specific values. */
    fun forCabinet(widthMm: Int, heightMm: Double, environment: CabinetEnvironment): ElectricalSpec =
        baseForCabinet(widthMm, heightMm, environment)

    fun supportsEnvironmentToggle(widthMm: Int, heightMm: Double): Boolean =
        widthMm == 500 && (heightMm == 500.0 || heightMm == 1000.0)

    private fun formatWatts(value: Double): String = value.roundToInt().toString()

    private fun formatPitch(pitch: Double): String =
        if (pitch % 1.0 == 0.0) pitch.toInt().toString()
        else trimTrailingZeros(pitch, 2)
}
