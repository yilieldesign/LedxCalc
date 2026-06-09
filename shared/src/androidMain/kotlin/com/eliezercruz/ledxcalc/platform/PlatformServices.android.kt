package com.eliezercruz.ledxcalc.platform

import android.content.Context
import android.content.Intent
import android.app.Activity
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.eliezercruz.ledxcalc.domain.PdfExportData
import com.eliezercruz.ledxcalc.platform.pdf.PdfSketchRenderer
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

actual class PlatformContext(val androidContext: Context)

actual fun createPlatformContext(): PlatformContext {
    throw IllegalStateException("Use LocalPlatformContext in Compose")
}

@Composable
fun rememberPlatformContext(): PlatformContext {
    val context = LocalContext.current
    return remember(context) { PlatformContext(context) }
}

@Composable
actual fun VideoBackground(modifier: Modifier) {
    val context = LocalContext.current
    var mediaPlayer: android.media.MediaPlayer? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    AndroidView(
        factory = { ctx ->
            val frame = android.widget.FrameLayout(ctx).apply {
                setBackgroundColor(android.graphics.Color.BLACK)
            }
            val tv = android.view.TextureView(ctx)
            frame.addView(tv, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            ))
            tv.surfaceTextureListener = object : android.view.TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(st: android.graphics.SurfaceTexture, w: Int, h: Int) {
                    runCatching {
                        val resId = ctx.resources.getIdentifier("fondo_video_2", "raw", ctx.packageName)
                        if (resId == 0) return
                        mediaPlayer = android.media.MediaPlayer().apply {
                            setDataSource(ctx, android.net.Uri.parse("android.resource://${ctx.packageName}/$resId"))
                            setSurface(android.view.Surface(st))
                            isLooping = true
                            setVolume(0f, 0f)
                            setOnPreparedListener { it.start() }
                            prepareAsync()
                        }
                    }
                }
                override fun onSurfaceTextureSizeChanged(st: android.graphics.SurfaceTexture, w: Int, h: Int) = Unit
                override fun onSurfaceTextureDestroyed(st: android.graphics.SurfaceTexture): Boolean {
                    mediaPlayer?.release()
                    mediaPlayer = null
                    return true
                }
                override fun onSurfaceTextureUpdated(st: android.graphics.SurfaceTexture) = Unit
            }
            frame
        },
        modifier = modifier
    )
}

actual fun sharePdf(context: PlatformContext, data: PdfExportData) {
    val file = createPdfFile(context.androidContext, data) ?: return
    val uri = FileProvider.getUriForFile(
        context.androidContext,
        "${context.androidContext.packageName}.fileprovider",
        file
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    launchIntent(context.androidContext, Intent.createChooser(sendIntent, "Compartir boceto PDF"))
}

actual fun previewPdf(context: PlatformContext, data: PdfExportData) {
    val file = createPdfFile(context.androidContext, data) ?: return
    val uri = FileProvider.getUriForFile(
        context.androidContext,
        "${context.androidContext.packageName}.fileprovider",
        file
    )
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    launchIntent(context.androidContext, viewIntent)
}

private fun launchIntent(context: Context, intent: Intent) {
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

actual fun closeApp(context: PlatformContext) {
    (context.androidContext as? android.app.Activity)?.finishAffinity()
}

private fun createPdfFile(context: Context, data: PdfExportData): File? {
    return runCatching {
        val pageWidth = 612
        val pageHeight = 792
        val pdfDocument = PdfDocument()
        val paint = Paint().apply { isAntiAlias = true }

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        PdfSketchRenderer.drawSinglePage(page.canvas, data, paint, pageWidth, pageHeight)
        pdfDocument.finishPage(page)

        val cacheDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val sizeText = if (data.inputWidthMeters != null && data.inputHeightMeters != null) {
            "${String.format(Locale.US, "%.2f", data.inputWidthMeters)}x${String.format(Locale.US, "%.2f", data.inputHeightMeters)}"
        } else "sin_medidas"
        val file = File(cacheDir, "pantalla_led_${sizeText}_metros.pdf")
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()
        file
    }.getOrNull()
}
