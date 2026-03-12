package com.example.juegoaerolinea.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean,
    val level: Int
)
