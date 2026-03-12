package com.example.juegoaerolinea.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prestige")
data class PrestigeEntity(
    @PrimaryKey val id: Int = 1,
    val tokens: Int = 0,
    val prestigeCount: Int = 0,
    val totalEarned: Double = 0.0
)
