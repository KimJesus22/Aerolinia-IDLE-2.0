package com.example.juegoaerolinea.ui.screens.game

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.ui.components.HudOverlay
import com.example.juegoaerolinea.ui.components.MapCanvas
import com.example.juegoaerolinea.ui.components.OfflineEarningsDialog
import com.example.juegoaerolinea.ui.components.PrestigeScreen
import com.example.juegoaerolinea.ui.components.UpgradesSheet
import com.example.juegoaerolinea.ui.theme.*
import com.example.juegoaerolinea.util.NumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // === RECOGER CADA FLOW SEPARADO (minimiza recomposiciones) ===
    val dynamic by viewModel.dynamicState.collectAsState()     // Alta freq: personaje, avión, bonos
    val economy by viewModel.economyState.collectAsState()     // Media freq: dinero, $/s
    val building by viewModel.buildingState.collectAsState()   // Baja freq: estaciones, upgrades, prestigio
    val uiFlags by viewModel.uiFlags.collectAsState()          // Flags UI

    val textMeasurer = rememberTextMeasurer()

    var selectedStationId by remember { mutableStateOf<String?>(null) }
    var showStationSheet by remember { mutableStateOf(false) }

    val selectedStation = selectedStationId?.let { id ->
        building.stations.find { it.id == id }
    }

    // === GAME LOOP (withFrameNanos → 60 FPS) ===
    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTime == 0L) {
                    lastFrameTime = frameTimeNanos
                    return@withFrameNanos
                }
                val deltaTime = (frameTimeNanos - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTimeNanos
                viewModel.updateGame(deltaTime)
            }
        }
    }

    // Pantalla de prestigio (overlay completo)
    if (uiFlags.showPrestigeScreen) {
        PrestigeScreen(
            prestige = building.prestige,
            canPrestige = viewModel.canPrestige(),
            tokensToEarn = viewModel.getPrestigeTokensToEarn(),
            onConfirm = { viewModel.performPrestige() },
            onDismiss = { viewModel.togglePrestigeScreen() }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            )
    ) {
        // === MAPA: solo se recompone con dynamic + building ===
        MapCanvas(
            characterState = dynamic.character,
            stations = building.stations,
            floatingBonuses = dynamic.floatingBonuses,
            flyingPlanes = dynamic.flyingPlanes,
            bonusProgress = dynamic.bonusProgress,
            textMeasurer = textMeasurer,
            onMapTap = { x, y -> viewModel.onMapClicked(x, y) },
            onStationTap = { station ->
                Log.d("GAME_DEBUG", "GameScreen: onStationTap id=${station.id}")
                selectedStationId = station.id
                showStationSheet = true
                viewModel.onStationTapped(station.id)
            }
        )

        // === HUD: solo se recompone con economy ===
        HudOverlay(
            money = economy.money,
            earnPerSec = economy.totalEarnPerSec,
            prestigeTokens = building.prestige.tokens,
            canPrestige = viewModel.canPrestige(),
            onUpgradesClick = { viewModel.toggleUpgradesSheet() },
            onPrestigeClick = { viewModel.togglePrestigeScreen() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // Diálogo offline
    if (uiFlags.showOfflineDialog) {
        OfflineEarningsDialog(
            earnings = uiFlags.offlineEarnings,
            onDismiss = { viewModel.dismissOfflineDialog() }
        )
    }

    // === PANEL de estación con graphicsLayer slide-up ===
    if (showStationSheet && selectedStation != null) {
        val station = selectedStation!!
        val canAfford = economy.money >= station.upgradeCost

        ModalBottomSheet(
            onDismissRequest = {
                showStationSheet = false
                selectedStationId = null
            },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkSurface
        ) {
            // graphicsLayer evita recomposición del contenido durante animación
            val panelAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(250),
                label = "panelAlpha"
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = panelAlpha
                }
            ) {
                StationUpgradePanel(
                    station = station,
                    canAfford = canAfford,
                    onUpgradeClick = {
                        Log.d("GAME_DEBUG", "Button clicked → upgradeStation(${station.id})")
                        viewModel.upgradeStation(station.id)
                        showStationSheet = false
                        selectedStationId = null
                    }
                )
            }
        }
    }

    // Bottom sheet mejoras globales
    if (uiFlags.showUpgradesSheet) {
        UpgradesSheet(
            upgrades = building.upgrades,
            money = economy.money,
            onPurchase = { upgradeId -> viewModel.purchaseUpgrade(upgradeId) },
            onDismiss = { viewModel.toggleUpgradesSheet() }
        )
    }
}

@Composable
fun StationUpgradePanel(
    station: Station,
    canAfford: Boolean,
    onUpgradeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${station.emoji} ${station.name}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = if (station.isUnlocked) "Nivel ${station.level}" else "🔒 Bloqueada",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp),
            color = if (station.isUnlocked) AccentSkyBlue else AccentRed
        )

        if (station.isUnlocked) {
            Text(
                text = "Ganancia: ${NumberFormatter.format(station.currentEarnPerSec)}/s",
                color = AccentGreen, fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            val nextEarn = station.baseEarn * (station.level + 1)
            Text(
                text = "Siguiente nivel: ${NumberFormatter.format(nextEarn)}/s",
                color = Color.Gray, fontSize = 13.sp
            )
        } else {
            Text(
                text = "Generará ${NumberFormatter.format(station.baseEarn)}/s al construirla",
                color = Color.Gray, fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = onUpgradeClick,
            enabled = canAfford,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canAfford) AccentGreen else Color(0xFF555555),
                disabledContainerColor = Color(0xFF555555)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            val actionText = if (station.isUnlocked) "⬆ Mejorar" else "🔓 Construir"
            Text(
                text = "$actionText — ${NumberFormatter.format(station.upgradeCost)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
