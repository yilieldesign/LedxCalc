package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.SketchKind
import com.eliezercruz.ledxcalc.domain.SketchSelection
import com.eliezercruz.ledxcalc.ui.formatUiText
import com.eliezercruz.ledxcalc.ui.theme.LedColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SketchOptionsSelector(
    selection: SketchSelection,
    onSelectionChange: (SketchSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    LedPanel(modifier = modifier, accentColor = LedColors.NeonPurple) {
        Text(
            text = formatUiText("📐 Bocetos a mostrar e incluir en PDF"),
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Estructura: ${selection.mounting.label} · Activa o desactiva cada boceto",
            color = LedColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SketchKind.entries
                .filter { it.availableFor(selection.mounting) }
                .forEach { kind ->
                    Button3D(
                        text = kind.shortLabel,
                        isSelected = selection.isEnabled(kind),
                        onClick = { onSelectionChange(selection.toggle(kind)) }
                    )
                }
        }
    }
}
