package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.BasesDistribution
import com.eliezercruz.ledxcalc.domain.SupportCalculation
import com.eliezercruz.ledxcalc.ui.theme.LedColors

private fun ghostGapLegend(ghostModules: Int): String =
    if (ghostModules == 1) {
        "Dejando hueco de 1 módulo"
    } else {
        "Dejando hueco de $ghostModules módulos"
    }

@Composable
fun BasesDistributionList(
    support: SupportCalculation,
    modifier: Modifier = Modifier,
    showStairs: Boolean = true,
    ghostModules: Int = 0
) {
    val baseLines = BasesDistribution.baseLines(support)
    if (baseLines.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Bases de piso",
            color = LedColors.NeonCyan,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        baseLines.forEach { line ->
            Text(
                text = line,
                color = LedColors.BasesAccent,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        if (showStairs) {
            BasesDistribution.stairLines(support).forEach { line ->
                Text(
                    text = line,
                    color = LedColors.NeonGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        if (ghostModules > 0) {
            Text(
                text = ghostGapLegend(ghostModules),
                color = LedColors.NeonPurple,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
