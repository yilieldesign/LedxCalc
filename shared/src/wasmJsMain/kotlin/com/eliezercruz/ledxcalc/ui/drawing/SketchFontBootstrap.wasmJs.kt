package com.eliezercruz.ledxcalc.ui.drawing

import com.eliezercruz.ledxcalc.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface

@OptIn(ExperimentalResourceApi::class)
object SketchFontBootstrap {
    private var loadedTypeface: Typeface? = null

    suspend fun ensureLoaded() {
        if (loadedTypeface != null) return
        val bytes = Res.readBytes("font/Roboto-Regular.ttf")
        loadedTypeface = FontMgr.default.makeFromData(Data.makeFromBytes(bytes))
    }

    fun typefaceOrNull(): Typeface? = loadedTypeface
}
