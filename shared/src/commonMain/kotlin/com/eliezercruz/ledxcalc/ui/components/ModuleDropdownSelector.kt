package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.CabinetEnvironment
import com.eliezercruz.ledxcalc.domain.ElectricalCatalog
import com.eliezercruz.ledxcalc.domain.ElectricalSpec
import com.eliezercruz.ledxcalc.domain.ModuleCatalog
import com.eliezercruz.ledxcalc.domain.ModulePhysicalCategory
import com.eliezercruz.ledxcalc.domain.ModuleSpec
import com.eliezercruz.ledxcalc.ui.theme.LedColors
import com.eliezercruz.ledxcalc.util.formatDouble
import kotlin.math.roundToInt

@Composable
fun ModuleDropdownSelector(
    selectedCategory: ModulePhysicalCategory,
    selectedModule: ModuleSpec?,
    onCategorySelected: (ModulePhysicalCategory) -> Unit,
    onModuleSelected: (ModuleSpec) -> Unit,
    modifier: Modifier = Modifier,
    environment: CabinetEnvironment = CabinetEnvironment.INDOOR,
    modulesOnSide: Boolean = false
) {
    val categoryModules = remember(selectedCategory) { ModuleCatalog.forCategory(selectedCategory) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var moduleExpanded by remember { mutableStateOf(false) }
    val displayModule = selectedModule?.withModulesOnSide(modulesOnSide)

    LedPanel(modifier = modifier, accentColor = LedColors.NeonCyan) {
        Text(
            text = "Seleccionar módulo",
            style = MaterialTheme.typography.titleMedium,
            color = LedColors.NeonCyan,
            modifier = Modifier.fillMaxWidth()
        )

        DropdownField(
            label = "Tamaño físico del módulo",
            value = selectedCategory.label,
            expanded = categoryExpanded,
            onExpand = { categoryExpanded = true },
            onDismiss = { categoryExpanded = false }
        ) {
            ModulePhysicalCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label) },
                    onClick = {
                        onCategorySelected(category)
                        categoryExpanded = false
                    }
                )
            }
        }

        DropdownField(
            label = "Resolución del módulo (px)",
            value = when {
                displayModule == null -> "Selecciona resolución"
                modulesOnSide -> "${displayModule.dropdownLabel} · acostado"
                else -> displayModule.dropdownLabel
            },
            expanded = moduleExpanded,
            onExpand = { moduleExpanded = true },
            onDismiss = { moduleExpanded = false }
        ) {
            categoryModules.forEach { module ->
                val electrical = ElectricalCatalog.forModule(module, environment)
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = module.dropdownLabel, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = buildString {
                                    append("Pitch ${module.pitch} · ${module.physicalLabel}")
                                    append(" · ${electrical.wattsPromedio.roundToInt()}–${electrical.wattsMax.roundToInt()} W")
                                    append(" · ${formatDouble(electrical.amps110vMax, 1)} A máx @110V")
                                    append(" · ${module.modulesPerSignalLine} mód/línea")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = LedColors.TextMuted
                            )
                        }
                    },
                    onClick = {
                        onModuleSelected(module)
                        moduleExpanded = false
                    }
                )
            }
        }

        displayModule?.let { module ->
            val electrical = remember(module.id, module.widthPx, module.heightPx, environment) {
                ElectricalCatalog.forModule(module, environment)
            }
            Text(
                text = "Cálculo: ${module.widthPx} px × cant. horizontal  |  ${module.heightPx} px × cant. vertical",
                style = MaterialTheme.typography.bodySmall,
                color = LedColors.NeonGold
            )
            if (modulesOnSide) {
                Text(
                    text = "Orientación: acostado · ${module.physicalLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LedColors.NeonOrange
                )
            }
            ModuleElectricalSummary(
                module = module,
                electrical = electrical,
                environment = environment
            )
        }
    }
}

@Composable
fun ModuleElectricalSummary(
    module: ModuleSpec,
    electrical: ElectricalSpec,
    environment: CabinetEnvironment,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .background(LedColors.Black.copy(alpha = 0.35f), MaterialTheme.shapes.small)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Por gabinete (${environment.label})",
            color = LedColors.NeonTeal,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${formatDouble(electrical.wattsPromedio, 0)} W prom / ${formatDouble(electrical.wattsMax, 0)} W máx",
            color = LedColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "110 V: ${formatDouble(electrical.amps110vPromedio, 2)} A prom / ${formatDouble(electrical.amps110vMax, 2)} A máx",
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "220 V: ${formatDouble(electrical.amps220vPromedio, 2)} A prom / ${formatDouble(electrical.amps220vMax, 2)} A máx",
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Señal: ${module.modulesPerSignalLine} gabinetes/línea (655360 ÷ ${module.totalPixels} px)",
            color = LedColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    menuContent: @Composable () -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(LedColors.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                .border(1.dp, LedColors.GridLine, MaterialTheme.shapes.small)
                .clickable { onExpand() }
                .padding(12.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = LedColors.TextSecondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(value, color = LedColors.TextPrimary, style = MaterialTheme.typography.bodyLarge)
                Text("▼", color = LedColors.NeonCyan)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            menuContent()
        }
    }
}
