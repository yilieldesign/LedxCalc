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
    "ℹ️" to "i",
    "↔" to "<->",
    "↕" to "^v"
)

actual fun formatUiText(text: String): String {
    var result = text
    emojiReplacements.forEach { (emoji, replacement) ->
        result = result.replace(emoji, replacement)
    }
    return result.replace(Regex("  +"), " ").trim()
}
