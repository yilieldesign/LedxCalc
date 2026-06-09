package com.eliezercruz.ledxcalc.data

import com.eliezercruz.ledxcalc.domain.CalculationHistoryEntry
import com.eliezercruz.ledxcalc.domain.ModuleSpec
import com.eliezercruz.ledxcalc.util.currentTimeMillis
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppRepository(
    private val settings: Settings = Settings()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun loadSavedModules(): List<ModuleSpec> {
        val raw = settings.getStringOrNull(KEY_SAVED_MODULES) ?: return emptyList()
        return runCatching { json.decodeFromString<List<ModuleSpec>>(raw) }.getOrDefault(emptyList())
    }

    fun saveModule(module: ModuleSpec) {
        val current = loadSavedModules().toMutableList()
        val withId = if (module.id.isBlank()) {
            module.copy(id = "custom_${currentTimeMillis()}", isCustom = true)
        } else module
        current.removeAll { it.id == withId.id }
        current.add(0, withId)
        settings.putString(KEY_SAVED_MODULES, json.encodeToString(current.take(MAX_SAVED_MODULES)))
    }

    fun deleteModule(moduleId: String) {
        val updated = loadSavedModules().filterNot { it.id == moduleId }
        settings.putString(KEY_SAVED_MODULES, json.encodeToString(updated))
    }

    fun loadHistory(): List<CalculationHistoryEntry> {
        val raw = settings.getStringOrNull(KEY_HISTORY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<CalculationHistoryEntry>>(raw) }.getOrDefault(emptyList())
    }

    fun addHistoryEntry(entry: CalculationHistoryEntry) {
        val current = loadHistory().toMutableList()
        current.removeAll { it.id == entry.id }
        current.add(0, entry)
        settings.putString(KEY_HISTORY, json.encodeToString(current.take(MAX_HISTORY)))
    }

    fun clearHistory() {
        settings.remove(KEY_HISTORY)
    }

    fun loadDarkThemeOverride(): Boolean? {
        return if (settings.hasKey(KEY_DARK_THEME)) settings.getBoolean(KEY_DARK_THEME, true) else null
    }

    fun saveDarkThemeOverride(isDark: Boolean) {
        settings.putBoolean(KEY_DARK_THEME, isDark)
    }

    companion object {
        private const val KEY_SAVED_MODULES = "saved_modules"
        private const val KEY_HISTORY = "calculation_history"
        private const val KEY_DARK_THEME = "dark_theme_override"
        private const val MAX_SAVED_MODULES = 20
        private const val MAX_HISTORY = 30
    }
}
