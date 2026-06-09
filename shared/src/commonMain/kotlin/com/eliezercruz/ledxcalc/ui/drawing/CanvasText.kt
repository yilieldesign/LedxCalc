package com.eliezercruz.ledxcalc.ui.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

expect fun DrawScope.drawCanvasText(
    text: String,
    x: Float,
    y: Float,
    textSizePx: Float,
    color: Color,
    bold: Boolean = false,
    centerAlign: Boolean = false,
    rotateDegrees: Float = 0f,
    withShadow: Boolean = true
)
