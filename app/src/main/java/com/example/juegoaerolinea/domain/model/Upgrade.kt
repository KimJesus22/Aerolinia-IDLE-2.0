package com.example.juegoaerolinea.domain.model

data class Upgrade(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val baseCost: Double,
    val maxLevel: Int,
    val level: Int
) {
    val upgradeCost: Double
        get() = baseCost * Math.pow(3.0, level.toDouble())

    val isMaxLevel: Boolean
        get() = level >= maxLevel

    fun getMultiplier(): Double {
        if (level == 0) return 1.0
        return when (id) {
            "fuel" -> 1.0 + level * 1.0         // x2 per level (additive)
            "lounge" -> 1.0 + level * 0.5        // x1.5 per level
            "training" -> 1.0 + level * 1.0      // x2 per level
            "marketing" -> 1.0 + level * 2.0     // x3 per level
            else -> 1.0
        }
    }
}
