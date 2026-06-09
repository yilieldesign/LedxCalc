package com.eliezercruz.ledxcalc.ui

/**
 * Normaliza etiquetas de UI para la plataforma actual.
 * En web/Wasm los emojis no se renderizan con la fuente Skia por defecto.
 */
expect fun formatUiText(text: String): String
