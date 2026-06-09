package com.eliezercruz.ledxcalc.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.eliezercruz.ledxcalc.domain.PdfExportData
import com.eliezercruz.ledxcalc.util.formatDouble
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement

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
    downloadTextFile("ledxcalc_${exportFileSuffix(data)}.txt", buildPdfTextSummary(data))
}

actual fun previewPdf(context: PlatformContext, data: PdfExportData) {
    val url = blobUrlForText(buildPdfTextSummary(data))
    window.open(url, "_blank")
}

actual fun closeApp(context: PlatformContext) = Unit

private fun exportFileSuffix(data: PdfExportData): String =
    if (data.inputWidthMeters != null && data.inputHeightMeters != null) {
        "${formatDouble(data.inputWidthMeters, 2)}x${formatDouble(data.inputHeightMeters, 2)}"
    } else {
        "pantalla"
    }

private fun blobUrlForText(content: String): String =
    js("URL.createObjectURL(new Blob([content], {type: 'text/plain;charset=utf-8'}))")

private fun revokeBlobUrl(url: String) {
    js("URL.revokeObjectURL(url)")
}

private fun downloadTextFile(filename: String, content: String) {
    val url = blobUrlForText(content)
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = filename
    document.body?.appendChild(anchor)
    anchor.click()
    document.body?.removeChild(anchor)
    revokeBlobUrl(url)
}
