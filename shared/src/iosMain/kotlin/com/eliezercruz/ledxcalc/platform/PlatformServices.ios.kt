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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
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

@OptIn(ExperimentalForeignApi::class)
actual fun sharePdf(context: PlatformContext, data: PdfExportData) {
    val bytes = LedxColoredPdf.build(data)
    val fileName = LedxColoredPdf.fileName(data)
    val path = NSTemporaryDirectory() + fileName
    val nsData = bytes.toNSData()
    nsData.writeToFile(path, atomically = true)
    val url = NSURL.fileURLWithPath(path)
    val controller = UIActivityViewController(
        activityItems = listOf(url),
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

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.dataWithBytes(pinned.addressOf(0), length = size.toULong())
}
