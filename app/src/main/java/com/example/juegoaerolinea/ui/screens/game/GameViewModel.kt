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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

data class GameUiState(
    val money: Double = 0.0,
    val stations: List<Station> = emptyList(),
    val upgrades: List<Upgrade> = emptyList(),
    val character: CharacterState = CharacterState(x = 500f, y = 500f, targetX = 500f, targetY = 500f),
    val floatingBonuses: List<FloatingBonus> = emptyList(),
    val prestige: PrestigeEntity = PrestigeEntity(),
    val totalEarnPerSec: Double = 0.0,
    val offlineEarnings: Double = 0.0,
    val showOfflineDialog: Boolean = false,
    val showPrestigeScreen: Boolean = false,
    val showUpgradesSheet: Boolean = false,
    val airplaneX: Float = -100f
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
        private const val CHAR_SPEED = 300f       // px/segundo (normalizado a deltaTime)
        private const val AIRPLANE_SPEED = 240f    // px/segundo
        private const val BONUS_INTERVAL = 8f      // segundos
        private const val SAVE_INTERVAL = 30f      // segundos
    }

    private val _characterState = MutableStateFlow(CharacterState(x = 500f, y = 500f, targetX = 500f, targetY = 500f))
    private val _floatingBonuses = MutableStateFlow<List<FloatingBonus>>(emptyList())
    private val _offlineEarnings = MutableStateFlow(0.0)
    private val _showOfflineDialog = MutableStateFlow(false)
    private val _showPrestigeScreen = MutableStateFlow(false)
    private val _showUpgradesSheet = MutableStateFlow(false)
    private val _airplaneX = MutableStateFlow(-100f)

    // Timers acumulativos (en segundos)
    private var bonusTimer = 0f
    private var saveTimer = 0f

    val uiState: StateFlow<GameUiState> = combine(
        repository.playerMoney,
        repository.getAllStations(),
        repository.getAllUpgrades(),
        repository.getPrestige(),
        _characterState
    ) { money, stations, upgrades, prestige, character ->
        val prestigeData = prestige ?: PrestigeEntity()
        val prestigeMultiplier = 1.0 + prestigeData.tokens * Constants.PRESTIGE_TOKEN_MULTIPLIER
        val fuelMultiplier = upgrades.find { it.id == "fuel" }?.getMultiplier() ?: 1.0
        val trainingMultiplier = upgrades.find { it.id == "training" }?.getMultiplier() ?: 1.0

        val totalEarnPerSec = stations.sumOf { it.currentEarnPerSec } *
            fuelMultiplier * trainingMultiplier * prestigeMultiplier

        GameUiState(
            money = money,
            stations = stations,
            upgrades = upgrades,
            character = character,
            floatingBonuses = _floatingBonuses.value,
            prestige = prestigeData,
            totalEarnPerSec = totalEarnPerSec,
            offlineEarnings = _offlineEarnings.value,
            showOfflineDialog = _showOfflineDialog.value,
            showPrestigeScreen = _showPrestigeScreen.value,
            showUpgradesSheet = _showUpgradesSheet.value,
            airplaneX = _airplaneX.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameUiState()
    )

    init {
        Log.d(TAG, "GameViewModel init: inicializando base de datos")
        viewModelScope.launch {
            repository.initStationsDb()
            repository.initUpgradesDb()
            repository.initPrestigeDb()
            Log.d(TAG, "GameViewModel init: DB inicializada, calculando offline earnings")
            calculateOfflineEarnings()
        }
        // Ya NO lanzamos game loop aquí. Se llama desde LaunchedEffect(withFrameNanos)
    }

    /**
     * Llamado desde LaunchedEffect(withFrameNanos) en GameScreen.
     * deltaTime está en segundos (ej: ~0.0167 para 60 FPS).
     */
    fun updateGame(deltaTime: Float) {
        // Limitar deltaTime para evitar saltos grandes (ej: cuando la app vuelve del background)
        val dt = deltaTime.coerceAtMost(0.1f)

        // 1. Mover personaje
        updateCharacterMovement(dt)

        // 2. Animar avión
        updateAirplane(dt)

        // 3. Ganancias idle
        generateIdleIncome(dt)

        // 4. Timer de bonos
        bonusTimer += dt
        if (bonusTimer >= BONUS_INTERVAL) {
            bonusTimer = 0f
            generateBonuses()
        }

        // 5. Autoguardado cada 30 segundos
        saveTimer += dt
        if (saveTimer >= SAVE_INTERVAL) {
            saveTimer = 0f
            viewModelScope.launch {
                repository.saveSessionTimestamp(System.currentTimeMillis())
            }
        }
    }

    private suspend fun calculateOfflineEarnings() {
        val lastTimestamp = repository.getLastSessionTimestamp()
        if (lastTimestamp > 0) {
            val nowMs = System.currentTimeMillis()
            val elapsedSeconds = (nowMs - lastTimestamp) / 1000.0
            if (elapsedSeconds > 5) {
                val stations = repository.getAllStations().first()
                val upgrades = repository.getAllUpgrades().first()
                val prestige = repository.getPrestige().first() ?: PrestigeEntity()

                val prestigeMultiplier = 1.0 + prestige.tokens * Constants.PRESTIGE_TOKEN_MULTIPLIER
                val fuelMultiplier = upgrades.find { it.id == "fuel" }?.getMultiplier() ?: 1.0
                val trainingMultiplier = upgrades.find { it.id == "training" }?.getMultiplier() ?: 1.0
                val marketingMultiplier = upgrades.find { it.id == "marketing" }?.getMultiplier() ?: 1.0

                val baseEarnPerSec = stations.sumOf { it.currentEarnPerSec } *
                    fuelMultiplier * trainingMultiplier * prestigeMultiplier

                val offlineEarnings = baseEarnPerSec * elapsedSeconds *
                    Constants.OFFLINE_EFFICIENCY * marketingMultiplier

                if (offlineEarnings > 0) {
                    Log.d(TAG, "offlineEarnings: $offlineEarnings (elapsed=${elapsedSeconds}s)")
                    _offlineEarnings.value = offlineEarnings
                    _showOfflineDialog.value = true
                    val currentMoney = repository.playerMoney.first()
                    repository.updateMoney(currentMoney + offlineEarnings)
                }
            }
        }
        repository.saveSessionTimestamp(System.currentTimeMillis())
    }

    private fun updateAirplane(dt: Float) {
        _airplaneX.update { currentX ->
            val newX = currentX + AIRPLANE_SPEED * dt
            if (newX > 1200f) -100f else newX
        }
    }

    private fun updateCharacterMovement(dt: Float) {
        val currentState = _characterState.value
        if (!currentState.isMoving) return

        val moveDistance = CHAR_SPEED * dt
        val dx = currentState.targetX - currentState.x
        val dy = currentState.targetY - currentState.y
        val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

        if (distance <= moveDistance) {
            _characterState.update {
                it.copy(x = it.targetX, y = it.targetY, isMoving = false)
            }
            checkBonusCollection(currentState.targetX, currentState.targetY)
        } else {
            val ratio = moveDistance / distance
            val newX = currentState.x + dx * ratio
            val newY = currentState.y + dy * ratio

            val newDirection = if (abs(dx) > abs(dy)) {
                if (dx > 0) CharacterDirection.RIGHT else CharacterDirection.LEFT
            } else {
                if (dy > 0) CharacterDirection.DOWN else CharacterDirection.UP
            }

            // Animación: cambiar frame cada ~0.1s
            val newFrame = ((currentState.animationFrame + (dt * 15f).toInt()) % 4)

            _characterState.update {
                it.copy(
                    x = newX,
                    y = newY,
                    direction = newDirection,
                    animationFrame = newFrame
                )
            }
        }
    }

    private fun generateIdleIncome(dt: Float) {
        val state = uiState.value
        if (state.totalEarnPerSec > 0) {
            val earnThisFrame = state.totalEarnPerSec * dt
            viewModelScope.launch {
                repository.updateMoney(state.money + earnThisFrame)
                val prestige = state.prestige
                repository.updatePrestige(prestige.copy(totalEarned = prestige.totalEarned + earnThisFrame))
            }
        }
    }

    private fun generateBonuses() {
        val activeStations = uiState.value.stations.filter { it.isUnlocked && it.level > 0 }
        val newBonuses = activeStations.map { station ->
            FloatingBonus(
                id = java.util.UUID.randomUUID().toString(),
                x = station.x,
                y = station.y - 50f,
                amount = station.currentEarnPerSec * 5,
                stationId = station.id
            )
        }
        _floatingBonuses.value = newBonuses
    }

    private fun checkBonusCollection(x: Float, y: Float) {
        val collectionRadius = 100f
        val bonuses = _floatingBonuses.value

        val collectedBonuses = bonuses.filter { bonus ->
            Math.hypot((bonus.x - x).toDouble(), (bonus.y - y).toDouble()) <= collectionRadius
        }

        if (collectedBonuses.isNotEmpty()) {
            val state = uiState.value
            val loungeMultiplier = state.upgrades.find { it.id == "lounge" }?.getMultiplier() ?: 1.0
            val totalBonus = collectedBonuses.sumOf { it.amount } * loungeMultiplier

            viewModelScope.launch {
                repository.updateMoney(state.money + totalBonus)
            }

            _floatingBonuses.update { list ->
                list.filterNot { collectedBonuses.contains(it) }
            }
        }
    }

    fun onMapClicked(x: Float, y: Float) {
        Log.d(TAG, "onMapClicked: x=$x, y=$y")
        _characterState.update {
            it.copy(targetX = x, targetY = y, isMoving = true)
        }
    }

    fun onStationTapped(stationId: String) {
        val state = uiState.value
        val station = state.stations.find { it.id == stationId }
        Log.d(TAG, "onStationTapped: id=$stationId, level=${station?.level}, " +
            "unlocked=${station?.isUnlocked}, cost=${station?.upgradeCost}, money=${state.money}")
    }

    fun upgradeStation(stationId: String) {
        val state = uiState.value
        val station = state.stations.find { it.id == stationId }

        if (station == null) {
            Log.e(TAG, "upgradeStation FAILED: '$stationId' not found")
            return
        }

        val cost = station.upgradeCost
        val canBuy = state.money >= cost

        Log.d(TAG, "upgradeStation: id=$stationId, level=${station.level}, " +
            "cost=$cost, money=${state.money}, canBuy=$canBuy")

        if (canBuy) {
            val newLevel = station.level + 1
            viewModelScope.launch {
                repository.updateMoney(state.money - cost)
                repository.saveStation(station.copy(level = newLevel, isUnlocked = true))
                Log.d(TAG, "upgradeStation SUCCESS: newLevel=$newLevel, newMoney=${state.money - cost}")
            }
        } else {
            Log.d(TAG, "upgradeStation FAILED: not enough money")
        }
    }

    fun purchaseUpgrade(upgradeId: String) {
        val state = uiState.value
        val upgrade = state.upgrades.find { it.id == upgradeId } ?: return

        if (!upgrade.isMaxLevel && state.money >= upgrade.upgradeCost) {
            viewModelScope.launch {
                repository.updateMoney(state.money - upgrade.upgradeCost)
                repository.saveUpgrade(upgrade.copy(level = upgrade.level + 1))
            }
        }
    }

    fun canPrestige(): Boolean = uiState.value.prestige.totalEarned >= Constants.PRESTIGE_THRESHOLD

    fun getPrestigeTokensToEarn(): Int {
        val totalEarned = uiState.value.prestige.totalEarned
        return floor(sqrt(totalEarned / Constants.PRESTIGE_THRESHOLD)).toInt()
    }

    fun performPrestige() {
        if (!canPrestige()) return
        val state = uiState.value
        val tokensToEarn = getPrestigeTokensToEarn()

        viewModelScope.launch {
            repository.updatePrestige(
                state.prestige.copy(
                    tokens = state.prestige.tokens + tokensToEarn,
                    prestigeCount = state.prestige.prestigeCount + 1,
                    totalEarned = 0.0
                )
            )
            repository.performPrestigeReset()
            _showPrestigeScreen.value = false
        }
    }

    fun dismissOfflineDialog() { _showOfflineDialog.value = false }
    fun togglePrestigeScreen() { _showPrestigeScreen.update { !it } }
    fun toggleUpgradesSheet() { _showUpgradesSheet.update { !it } }

    fun saveSession() {
        viewModelScope.launch {
            repository.saveSessionTimestamp(System.currentTimeMillis())
        }
    }
}
