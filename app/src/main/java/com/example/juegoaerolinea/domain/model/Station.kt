package com.example.juegoaerolinea.domain.model

data class Station(
    val id: String,
    val name: String,
    val emoji: String,
    val baseCost: Double,
    val baseEarn: Double,
    val level: Int,
    val isUnlocked: Boolean,
    val x: Float, // Position on map (Grid/Canvas X)
    val y: Float  // Position on map (Grid/Canvas Y)
) {
    val upgradeCost: Double
        get() = baseCost * Math.pow(1.18, level.toDouble())

    val currentEarnPerSec: Double
        get() = if (isUnlocked) baseEarn * level else 0.0
}
