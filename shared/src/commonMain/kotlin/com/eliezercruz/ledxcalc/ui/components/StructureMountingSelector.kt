package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.StructureMounting
import com.eliezercruz.ledxcalc.ui.theme.LedColors

@Composable
fun StructureMountingSelector(
    selected: StructureMounting,
    onSelect: (StructureMounting) -> Unit,
    modifier: Modifier = Modifier
) {
    LedPanel(modifier = modifier, accentColor = LedColors.NeonBlue) {
        Text(
            text = "🏗️ ¿Cuál será tu estructura?",
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Elige el montaje para activar los bocetos correspondientes (bases o truss).",
            color = LedColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StructureMounting.entries.forEach { mounting ->
                Button3D(
                    text = mounting.label,
                    isSelected = selected == mounting,
                    onClick = { onSelect(mounting) }
                )
            }
        }

        val hint = when (selected) {
            StructureMounting.FLOOR_BASES ->
                "Bocetos disponibles: bases de piso y escaleras. Actívalos en resultados."
            StructureMounting.TRUSS ->
                "Boceto de truss disponible. Actívalo en resultados para ver dimensiones recomendadas."
        }
        Text(
            text = hint,
            color = if (selected == StructureMounting.FLOOR_BASES) LedColors.BasesAccent else LedColors.TrussAccent,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StructureRecommendationCard(
    headline: String,
    primaryLabel: String,
    primaryValue: String,
    detailLines: List<String>,
    mounting: StructureMounting,
    modifier: Modifier = Modifier,
    summaryLines: List<String> = emptyList()
) {
    val accent = when (mounting) {
        StructureMounting.FLOOR_BASES -> LedColors.BasesAccent
        StructureMounting.TRUSS -> LedColors.TrussAccent
    }
    LedPanel(modifier = modifier, accentColor = accent) {
        Text(
            text = headline,
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        if (summaryLines.isNotEmpty()) {
            Text(
                text = primaryLabel,
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            summaryLines.forEach { line ->
                Text(
                    text = line,
                    color = LedColors.BasesAccent,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else if (primaryValue.isNotBlank()) {
            Text(
                text = primaryLabel,
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = primaryValue,
                color = accent,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        detailLines.forEach { line ->
            Text(
                text = "• $line",
                color = LedColors.TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
