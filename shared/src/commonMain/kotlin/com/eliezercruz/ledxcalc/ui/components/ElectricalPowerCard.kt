package com.eliezercruz.ledxcalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.domain.BreakerRating
import com.eliezercruz.ledxcalc.domain.ElectricalLoadCalculator
import com.eliezercruz.ledxcalc.domain.SupplyVoltage
import com.eliezercruz.ledxcalc.ui.formatUiText
import com.eliezercruz.ledxcalc.ui.theme.LedColors
import com.eliezercruz.ledxcalc.util.formatDouble

@Composable
fun ElectricalPowerCard(
    modelLabel: String,
    totalGabinetes: Int,
    wattsPromedioPorGabinete: Double,
    wattsMaxPorGabinete: Double,
    selectedVoltage: SupplyVoltage,
    onVoltageChange: (SupplyVoltage) -> Unit,
    selectedBreaker: BreakerRating,
    onBreakerChange: (BreakerRating) -> Unit,
    modifier: Modifier = Modifier
) {
    val voltage = selectedVoltage
    val breaker = selectedBreaker

    val load = ElectricalLoadCalculator.calcularCargaElectrica(
        wattsMaxPorGabinete = wattsMaxPorGabinete,
        wattsPromedioPorGabinete = wattsPromedioPorGabinete,
        cantidad = totalGabinetes,
        voltaje = voltage,
        amperajeBreaker = breaker
    )

    val cardBg by animateColorAsState(
        targetValue = if (load.sobrepasaLimite) LedColors.ElectricPanelWarn else LedColors.ElectricPanelOk,
        animationSpec = tween(400),
        label = "cardBg"
    )
    val accentColor by animateColorAsState(
        targetValue = if (load.sobrepasaLimite) LedColors.ElectricWarn else LedColors.ElectricOk,
        animationSpec = tween(400),
        label = "accentColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (load.sobrepasaLimite) Color(0xFFFF5252) else Color(0xFF00C853),
        animationSpec = tween(400),
        label = "borderColor"
    )

    LedPanel(
        modifier = modifier.border(2.dp, borderColor, MaterialTheme.shapes.medium),
        accentColor = if (load.sobrepasaLimite) LedColors.ElectricWarn else LedColors.ElectricOk,
        glowElevation = if (load.sobrepasaLimite) 14.dp else 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg.copy(alpha = 0.85f), MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        Text(
            text = formatUiText("⚡ POTENCIA Y PROTECCIÓN (Regla del 80%)"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LedColors.NeonCyan,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$modelLabel · ${formatW(wattsPromedioPorGabinete)} W prom / ${formatW(wattsMaxPorGabinete)} W máx por gabinete",
            color = LedColors.NeonTeal,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Voltaje:", color = LedColors.TextPrimary)
            Button3D("110V", voltage == SupplyVoltage.V110) { onVoltageChange(SupplyVoltage.V110) }
            Button3D("220V", voltage == SupplyVoltage.V220) { onVoltageChange(SupplyVoltage.V220) }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("Breaker:", color = LedColors.TextPrimary)
            BreakerRating.commonOptions.forEach { option ->
                Button3D(option.label, breaker == option) { onBreakerChange(option) }
            }
        }

        PowerLine("Consumo prom: ${formatW(load.wattsTotalesPromedio)} W", Color(0xFF69F0AE))
        PowerLine(
            "Consumo máx (pico blanco): ${formatW(load.wattsTotalesMax)} W",
            if (load.sobrepasaLimite) LedColors.NeonOrange else LedColors.NeonGold
        )
        PowerLine("Amperaje prom (${voltage.label}): ${load.amperajePromedioFormatted} A", LedColors.NeonTeal)
        PowerLine(
            "Amperaje máx (${voltage.label}): ${load.amperajeMaxFormatted} A",
            accentColor,
            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        PowerLine("Límite continuo seguro (80% de ${breaker.label}): ${load.limiteSeguridadFormatted} A", LedColors.NeonGold)
        PowerLine("Gabinetes máx. por circuito: ${load.gabinetesPermitidosPorCircuito}", LedColors.BasesAccent)

        Text(
            text = formatUiText(load.mensajeAlerta),
            color = accentColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (load.sobrepasaLimite) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .background(LedColors.Black.copy(alpha = 0.35f), MaterialTheme.shapes.small)
                .padding(10.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = formatUiText("🔌 ${load.mensajeDistribucion}"),
            color = LedColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        }
    }
}

@Composable
private fun PowerLine(
    text: String,
    color: Color,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    Text(
        text = text,
        color = color,
        style = style,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

private fun formatW(value: Double): String = formatDouble(value, 0)
