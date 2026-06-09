package com.eliezercruz.ledxcalc.domain

enum class StructureMounting(val label: String) {
    FLOOR_BASES("Bases de piso"),
    TRUSS("Truss / colgado")
}

enum class SketchKind(val label: String, val shortLabel: String) {
    MODULE_SIGNAL("Distribución de módulos / señal", "Módulos"),
    SCREEN_HOLE("Hueco de pantalla", "Hueco"),
    FLOOR_BASES("Bases y escaleras", "Bases"),
    TRUSS("Estructura truss", "Truss"),
    ELECTRICAL("Reporte eléctrico (80%)", "Eléctrico");

    fun availableFor(mounting: StructureMounting): Boolean = when (this) {
        FLOOR_BASES -> mounting == StructureMounting.FLOOR_BASES
        TRUSS -> mounting == StructureMounting.TRUSS
        else -> true
    }
}

object SketchDefaults {
    fun forMounting(mounting: StructureMounting): Set<SketchKind> = buildSet {
        add(SketchKind.MODULE_SIGNAL)
        add(SketchKind.SCREEN_HOLE)
        add(SketchKind.ELECTRICAL)
        when (mounting) {
            StructureMounting.FLOOR_BASES -> add(SketchKind.FLOOR_BASES)
            StructureMounting.TRUSS -> add(SketchKind.TRUSS)
        }
    }
}

data class SketchSelection(
    val mounting: StructureMounting,
    val enabled: Set<SketchKind>
) {
    val sketchKindsForPdf: Set<SketchKind> get() = enabled

    fun withMounting(mounting: StructureMounting): SketchSelection {
        if (mounting == this.mounting) return this
        val kept = enabled.filter { it.availableFor(mounting) }.toSet()
        val defaults = SketchDefaults.forMounting(mounting)
        return copy(
            mounting = mounting,
            enabled = (defaults + kept).filter { it.availableFor(mounting) }.toSet()
        )
    }

    fun toggle(kind: SketchKind): SketchSelection {
        if (!kind.availableFor(mounting)) return this
        val next = if (kind in enabled) enabled - kind else enabled + kind
        return copy(enabled = next)
    }

    fun isEnabled(kind: SketchKind): Boolean = kind in enabled

    companion object {
        fun initial(): SketchSelection = SketchSelection(
            mounting = StructureMounting.FLOOR_BASES,
            enabled = SketchDefaults.forMounting(StructureMounting.FLOOR_BASES)
        )

        fun decode(mountingKey: String, enabledKeys: String): SketchSelection {
            val mounting = runCatching { StructureMounting.valueOf(mountingKey) }
                .getOrDefault(StructureMounting.FLOOR_BASES)
            val parsed = enabledKeys.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != "TARIMA" }
                .mapNotNull { runCatching { SketchKind.valueOf(it) }.getOrNull() }
                .filter { it.availableFor(mounting) }
                .toSet()
            return SketchSelection(
                mounting = mounting,
                enabled = parsed.ifEmpty { SketchDefaults.forMounting(mounting) }
            )
        }

        fun encodeEnabled(enabled: Set<SketchKind>): String =
            enabled.joinToString(",") { it.name }
    }
}
