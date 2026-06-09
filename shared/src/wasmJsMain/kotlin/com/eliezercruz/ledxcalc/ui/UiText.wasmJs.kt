package com.eliezercruz.ledxcalc.ui

private val emojiReplacements = mapOf(
    "➕" to "+",
    "📋" to "[]",
    "💾" to "[]",
    "🗑️" to "X",
    "🗑" to "X",
    "❌" to "X",
    "📐" to "",
    "📏" to "",
    "⚠️" to "!",
    "✨" to "*",
    "🏗️" to "",
    "⚡" to "",
    "🔌" to "",
    "👁️" to "",
    "📄" to "",
    "🚀" to "",
    "🕳️" to "",
    "🖥️" to "",
    "🔩" to "",
    "🌐" to "",
    "🔲" to "",
    "📺" to "",
    "ℹ️" to "i",
    "↔" to "<->",
    "↕" to "^v"
)

actual fun formatUiText(text: String): String {
    var result = text
    emojiReplacements.forEach { (emoji, replacement) ->
        result = result.replace(emoji, replacement)
    }
    result = stripRemainingEmojis(result)
    return result.replace(Regex("  +"), " ").trim()
}

/** Quita emojis restantes que Skia/Wasm no puede dibujar (evita cuadros vacíos). */
private fun stripRemainingEmojis(text: String): String {
    if (text.isEmpty()) return text
    val out = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        val c = text[i]
        if (c.isHighSurrogate() && i + 1 < text.length) {
            val low = text[i + 1]
            if (low.isLowSurrogate()) {
                val codePoint = ((c.code - 0xD800) shl 10) + (low.code - 0xDC00) + 0x10000
                if (!isEmojiCodePoint(codePoint)) {
                    out.append(c)
                    out.append(low)
                }
                i += 2
                continue
            }
        }
        if (!isEmojiCodePoint(c.code)) {
            out.append(c)
        }
        i += 1
    }
    return out.toString()
}

private fun isEmojiCodePoint(codePoint: Int): Boolean = when (codePoint) {
    in 0x1F300..0x1FAFF,
    in 0x1F600..0x1F64F,
    in 0x1F680..0x1F6FF,
    in 0x1F900..0x1F9FF,
    in 0x2600..0x26FF,
    in 0x2700..0x27BF,
    in 0x2300..0x23FF,
    in 0x2B05..0x2B55,
    0xFE0F, 0x200D -> true
    else -> false
}
