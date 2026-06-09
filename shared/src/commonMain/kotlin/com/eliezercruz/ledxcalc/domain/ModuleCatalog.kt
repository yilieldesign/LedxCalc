package com.eliezercruz.ledxcalc.domain

import com.eliezercruz.ledxcalc.util.formatDouble

enum class ModulePhysicalCategory(val label: String) {
    SIZE_500x500("Gabinetes 500 × 500 mm"),
    SIZE_500x1000("Gabinetes 500 × 1000 mm"),
    SIZE_1000x1000("Gabinetes 1000 × 1000 mm"),
    OTHER_FORMATS("Otros formatos fijos y publicidad");

    fun modules(): List<ModuleSpec> = ModuleCatalog.forCategory(this)
}

object ModuleCatalog {

    /** ~650 kpx por línea de señal (estándar receptor LED). */
    private fun modulesPerSignalLine(totalPixels: Int): Int =
        (650_000 / totalPixels).coerceIn(4, 40)

    private fun cabinet(
        category: ModulePhysicalCategory,
        model: String,
        pitch: Double,
        widthMm: Int,
        heightMm: Double,
        resWidth: Int,
        resHeight: Int,
        totalPixels: Int
    ): ModuleSpec {
        val safeModel = model
            .replace(" ", "_")
            .replace(".", "_")
            .replace("(", "")
            .replace(")", "")
        val id = "${category.name.lowercase()}_${safeModel}_${resWidth}x${resHeight}"
        val wM = widthMm / 1000.0
        val hM = heightMm / 1000.0
        val indoor = ElectricalCatalog.forModel(model, pitch, widthMm, heightMm, CabinetEnvironment.INDOOR)
        val outdoor = if (ElectricalCatalog.supportsEnvironmentToggle(widthMm, heightMm)) {
            ElectricalCatalog.forModel(model, pitch, widthMm, heightMm, CabinetEnvironment.OUTDOOR)
        } else {
            null
        }
        return ModuleSpec(
            id = id,
            model = model,
            pitch = pitch,
            title = "$model — ${resWidth}×${resHeight} px (${physicalMmLabel(widthMm, heightMm)})",
            widthPx = resWidth,
            heightPx = resHeight,
            widthMeters = wM,
            heightMeters = hM,
            widthMm = widthMm,
            heightMm = heightMm,
            totalPixels = totalPixels,
            modulesPerSignalLine = modulesPerSignalLine(totalPixels),
            wattsPerModule = indoor.wattsPromedio,
            wattsPerModuleMax = indoor.wattsMax,
            wattsOutdoorProm = outdoor?.wattsPromedio ?: 0.0,
            wattsOutdoorMax = outdoor?.wattsMax ?: 0.0
        )
    }

    private fun physicalMmLabel(widthMm: Int, heightMm: Double): String {
        return if (heightMm % 1.0 == 0.0) {
            "${widthMm}×${heightMm.toInt()} mm"
        } else {
            "${widthMm}×${formatDouble(heightMm, 1)} mm"
        }
    }

    // ── Gabinetes Cuadrados Estándar 500×500 mm ───────────────────
    private val modules500x500: List<ModuleSpec> = listOf(
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P1.56", 1.56, 500, 500.0, 320, 320, 102400),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P1.66", 1.66, 500, 500.0, 300, 300, 90000),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P1.87", 1.87, 500, 500.0, 268, 268, 71824),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P1.95", 1.95, 500, 500.0, 256, 256, 65536),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P2.0", 2.0, 500, 500.0, 250, 250, 62500),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P2.5", 2.5, 500, 500.0, 200, 200, 40000),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P2.6", 2.6, 500, 500.0, 192, 192, 36864),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P2.84", 2.84, 500, 500.0, 176, 176, 30976),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P2.97", 2.97, 500, 500.0, 168, 168, 28224),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P3.0", 3.0, 500, 500.0, 166, 166, 27556),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P3.91", 3.91, 500, 500.0, 128, 128, 16384),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P4.0", 4.0, 500, 500.0, 125, 125, 15625),
        cabinet(ModulePhysicalCategory.SIZE_500x500, "P4.81", 4.81, 500, 500.0, 104, 104, 10816)
    )

    // ── Gabinetes Rectangulares 500×1000 mm ───────────────────────
    private val modules500x1000: List<ModuleSpec> = listOf(
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P2.97", 2.97, 500, 1000.0, 168, 336, 56448),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P3.0", 3.0, 500, 1000.0, 166, 332, 55112),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P3.91", 3.91, 500, 1000.0, 128, 256, 32768),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P4.0", 4.0, 500, 1000.0, 125, 250, 31250),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P4.81", 4.81, 500, 1000.0, 104, 208, 21632),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P5.95", 5.95, 500, 1000.0, 84, 168, 14112),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P8.0", 8.0, 500, 1000.0, 62, 124, 7688),
        cabinet(ModulePhysicalCategory.SIZE_500x1000, "P10.0", 10.0, 500, 1000.0, 50, 100, 5000)
    )

    // ── Gabinetes Grandes de Exterior 1000×1000 mm (gran formato) ──
    private val modules1000x1000: List<ModuleSpec> = listOf(
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P3.91", 3.91, 1000, 1000.0, 256, 256, 65536),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P4.81", 4.81, 1000, 1000.0, 208, 208, 43264),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P5.0", 5.0, 1000, 1000.0, 200, 200, 40000),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P5.95", 5.95, 1000, 1000.0, 168, 168, 28224),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P6.67", 6.67, 1000, 1000.0, 150, 150, 22500),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P8.0", 8.0, 1000, 1000.0, 125, 125, 15625),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P10.0", 10.0, 1000, 1000.0, 100, 100, 10000),
        cabinet(ModulePhysicalCategory.SIZE_1000x1000, "P16.0", 16.0, 1000, 1000.0, 62, 62, 3844)
    )

    // ── Otros formatos fijos y publicidad ─────────────────────────
    private val otherFormats: List<ModuleSpec> = listOf(
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P4.0 (Fijo)", 4.0, 960, 960.0, 240, 240, 57600),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P5.0 (Fijo)", 5.0, 960, 960.0, 192, 192, 36864),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P6.0 (Fijo)", 6.0, 960, 960.0, 160, 160, 25600),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P8.0 (Fijo)", 8.0, 960, 960.0, 120, 120, 14400),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P10.0 (Fijo)", 10.0, 960, 960.0, 96, 96, 9216),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P2.0 (Fijo Pequeño)", 2.0, 640, 640.0, 320, 320, 102400),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P2.5 (Fijo Pequeño)", 2.5, 640, 640.0, 256, 256, 65536),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P3.07 (Fijo Pequeño)", 3.07, 640, 640.0, 208, 208, 43264),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P4.0 (Fijo Pequeño)", 4.0, 640, 640.0, 160, 160, 25600),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P0.9 (Slim 16:9)", 0.9, 600, 337.5, 640, 360, 230400),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P1.25 (Slim 16:9)", 1.25, 600, 337.5, 480, 270, 129600),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P1.56 (Slim 16:9)", 1.56, 600, 337.5, 384, 216, 82944),
        cabinet(ModulePhysicalCategory.OTHER_FORMATS, "P1.87 (Slim 16:9)", 1.87, 600, 337.5, 320, 180, 57600)
    )

    fun forCategory(category: ModulePhysicalCategory): List<ModuleSpec> = when (category) {
        ModulePhysicalCategory.SIZE_500x500 -> modules500x500
        ModulePhysicalCategory.SIZE_500x1000 -> modules500x1000
        ModulePhysicalCategory.SIZE_1000x1000 -> modules1000x1000
        ModulePhysicalCategory.OTHER_FORMATS -> otherFormats
    }

    fun allPresets(): List<ModuleSpec> = modules500x500 + modules500x1000 + modules1000x1000 + otherFormats

    fun findById(id: String): ModuleSpec? = allPresets().find { it.id == id }

    fun findByModel(category: ModulePhysicalCategory, model: String): ModuleSpec? =
        forCategory(category).find { it.model == model }

    fun defaultModuleId(category: ModulePhysicalCategory): String =
        forCategory(category).first().id
}
