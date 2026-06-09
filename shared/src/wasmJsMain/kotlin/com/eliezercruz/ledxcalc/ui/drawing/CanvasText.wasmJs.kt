package com.eliezercruz.ledxcalc.ui.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint as SkiaPaint

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
    val font = Font(null, textSizePx)
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
