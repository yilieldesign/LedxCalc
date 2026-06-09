package com.eliezercruz.ledxcalc.ui.drawing

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

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
    val paint = Paint().apply {
        this.color = color.toArgb()
        textSize = textSizePx
        textAlign = if (centerAlign) Paint.Align.CENTER else Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        isAntiAlias = true
        if (withShadow) {
            setShadowLayer(3f, 1f, 1f, android.graphics.Color.BLACK)
        }
    }
    val canvas = drawContext.canvas.nativeCanvas
    if (rotateDegrees != 0f) {
        canvas.save()
        canvas.rotate(rotateDegrees, x, y)
        canvas.drawText(text, x, y, paint)
        canvas.restore()
    } else {
        canvas.drawText(text, x, y, paint)
    }
}
