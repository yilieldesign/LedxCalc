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
    sharePdfInBrowser(Base64.encode(bytes), LedxColoredPdf.fileName(data))
}

@OptIn(ExperimentalEncodingApi::class)
actual fun previewPdf(context: PlatformContext, data: PdfExportData) {
    val bytes = LedxColoredPdf.build(data)
    previewPdfInBrowser(Base64.encode(bytes), LedxColoredPdf.fileName(data))
}

actual fun closeApp(context: PlatformContext) = Unit

/**
 * iOS Safari/PWA: data: URLs open blank tabs. Use Blob URLs + Web Share API.
 * [js] may only reference this function's parameters.
 */
private fun sharePdfInBrowser(base64: String, filename: String): Unit = js(
    """{
      var binary = atob(base64);
      var bytes = new Uint8Array(binary.length);
      for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
      var file = new File([bytes], filename, { type: 'application/pdf' });
      if (navigator.share && navigator.canShare && navigator.canShare({ files: [file] })) {
        navigator.share({ files: [file], title: filename }).catch(function () {});
        return;
      }
      var url = URL.createObjectURL(file);
      var a = document.createElement('a');
      a.href = url;
      a.download = filename;
      a.target = '_blank';
      a.rel = 'noopener';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      setTimeout(function () { URL.revokeObjectURL(url); }, 60000);
    }"""
)

private fun previewPdfInBrowser(base64: String, filename: String): Unit = js(
    """{
      var binary = atob(base64);
      var bytes = new Uint8Array(binary.length);
      for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
      var blob = new Blob([bytes], { type: 'application/pdf' });
      var file = new File([bytes], filename, { type: 'application/pdf' });
      var url = URL.createObjectURL(blob);
      var isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) ||
        (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
      // iOS Safari/PWA: data: URLs = blank tab; blob open is flaky in standalone.
      // Share sheet lets the user Open in Books / Files / Markup.
      if (isIOS && navigator.share && navigator.canShare && navigator.canShare({ files: [file] })) {
        navigator.share({ files: [file], title: filename }).catch(function () {
          window.location.href = url;
        });
        setTimeout(function () { URL.revokeObjectURL(url); }, 120000);
        return;
      }
      var opened = window.open(url, '_blank');
      if (!opened) {
        window.location.href = url;
      } else {
        setTimeout(function () { URL.revokeObjectURL(url); }, 60000);
      }
    }"""
)
