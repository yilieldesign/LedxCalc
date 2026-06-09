package com.eliezercruz.ledxcalc.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.eliezercruz.ledxcalc.domain.PdfExportData
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual class PlatformContext

actual fun createPlatformContext(): PlatformContext = PlatformContext()

@Composable
actual fun VideoBackground(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    )
}

actual fun sharePdf(context: PlatformContext, data: PdfExportData) {
    val summary = buildPdfTextSummary(data)
    val controller = UIActivityViewController(
        activityItems = listOf(summary),
        applicationActivities = null
    )
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        controller,
        animated = true,
        completion = null
    )
}

actual fun previewPdf(context: PlatformContext, data: PdfExportData) {
    sharePdf(context, data)
}

actual fun closeApp(context: PlatformContext) {
    kotlin.system.exitProcess(0)
}
