package com.example.juegoaerolinea.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.juegoaerolinea.data.local.entity.PrestigeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestigeDao {
    @Query("SELECT * FROM prestige WHERE id = 1 LIMIT 1")
    fun getPrestige(): Flow<PrestigeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestige(prestige: PrestigeEntity)

    @Update
    suspend fun updatePrestige(prestige: PrestigeEntity)
}
