package com.eliezercruz.ledxcalc.util

import kotlin.math.pow
import kotlin.math.round

fun formatDouble(value: Double, decimals: Int = 2): String {
    if (decimals <= 0) return round(value).toLong().toString()
    val factor = 10.0.pow(decimals)
    val rounded = round(value * factor) / factor
    val negative = rounded < 0
    val abs = kotlin.math.abs(rounded)
    val intPart = abs.toLong()
    val frac = ((abs - intPart) * factor + 0.5).toLong().coerceAtMost(factor.toLong() - 1)
    val fracStr = frac.toString().padStart(decimals, '0')
    return buildString {
        if (negative) append('-')
        append(intPart)
        append('.')
        append(fracStr)
    }
}

fun trimTrailingZeros(value: Double, decimals: Int = 2): String =
    formatDouble(value, decimals).trimEnd('0').trimEnd('.')

expect fun currentTimeMillis(): Long
