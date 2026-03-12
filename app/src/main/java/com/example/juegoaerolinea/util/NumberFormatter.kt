package com.example.juegoaerolinea.util

import kotlin.math.abs
import kotlin.math.floor

object NumberFormatter {
    private val suffixes = listOf("", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc")

    fun format(value: Double): String {
        if (value < 0) return "-${format(abs(value))}"
        if (value < 1000) return "$${floor(value).toLong()}"

        var reduced = value
        var suffixIndex = 0
        while (reduced >= 1000 && suffixIndex < suffixes.size - 1) {
            reduced /= 1000.0
            suffixIndex++
        }

        return if (reduced >= 100) {
            "$${floor(reduced).toLong()}${suffixes[suffixIndex]}"
        } else if (reduced >= 10) {
            "$${String.format("%.1f", reduced)}${suffixes[suffixIndex]}"
        } else {
            "$${String.format("%.2f", reduced)}${suffixes[suffixIndex]}"
        }
    }
}
