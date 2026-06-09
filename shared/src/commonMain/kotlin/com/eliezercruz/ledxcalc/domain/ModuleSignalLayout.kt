package com.eliezercruz.ledxcalc.domain

import kotlin.math.ceil

/**
 * Orden serpentina de módulos por línea de señal (mismo criterio que el boceto original).
 */
object ModuleSignalLayout {
    data class Layout(
        val moduleGroupIndex: Map<Pair<Int, Int>, Int>,
        val signalPaths: List<List<Pair<Int, Int>>>,
        val numSignalLines: Int,
        val maxColumnsPerLine: Int
    )

    fun compute(columns: Int, rows: Int, groupSize: Int): Layout? {
        if (columns <= 0 || rows <= 0) return null
        val maxColumnsPerLine = (groupSize / rows).coerceAtLeast(1)
        val numSignalLines = ceil(columns.toFloat() / maxColumnsPerLine).toInt().coerceAtLeast(1)
        val moduleGroupIndex = mutableMapOf<Pair<Int, Int>, Int>()
        val signalPaths = mutableListOf<List<Pair<Int, Int>>>()

        for (lineIdx in 0 until numSignalLines) {
            val startCol = lineIdx * maxColumnsPerLine
            val endCol = minOf(startCol + maxColumnsPerLine, columns)
            if (startCol >= columns) continue
            val path = mutableListOf<Pair<Int, Int>>()
            for (rowFromBottom in 0 until rows) {
                val r = (rows - 1) - rowFromBottom
                val colRange = if (rowFromBottom % 2 == 0) {
                    startCol until endCol
                } else {
                    (endCol - 1 downTo startCol)
                }
                for (c in colRange) {
                    val key = Pair(c, r)
                    moduleGroupIndex[key] = lineIdx
                    path.add(key)
                }
            }
            signalPaths.add(path)
        }

        return Layout(
            moduleGroupIndex = moduleGroupIndex,
            signalPaths = signalPaths,
            numSignalLines = numSignalLines,
            maxColumnsPerLine = maxColumnsPerLine
        )
    }
}
