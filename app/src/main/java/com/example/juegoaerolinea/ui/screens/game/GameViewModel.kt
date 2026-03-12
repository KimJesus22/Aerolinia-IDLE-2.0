package com.example.juegoaerolinea.ui.screens.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juegoaerolinea.data.local.entity.PrestigeEntity
import com.example.juegoaerolinea.data.repository.GameRepository
import com.example.juegoaerolinea.domain.model.CharacterDirection
import com.example.juegoaerolinea.domain.model.CharacterState
import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.domain.model.Upgrade
import com.example.juegoaerolinea.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

// === ESTADOS SEPARADOS POR FRECUENCIA ===

/** Alta frecuencia — cambia cada frame (personaje, avión, bonos) */
data class DynamicState(
    val character: CharacterState = CharacterState(x = 500f, y = 500f, targetX = 500f, targetY = 500f),
    val airplaneX: Float = -100f,
    val floatingBonuses: List<FloatingBonus> = emptyList(),
    val bonusProgress: Float = 0f // 0..1 progreso hacia el próximo bono
)

/** Media frecuencia — cambia con income ticks (~1/seg visible) */
data class EconomyState(
    val money: Double = 0.0,
    val totalEarnPerSec: Double = 0.0
)

/** Baja frecuencia — cambia solo con acciones del jugador */
data class BuildingState(
    val stations: List<Station> = emptyList(),
    val upgrades: List<Upgrade> = emptyList(),
    val prestige: PrestigeEntity = PrestigeEntity()
)

/** Flags de UI — cambian al interactuar */
data class UiFlags(
    val offlineEarnings: Double = 0.0,
    val showOfflineDialog: Boolean = false,
    val showPrestigeScreen: Boolean = false,
    val showUpgradesSheet: Boolean = false
)

data class FloatingBonus(
    val id: String,
    val x: Float,
    val y: Float,
    val amount: Double,
    val stationId: String
)

class GameViewModel(
    private val repository: GameRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GAME_DEBUG"
        private const val CHAR_SPEED = 300f
        private const val AIRPLANE_SPEED = 240f
        private const val BONUS_INTERVAL = 8f
        private const val SAVE_INTERVAL = 30f
    }

    // === ALTA FRECUENCIA (cada frame) ===
    private val _dynamicState = MutableStateFlow(DynamicState())
    val dynamicState: StateFlow<DynamicState> = _dynamicState.asStateFlow()

    // === MEDIA FRECUENCIA (income) ===
    val economyState: StateFlow<EconomyState> = combine(
        repository.playerMoney,
        repository.getAllStations(),
        repository.getAllUpgrades(),
        repository.getPrestige()
    ) { money, stations, upgrades, prestige ->
        val prestigeData = prestige ?: PrestigeEntity()
        val prestigeMultiplier = 1.0 + prestigeData.tokens * Constants.PRESTIGE_TOKEN_MULTIPLIER
        val fuelMultiplier = upgrades.find { it.id == "fuel" }?.getMultiplier() ?: 1.0
        val trainingMultiplier = upgrades.find { it.id == "training" }?.getMultiplier() ?: 1.0
        val totalEarnPerSec = stations.sumOf { it.currentEarnPerSec } *
            fuelMultiplier * trainingMultiplier * prestigeMultiplier

        EconomyState(money = money, totalEarnPerSec = totalEarnPerSec)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EconomyState())

    // === BAJA FRECUENCIA (acciones del jugador) ===
    val buildingState: StateFlow<BuildingState> = combine(
        repository.getAllStations(),
        repository.getAllUpgrades(),
        repository.getPrestige()
    ) { stations, upgrades, prestige ->
        BuildingState(
            stations = stations,
            upgrades = upgrades,
            prestige = prestige ?: PrestigeEntity()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BuildingState())

    // === FLAGS DE UI ===
    private val _uiFlags = MutableStateFlow(UiFlags())
    val uiFlags: StateFlow<UiFlags> = _uiFlags.asStateFlow()

    // Timers
    private var bonusTimer = 0f
    private var saveTimer = 0f

    init {
        Log.d(TAG, "GameViewModel init")
        viewModelScope.launch {
            repository.initStationsDb()
            repository.initUpgradesDb()
            repository.initPrestigeDb()
            calculateOfflineEarnings()
        }
    }

    /** Llamado desde withFrameNanos en GameScreen. deltaTime en segundos. */
    fun updateGame(deltaTime: Float) {
        val dt = deltaTime.coerceAtMost(0.1f)

        // Alta frecuencia: personaje + avión + progreso de bono
        _dynamicState.update { dyn ->
            val newChar = moveCharacter(dyn.character, dt)
            val newPlaneX = let {
                val x = dyn.airplaneX + AIRPLANE_SPEED * dt
                if (x > 1200f) -100f else x
            }
            dyn.copy(
                character = newChar,
                airplaneX = newPlaneX,
                bonusProgress = (bonusTimer / BONUS_INTERVAL).coerceIn(0f, 1f)
            )
        }

        // Media frecuencia: income
        generateIdleIncome(dt)

        // Timers
        bonusTimer += dt
        if (bonusTimer >= BONUS_INTERVAL) {
            bonusTimer = 0f
            generateBonuses()
        }
        saveTimer += dt
        if (saveTimer >= SAVE_INTERVAL) {
            saveTimer = 0f
            viewModelScope.launch { repository.saveSessionTimestamp(System.currentTimeMillis()) }
        }
    }

    private fun moveCharacter(char: CharacterState, dt: Float): CharacterState {
        if (!char.isMoving) return char

        val moveDistance = CHAR_SPEED * dt
        val dx = char.targetX - char.x
        val dy = char.targetY - char.y
        val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

        return if (distance <= moveDistance) {
            // Llegó al destino — revisar bonos
            checkBonusCollection(char.targetX, char.targetY)
            char.copy(x = char.targetX, y = char.targetY, isMoving = false)
        } else {
            val ratio = moveDistance / distance
            val newDir = if (abs(dx) > abs(dy)) {
                if (dx > 0) CharacterDirection.RIGHT else CharacterDirection.LEFT
            } else {
                if (dy > 0) CharacterDirection.DOWN else CharacterDirection.UP
            }
            char.copy(
                x = char.x + dx * ratio,
                y = char.y + dy * ratio,
                direction = newDir,
                animationFrame = (char.animationFrame + (dt * 15f).toInt()) % 4
            )
        }
    }

    private fun generateIdleIncome(dt: Float) {
        val econ = economyState.value
        if (econ.totalEarnPerSec > 0) {
            val earn = econ.totalEarnPerSec * dt
            viewModelScope.launch {
                repository.updateMoney(econ.money + earn)
                val prestige = buildingState.value.prestige
                repository.updatePrestige(prestige.copy(totalEarned = prestige.totalEarned + earn))
            }
        }
    }

    private fun generateBonuses() {
        val activeStations = buildingState.value.stations.filter { it.isUnlocked && it.level > 0 }
        val newBonuses = activeStations.map { station ->
            FloatingBonus(
                id = java.util.UUID.randomUUID().toString(),
                x = station.x, y = station.y - 50f,
                amount = station.currentEarnPerSec * 5,
                stationId = station.id
            )
        }
        _dynamicState.update { it.copy(floatingBonuses = newBonuses) }
    }

    private fun checkBonusCollection(x: Float, y: Float) {
        val bonuses = _dynamicState.value.floatingBonuses
        val collected = bonuses.filter { b ->
            Math.hypot((b.x - x).toDouble(), (b.y - y).toDouble()) <= 100f
        }
        if (collected.isNotEmpty()) {
            val econ = economyState.value
            val loungeMultiplier = buildingState.value.upgrades.find { it.id == "lounge" }?.getMultiplier() ?: 1.0
            val total = collected.sumOf { it.amount } * loungeMultiplier
            viewModelScope.launch { repository.updateMoney(econ.money + total) }
            _dynamicState.update { dyn -> dyn.copy(floatingBonuses = dyn.floatingBonuses.filterNot { collected.contains(it) }) }
        }
    }

    private suspend fun calculateOfflineEarnings() {
        val lastTs = repository.getLastSessionTimestamp()
        if (lastTs > 0) {
            val elapsed = (System.currentTimeMillis() - lastTs) / 1000.0
            if (elapsed > 5) {
                val stations = repository.getAllStations().first()
                val upgrades = repository.getAllUpgrades().first()
                val prestige = repository.getPrestige().first() ?: PrestigeEntity()

                val pm = 1.0 + prestige.tokens * Constants.PRESTIGE_TOKEN_MULTIPLIER
                val fm = upgrades.find { it.id == "fuel" }?.getMultiplier() ?: 1.0
                val tm = upgrades.find { it.id == "training" }?.getMultiplier() ?: 1.0
                val mm = upgrades.find { it.id == "marketing" }?.getMultiplier() ?: 1.0

                val base = stations.sumOf { it.currentEarnPerSec } * fm * tm * pm
                val earnings = base * elapsed * Constants.OFFLINE_EFFICIENCY * mm

                if (earnings > 0) {
                    Log.d(TAG, "offlineEarnings: $earnings")
                    _uiFlags.update { it.copy(offlineEarnings = earnings, showOfflineDialog = true) }
                    val money = repository.playerMoney.first()
                    repository.updateMoney(money + earnings)
                }
            }
        }
        repository.saveSessionTimestamp(System.currentTimeMillis())
    }

    // === ACCIONES DEL JUGADOR ===

    fun onMapClicked(x: Float, y: Float) {
        _dynamicState.update { dyn ->
            dyn.copy(character = dyn.character.copy(targetX = x, targetY = y, isMoving = true))
        }
    }

    fun onStationTapped(stationId: String) {
        val s = buildingState.value.stations.find { it.id == stationId }
        Log.d(TAG, "onStationTapped: id=$stationId, level=${s?.level}, unlocked=${s?.isUnlocked}")
    }

    fun upgradeStation(stationId: String) {
        val building = buildingState.value
        val econ = economyState.value
        val station = building.stations.find { it.id == stationId }

        if (station == null) { Log.e(TAG, "upgradeStation FAILED: not found"); return }

        val cost = station.upgradeCost
        val canBuy = econ.money >= cost
        Log.d(TAG, "upgradeStation: id=$stationId, lvl=${station.level}, cost=$cost, money=${econ.money}, canBuy=$canBuy")

        if (canBuy) {
            viewModelScope.launch {
                repository.updateMoney(econ.money - cost)
                repository.saveStation(station.copy(level = station.level + 1, isUnlocked = true))
                Log.d(TAG, "upgradeStation SUCCESS: newLevel=${station.level + 1}")
            }
        }
    }

    fun purchaseUpgrade(upgradeId: String) {
        val building = buildingState.value
        val econ = economyState.value
        val upgrade = building.upgrades.find { it.id == upgradeId } ?: return
        if (!upgrade.isMaxLevel && econ.money >= upgrade.upgradeCost) {
            viewModelScope.launch {
                repository.updateMoney(econ.money - upgrade.upgradeCost)
                repository.saveUpgrade(upgrade.copy(level = upgrade.level + 1))
            }
        }
    }

    fun canPrestige(): Boolean = buildingState.value.prestige.totalEarned >= Constants.PRESTIGE_THRESHOLD

    fun getPrestigeTokensToEarn(): Int {
        val te = buildingState.value.prestige.totalEarned
        return floor(sqrt(te / Constants.PRESTIGE_THRESHOLD)).toInt()
    }

    fun performPrestige() {
        if (!canPrestige()) return
        val p = buildingState.value.prestige
        val tokens = getPrestigeTokensToEarn()
        viewModelScope.launch {
            repository.updatePrestige(p.copy(
                tokens = p.tokens + tokens,
                prestigeCount = p.prestigeCount + 1,
                totalEarned = 0.0
            ))
            repository.performPrestigeReset()
            _uiFlags.update { it.copy(showPrestigeScreen = false) }
        }
    }

    fun dismissOfflineDialog() { _uiFlags.update { it.copy(showOfflineDialog = false) } }
    fun togglePrestigeScreen() { _uiFlags.update { it.copy(showPrestigeScreen = !it.showPrestigeScreen) } }
    fun toggleUpgradesSheet() { _uiFlags.update { it.copy(showUpgradesSheet = !it.showUpgradesSheet) } }

    fun saveSession() {
        viewModelScope.launch { repository.saveSessionTimestamp(System.currentTimeMillis()) }
    }
}
