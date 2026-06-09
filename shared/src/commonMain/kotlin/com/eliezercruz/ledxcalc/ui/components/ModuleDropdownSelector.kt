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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.ModuleCatalog
import com.eliezercruz.ledxcalc.domain.ModulePhysicalCategory
import com.eliezercruz.ledxcalc.domain.ModuleSpec
import com.eliezercruz.ledxcalc.ui.theme.LedColors

@Composable
fun ModuleDropdownSelector(
    selectedCategory: ModulePhysicalCategory,
    selectedModule: ModuleSpec?,
    onCategorySelected: (ModulePhysicalCategory) -> Unit,
    onModuleSelected: (ModuleSpec) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryModules = remember(selectedCategory) { ModuleCatalog.forCategory(selectedCategory) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var moduleExpanded by remember { mutableStateOf(false) }

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
            value = selectedModule?.dropdownLabel ?: "Selecciona resolución",
            expanded = moduleExpanded,
            onExpand = { moduleExpanded = true },
            onDismiss = { moduleExpanded = false }
        ) {
            categoryModules.forEach { module ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = module.dropdownLabel, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = buildString {
                                    append("Pitch ${module.pitch} · ${module.physicalLabel}")
                                    append(" · ${module.wattsPerModule.toInt()}–${module.wattsPerModuleMax.toInt()} W")
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

        selectedModule?.let { module ->
            Text(
                text = "Cálculo: ${module.widthPx} px × cant. horizontal  |  ${module.heightPx} px × cant. vertical",
                style = MaterialTheme.typography.bodySmall,
                color = LedColors.NeonGold
            )
        }
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
