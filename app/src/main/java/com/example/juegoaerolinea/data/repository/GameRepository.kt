package com.example.juegoaerolinea.data.repository

import com.example.juegoaerolinea.data.local.dao.PrestigeDao
import com.example.juegoaerolinea.data.local.dao.StationDao
import com.example.juegoaerolinea.data.local.dao.UpgradeDao
import com.example.juegoaerolinea.data.local.entity.PrestigeEntity
import com.example.juegoaerolinea.data.local.entity.StationEntity
import com.example.juegoaerolinea.data.local.entity.UpgradeEntity
import com.example.juegoaerolinea.data.preferences.GamePreferences
import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.domain.model.Upgrade
import com.example.juegoaerolinea.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(
    private val stationDao: StationDao,
    private val upgradeDao: UpgradeDao,
    private val prestigeDao: PrestigeDao,
    private val gamePreferences: GamePreferences
) {
    val playerMoney: Flow<Double> = gamePreferences.playerMoneyFlow

    fun getAllStations(): Flow<List<Station>> {
        return stationDao.getAllStations().map { entities ->
            Constants.baseStations.map { baseStation ->
                val entity = entities.find { it.id == baseStation.id }
                if (entity != null) {
                    baseStation.copy(isUnlocked = entity.isUnlocked, level = entity.level)
                } else {
                    baseStation
                }
            }
        }
    }

    fun getAllUpgrades(): Flow<List<Upgrade>> {
        return upgradeDao.getAllUpgrades().map { entities ->
            Constants.baseUpgrades.map { baseUpgrade ->
                val entity = entities.find { it.id == baseUpgrade.id }
                if (entity != null) {
                    baseUpgrade.copy(level = entity.level)
                } else {
                    baseUpgrade
                }
            }
        }
    }

    fun getPrestige(): Flow<PrestigeEntity?> = prestigeDao.getPrestige()

    suspend fun updateMoney(amount: Double) {
        gamePreferences.updateMoney(amount)
    }

    suspend fun saveStation(station: Station) {
        stationDao.upsertStation(StationEntity(id = station.id, isUnlocked = station.isUnlocked, level = station.level))
    }

    suspend fun saveUpgrade(upgrade: Upgrade) {
        upgradeDao.updateUpgrade(UpgradeEntity(id = upgrade.id, level = upgrade.level))
    }

    suspend fun initStationsDb() {
        // Insertar TODAS las 8 estaciones. REPLACE no sobreescribirá
        // datos existentes porque insertAll usa OnConflictStrategy.REPLACE
        // solo cuando no hay fila previa.
        val allStations = Constants.baseStations.map { base ->
            if (base.id == "checkin") {
                StationEntity(base.id, true, 1) // Check-in empieza desbloqueada nivel 1
            } else {
                StationEntity(base.id, false, 0) // Las demás empiezan bloqueadas nivel 0
            }
        }
        stationDao.insertAll(allStations)
    }

    suspend fun initUpgradesDb() {
        val upgrades = Constants.baseUpgrades.map { UpgradeEntity(it.id, 0) }
        upgradeDao.insertAll(upgrades)
    }

    suspend fun initPrestigeDb() {
        prestigeDao.insertPrestige(PrestigeEntity(id = 1, tokens = 0, prestigeCount = 0, totalEarned = 0.0))
    }

    suspend fun updatePrestige(prestige: PrestigeEntity) {
        prestigeDao.updatePrestige(prestige)
    }

    // Offline earnings
    suspend fun getLastSessionTimestamp(): Long = gamePreferences.getLastSessionTimestamp()
    suspend fun saveSessionTimestamp(ts: Long) = gamePreferences.saveSessionTimestamp(ts)

    // Prestige reset
    suspend fun performPrestigeReset() {
        stationDao.deleteAll()
        upgradeDao.deleteAll()
        gamePreferences.resetMoney(Constants.STARTING_MONEY)
        // Re-init with defaults
        initStationsDb()
        initUpgradesDb()
    }
}
