package com.example.juegoaerolinea.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.juegoaerolinea.data.local.entity.UpgradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UpgradeDao {
    @Query("SELECT * FROM upgrades")
    fun getAllUpgrades(): Flow<List<UpgradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(upgrades: List<UpgradeEntity>)

    @Update
    suspend fun updateUpgrade(upgrade: UpgradeEntity)

    @Query("DELETE FROM upgrades")
    suspend fun deleteAll()
}
