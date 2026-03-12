package com.example.juegoaerolinea.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.juegoaerolinea.data.local.dao.PrestigeDao
import com.example.juegoaerolinea.data.local.dao.StationDao
import com.example.juegoaerolinea.data.local.dao.UpgradeDao
import com.example.juegoaerolinea.data.local.entity.PrestigeEntity
import com.example.juegoaerolinea.data.local.entity.StationEntity
import com.example.juegoaerolinea.data.local.entity.UpgradeEntity

@Database(
    entities = [StationEntity::class, UpgradeEntity::class, PrestigeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun upgradeDao(): UpgradeDao
    abstract fun prestigeDao(): PrestigeDao
}
