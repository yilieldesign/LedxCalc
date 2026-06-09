package com.eliezercruz.ledxcalc.ui.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.FontCollection

/**
 * En Wasm/Skia, Font(null) no tiene glifos en el navegador.
 * Hay que resolver un Typeface real (fuente embebida de Skiko o sans-serif).
 */
private object WasmSketchFonts {
    fun typeface(bold: Boolean): Typeface? {
        SketchFontBootstrap.typefaceOrNull()?.let { return it }
        return if (bold) boldFallback() else normalFallback()
    }

    private fun normalFallback(): Typeface? =
        FontCollection().defaultFallback()
            ?: FontMgr.default.matchFamilyStyle(null, FontStyle.NORMAL)
            ?: FontMgr.default.matchFamilyStyle("sans-serif", FontStyle.NORMAL)
            ?: FontMgr.default.matchFamilyStyle("Arial", FontStyle.NORMAL)

    private fun boldFallback(): Typeface? =
        FontMgr.default.matchFamilyStyle(null, FontStyle.BOLD)
            ?: FontMgr.default.matchFamilyStyle("sans-serif", FontStyle.BOLD)
            ?: normalFallback()
}

actual fun DrawScope.drawCanvasText(
    text: String,
    x: Float,
    y: Float,
    textSizePx: Float,
    color: Color,
    bold: Boolean,
    centerAlign: Boolean,
    rotateDegrees: Float,
    withShadow: Boolean
) {
    val skiaPaint = SkiaPaint().apply {
        this.color = org.jetbrains.skia.Color.makeARGB(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        isAntiAlias = true
    }
    val baseTypeface = WasmSketchFonts.typeface(bold)
    val font = if (baseTypeface != null) Font(baseTypeface, textSizePx) else Font(null, textSizePx)
    val textWidth = font.measureTextWidth(text, skiaPaint)
    val drawX = if (centerAlign) x - textWidth / 2f else x
    val canvas = drawContext.canvas.nativeCanvas
    if (rotateDegrees != 0f) {
        canvas.save()
        canvas.rotate(rotateDegrees, drawX, y)
        canvas.drawString(text, drawX, y, font, skiaPaint)
        canvas.restore()
    } else {
        canvas.drawString(text, drawX, y, font, skiaPaint)
    }
}
