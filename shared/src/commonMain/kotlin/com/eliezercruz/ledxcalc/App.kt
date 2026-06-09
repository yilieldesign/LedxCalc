package com.eliezercruz.ledxcalc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eliezercruz.ledxcalc.data.AppRepository
import com.eliezercruz.ledxcalc.platform.PlatformContext
import com.eliezercruz.ledxcalc.platform.VideoBackground
import com.eliezercruz.ledxcalc.ui.components.Button3D
import com.eliezercruz.ledxcalc.ui.components.LedPixelBackground
import com.eliezercruz.ledxcalc.ui.components.LedTitleBar
import com.eliezercruz.ledxcalc.ui.screens.ResolutionCalculatorScreen
import com.eliezercruz.ledxcalc.ui.formatUiText
import com.eliezercruz.ledxcalc.ui.theme.LedColors
import com.eliezercruz.ledxcalc.ui.theme.LedxCalcTheme
import com.eliezercruz.ledxcalc.resources.Res
import com.eliezercruz.ledxcalc.resources.logo_calculadora
import com.eliezercruz.ledxcalc.resources.logo_g
import org.jetbrains.compose.resources.painterResource

@Composable
fun LedxCalcApp(
    platformContext: PlatformContext,
    repository: AppRepository = remember { AppRepository() }
) {
    val darkOverride = remember { repository.loadDarkThemeOverride() }
    var showWelcome by rememberSaveable { mutableStateOf(true) }

    LedxCalcTheme(forceDarkTheme = darkOverride ?: true) {
        Box(Modifier.fillMaxSize()) {
            VideoBackground(Modifier.fillMaxSize())
            LedPixelBackground(Modifier.fillMaxSize())
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(
                            LedColors.Black.copy(alpha = 0.72f),
                            LedColors.Black.copy(alpha = 0.88f)
                        )
                    )
                )
            )

            if (showWelcome) {
                WelcomeScreen(onStart = { showWelcome = false })
            } else {
                ResolutionCalculatorScreen(
                    repository = repository,
                    platformContext = platformContext,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(Res.drawable.logo_calculadora),
            contentDescription = "Logo",
            modifier = Modifier.height(150.dp).shadow(24.dp)
        )
        Spacer(Modifier.height(32.dp))
        Text(
            "Bienvenid@",
            style = MaterialTheme.typography.headlineLarge,
            color = LedColors.NeonCyan,
            modifier = Modifier
                .shadow(12.dp, spotColor = LedColors.NeonCyan.copy(alpha = 0.6f))
                .background(LedColors.Panel.copy(alpha = 0.85f), MaterialTheme.shapes.medium)
                .border(1.dp, LedColors.NeonCyan.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button3D("🚀 Calcular", true, onClick = onStart)
        Spacer(Modifier.weight(1f))
        Image(painterResource(Res.drawable.logo_g), "Logo G", Modifier.height(50.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            formatUiText("✨ Esta app fue creada por Eliezer Cruz ✨"),
            color = LedColors.NeonGold,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}
