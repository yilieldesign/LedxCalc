package com.eliezercruz.ledxcalc.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.eliezercruz.ledxcalc.domain.PdfExportData
import com.eliezercruz.ledxcalc.platform.pdf.LedxColoredPdf
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

@OptIn(ExperimentalEncodingApi::class)
actual fun sharePdf(context: PlatformContext, data: PdfExportData) {
    val bytes = LedxColoredPdf.build(data)
    val dataUrl = "data:application/pdf;base64,${Base64.encode(bytes)}"
    triggerPdfDownload(LedxColoredPdf.fileName(data), dataUrl)
}

@OptIn(ExperimentalEncodingApi::class)
actual fun previewPdf(context: PlatformContext, data: PdfExportData) {
    val bytes = LedxColoredPdf.build(data)
    val dataUrl = "data:application/pdf;base64,${Base64.encode(bytes)}"
    openPdfPreview(dataUrl)
}

actual fun closeApp(context: PlatformContext) = Unit

private fun triggerPdfDownload(filename: String, dataUrl: String): Unit =
    js(
        """
        (function() {
          var a = document.createElement('a');
          a.href = dataUrl;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
        })()
        """
    )

private fun openPdfPreview(dataUrl: String): Unit =
    js("window.open(dataUrl, '_blank')")
