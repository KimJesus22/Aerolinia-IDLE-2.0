package com.example.juegoaerolinea.util

import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.domain.model.Upgrade

object Constants {
    val baseStations = listOf(
        Station("checkin", "Check-In", "🛂", 10.0, 1.0, 0, false, 200f, 200f),
        Station("national", "Nacional", "✈️", 80.0, 4.0, 0, false, 500f, 200f),
        Station("cafe", "Cafetería", "☕", 200.0, 10.0, 0, false, 800f, 200f),
        Station("international", "Internacional", "🌍", 1500.0, 40.0, 0, false, 200f, 500f),
        Station("vip", "Sala VIP", "💎", 8000.0, 150.0, 0, false, 500f, 500f),
        Station("dutyfree", "Duty Free", "🛍️", 30000.0, 500.0, 0, false, 800f, 500f),
        Station("hangar", "Hangar", "🔧", 120000.0, 2000.0, 0, false, 350f, 800f),
        Station("space", "Espacial", "🚀", 1000000.0, 15000.0, 0, false, 650f, 800f)
    )

    val baseUpgrades = listOf(
        Upgrade("fuel", "Combustible Premium", "⛽", "x2 ganancias globales", 500.0, 10, 0),
        Upgrade("lounge", "Sala VIP", "🛋️", "x1.5 ganancias por click", 2000.0, 8, 0),
        Upgrade("training", "Entrenamiento Pilotos", "👨‍✈️", "x2 velocidad producción", 5000.0, 10, 0),
        Upgrade("marketing", "Marketing Global", "📢", "x3 ganancias offline", 10000.0, 5, 0)
    )

    const val OFFLINE_EFFICIENCY = 0.5
    const val PRESTIGE_THRESHOLD = 1_000_000.0
    const val PRESTIGE_TOKEN_MULTIPLIER = 0.25
    const val BONUS_INTERVAL_MS = 8000L
    const val GAME_TICK_MS = 100L
    const val STARTING_MONEY = 50.0
}
