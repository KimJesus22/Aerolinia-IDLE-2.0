package com.example.juegoaerolinea.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.domain.model.CharacterState
import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.ui.screens.game.FloatingBonus
import com.example.juegoaerolinea.ui.theme.*
import kotlin.math.sin

// === COLORES PREALLOCADOS ===
private val gridStrokeColor = Color(0x15000000)
private val runwayDashColor = Color.White.copy(alpha = 0.8f)
private val runwayEdgeColor = Color.Yellow.copy(alpha = 0.6f)
private val shadowColor = Color.Black.copy(alpha = 0.35f)
private val captainHatVisera = CaptainHat.copy(alpha = 0.9f)
private val bonusGlowColor = AccentGreen.copy(alpha = 0.25f)
private val planeWingColor = Color(0xFFB0BEC5)
private val lockBodyColor = Color(0xFFDAA520)
private val lockShackleColor = Color(0xFFB8860B)
private val stationShadowColor = Color.Black.copy(alpha = 0.3f)
private val barBgColor = Color.Black.copy(alpha = 0.3f)
private val barReadyColor = Color(0xFF4CAF50)
private val barFillColor = Color(0xFF81C784)

// Tiers de color para estaciones construidas
private val tierBasicGrad = listOf(Color(0xFF455A64), Color(0xFF37474F))
private val tierBlueGrad = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
private val tierGoldGrad = listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
private val tierMaxGrad = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
private val tierBasicBorder = Color(0xFF78909C)
private val tierBlueBorder = Color(0xFF64B5F6)
private val tierGoldBorder = Color(0xFFFFD700)
private val tierMaxBorder = Color(0xFFFFE082)

// Strokes
private val gridStroke = Stroke(width = 1f)
private val smileStroke = Stroke(width = 1.5f, cap = StrokeCap.Round)
private val thinBorderStroke = Stroke(width = 2f)
private val thickBorderStroke = Stroke(width = 3f)
private val lockShackleStroke = Stroke(width = 3f, cap = StrokeCap.Round)

@Composable
fun MapCanvas(
    characterState: CharacterState,
    stations: List<Station>,
    floatingBonuses: List<FloatingBonus>,
    airplaneX: Float,
    bonusProgress: Float,
    textMeasurer: TextMeasurer,
    onMapTap: (Float, Float) -> Unit,
    onStationTap: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    // IDs de estaciones con bono disponible
    val bonusStationIds = remember(floatingBonuses) {
        floatingBonuses.map { it.stationId }.toSet()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val clickedStation = stations.find {
                        val distance = Math.hypot(
                            (it.x - offset.x).toDouble(),
                            (it.y - offset.y).toDouble()
                        )
                        distance < 80f
                    }
                    if (clickedStation != null) {
                        onStationTap(clickedStation)
                    } else {
                        onMapTap(offset.x, offset.y)
                    }
                }
            }
            // === CAPA 1: FONDO ESTÁTICO (cacheado por tamaño) ===
            .drawWithCache {
                val w = size.width
                val h = size.height
                val tileSize = 120f
                val tilesX = (w / tileSize).toInt() + 2
                val tilesY = (h / tileSize).toInt() + 2

                onDrawBehind {
                    for (x in 0 until tilesX) {
                        for (y in 0 until tilesY) {
                            val color = if ((x + y) % 2 == 0) TileLight else TileDark
                            val topLeft = Offset(x * tileSize, y * tileSize)
                            val sz = Size(tileSize, tileSize)
                            drawRect(color, topLeft, sz)
                            drawRect(gridStrokeColor, topLeft, sz, style = gridStroke)
                        }
                    }
                    val runwayY = h - 100f; val runwayH = 80f
                    drawRect(RunwayColor, Offset(0f, runwayY), Size(w, runwayH))
                    var dashX = 10f
                    while (dashX < w) {
                        drawRect(runwayDashColor, Offset(dashX, runwayY + runwayH / 2 - 2f), Size(40f, 4f))
                        dashX += 70f
                    }
                    drawLine(runwayEdgeColor, Offset(0f, runwayY + 4f), Offset(w, runwayY + 4f), 3f)
                    drawLine(runwayEdgeColor, Offset(0f, runwayY + runwayH - 4f), Offset(w, runwayY + runwayH - 4f), 3f)
                }
            }
    ) {
        // === CAPA 2+3: ESTACIONES + DINÁMICOS ===
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDecorations(textMeasurer)

            stations.forEach { station ->
                val hasBonus = bonusStationIds.contains(station.id)
                drawStation(station, bonusProgress, hasBonus, textMeasurer)
            }

            drawFlyingAirplane(airplaneX)
            drawCharacter(characterState, textMeasurer)

            floatingBonuses.forEach { bonus ->
                drawFloatingBonus(bonus, textMeasurer)
            }
        }
    }
}

// === DECORACIONES ===
private fun DrawScope.drawDecorations(textMeasurer: TextMeasurer) {
    val w = size.width
    val pStyle = TextStyle(fontSize = 28.sp)
    val sStyle = TextStyle(fontSize = 20.sp)
    drawText(textMeasurer, "🌴", Offset(35f, 135f), style = pStyle)
    drawText(textMeasurer, "🌴", Offset(w - 65f, 135f), style = pStyle)
    drawText(textMeasurer, "🌴", Offset(35f, 435f), style = pStyle)
    drawText(textMeasurer, "🌴", Offset(w - 65f, 435f), style = pStyle)
    drawText(textMeasurer, "💺", Offset(120f, 350f), style = sStyle)
    drawText(textMeasurer, "💺", Offset(160f, 350f), style = sStyle)
    drawText(textMeasurer, "🧳", Offset(w - 120f, 350f), style = sStyle)
}

// === ESTACIONES ===
private fun DrawScope.drawStation(
    station: Station,
    bonusProgress: Float,
    hasBonus: Boolean,
    textMeasurer: TextMeasurer
) {
    val isActive = station.isUnlocked && station.level > 0
    if (isActive) {
        drawBuiltStation(station, bonusProgress, hasBonus, textMeasurer)
    } else {
        drawLockedStation(station, textMeasurer)
    }
}

// ==============================
// ESTACIÓN BLOQUEADA (nivel 0)
// ==============================
private fun DrawScope.drawLockedStation(station: Station, textMeasurer: TextMeasurer) {
    val s = 100f
    val left = station.x - s / 2
    val top = station.y - s / 2
    val stationRect = Size(s, s)
    val corner = CornerRadius(12f)

    // Fondo gris con 60% opacidad
    drawRoundRect(
        color = Color(0xFF2A2A2A).copy(alpha = 0.6f),
        topLeft = Offset(left, top),
        size = stationRect,
        cornerRadius = corner
    )
    drawRoundRect(
        color = Color(0xFF444444),
        topLeft = Offset(left, top),
        size = stationRect,
        cornerRadius = corner,
        style = thinBorderStroke
    )

    // *** CANDADO DORADO dibujado con Canvas ***
    val lockCx = station.x
    val lockCy = station.y - 8f

    // Arco del candado (shackle)
    drawArc(
        color = lockShackleColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(lockCx - 10f, lockCy - 18f),
        size = Size(20f, 20f),
        style = lockShackleStroke
    )
    // Barras verticales del shackle
    drawLine(lockShackleColor, Offset(lockCx - 10f, lockCy - 8f), Offset(lockCx - 10f, lockCy), 3f)
    drawLine(lockShackleColor, Offset(lockCx + 10f, lockCy - 8f), Offset(lockCx + 10f, lockCy), 3f)

    // Cuerpo del candado
    drawRoundRect(
        color = lockBodyColor,
        topLeft = Offset(lockCx - 14f, lockCy),
        size = Size(28f, 22f),
        cornerRadius = CornerRadius(4f)
    )
    // Cerradura (círculo oscuro)
    drawCircle(
        color = Color(0xFF8B6914),
        radius = 4f,
        center = Offset(lockCx, lockCy + 10f)
    )
    // Ranura de llave
    drawLine(
        color = Color(0xFF6B4F10),
        start = Offset(lockCx, lockCy + 10f),
        end = Offset(lockCx, lockCy + 16f),
        strokeWidth = 2f
    )

    // Precio en amarillo debajo
    val priceText = "\$${station.baseCost.toInt()}"
    drawText(
        textMeasurer, priceText,
        Offset(station.x - 25f, station.y + 28f),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold
        )
    )
}

// ==============================
// ESTACIÓN CONSTRUIDA (nivel ≥ 1)
// ==============================
private fun DrawScope.drawBuiltStation(
    station: Station,
    bonusProgress: Float,
    hasBonus: Boolean,
    textMeasurer: TextMeasurer
) {
    val s = 100f
    val left = station.x - s / 2
    val top = station.y - s / 2
    val stationRect = Size(s, s)
    val corner = CornerRadius(12f)
    val level = station.level

    // Colores por tier
    val tierGrad = when {
        level >= 20 -> tierMaxGrad
        level >= 10 -> tierGoldGrad
        level >= 5 -> tierBlueGrad
        else -> tierBasicGrad
    }
    val borderColor = when {
        level >= 20 -> tierMaxBorder
        level >= 10 -> tierGoldBorder
        level >= 5 -> tierBlueBorder
        else -> tierBasicBorder
    }
    val borderStroke = if (level >= 10) thickBorderStroke else thinBorderStroke

    // Sombra
    drawRoundRect(
        stationShadowColor,
        Offset(left + 3f, top + 3f),
        stationRect, corner
    )

    // Fondo con gradiente por tier
    drawRoundRect(
        brush = Brush.verticalGradient(tierGrad, startY = top, endY = top + s),
        topLeft = Offset(left, top),
        size = stationRect,
        cornerRadius = corner
    )

    // Borde por tier
    drawRoundRect(borderColor, Offset(left, top), stationRect, corner, style = borderStroke)

    // Glow para nivel 10+ (brillo dorado sutil)
    if (level >= 10) {
        drawRoundRect(
            borderColor.copy(alpha = 0.2f),
            Offset(left - 4f, top - 4f),
            Size(s + 8f, s + 8f),
            CornerRadius(16f)
        )
    }

    // Glow animado brillante para nivel 20+ (pulso)
    if (level >= 20) {
        val pulse = (sin(System.nanoTime() / 300_000_000.0).toFloat() * 0.15f + 0.15f)
        drawRoundRect(
            tierMaxBorder.copy(alpha = pulse),
            Offset(left - 6f, top - 6f),
            Size(s + 12f, s + 12f),
            CornerRadius(18f)
        )
    }

    // Emoji de la estación (un poco más grande para nivel 5+)
    val emojiSize = when {
        level >= 5 -> 36.sp
        else -> 32.sp
    }
    drawText(
        textMeasurer, station.emoji,
        Offset(station.x - 18f, station.y - 25f),
        style = TextStyle(fontSize = emojiSize)
    )

    // === BADGE DE NIVEL (esquina superior derecha) ===
    val badgeR = 13f
    val badgeCx = left + s - badgeR - 3f
    val badgeCy = top + badgeR + 3f
    val badgeColor = when {
        level >= 20 -> Color(0xFFFF6F00)
        level >= 10 -> Color(0xFFE65100)
        level >= 5 -> Color(0xFF1565C0)
        else -> Color(0xFF1B5E20)
    }
    drawCircle(badgeColor, badgeR, Offset(badgeCx, badgeCy))
    drawCircle(Color.White.copy(alpha = 0.2f), badgeR - 2f, Offset(badgeCx, badgeCy))
    drawText(
        textMeasurer, "$level",
        Offset(badgeCx - if (level >= 10) 9f else 5f, badgeCy - 8f),
        style = TextStyle(
            fontSize = if (level >= 10) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    )

    // === BARRA DE PROGRESO DE RECOLECCIÓN ===
    val barWidth = s - 16f
    val barHeight = 5f
    val barY = top + s - 12f
    drawRoundRect(barBgColor, Offset(left + 8f, barY), Size(barWidth, barHeight), CornerRadius(2f))
    val fillColor = if (hasBonus) barReadyColor else barFillColor
    val fillWidth = if (hasBonus) barWidth else barWidth * bonusProgress
    drawRoundRect(fillColor, Offset(left + 8f, barY), Size(fillWidth, barHeight), CornerRadius(2f))

    // === INDICADOR DE BONO LISTO ===
    if (hasBonus) {
        val bobOffset = sin(System.nanoTime() / 200_000_000.0).toFloat() * 4f
        val bonusCx = station.x
        val bonusCy = top - 14f + bobOffset

        // Glow verde
        drawCircle(
            AccentGreen.copy(alpha = 0.3f),
            radius = 20f,
            center = Offset(bonusCx, bonusCy)
        )
        // Círculo verde
        drawCircle(
            barReadyColor,
            radius = 14f,
            center = Offset(bonusCx, bonusCy)
        )
        // "$" en el bonus
        drawText(
            textMeasurer, "$",
            Offset(bonusCx - 6f, bonusCy - 9f),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

// === AVIÓN ANIMADO ===
private fun DrawScope.drawFlyingAirplane(airplaneX: Float) {
    val planeY = size.height - 85f
    drawRoundRect(Color.White, Offset(airplaneX, planeY), Size(50f, 14f), CornerRadius(7f))
    val wt = Path().apply {
        moveTo(airplaneX + 15f, planeY + 7f); lineTo(airplaneX + 10f, planeY - 12f)
        lineTo(airplaneX + 35f, planeY - 12f); lineTo(airplaneX + 30f, planeY + 7f); close()
    }
    drawPath(wt, planeWingColor)
    val wb = Path().apply {
        moveTo(airplaneX + 15f, planeY + 7f); lineTo(airplaneX + 10f, planeY + 26f)
        lineTo(airplaneX + 35f, planeY + 26f); lineTo(airplaneX + 30f, planeY + 7f); close()
    }
    drawPath(wb, planeWingColor)
    val tail = Path().apply {
        moveTo(airplaneX, planeY + 7f); lineTo(airplaneX - 10f, planeY - 8f)
        lineTo(airplaneX + 5f, planeY); close()
    }
    drawPath(tail, AccentSkyBlue)
    for (i in 0..3) drawCircle(AccentSkyBlue, 2f, Offset(airplaneX + 18f + i * 7f, planeY + 5f))
    drawCircle(AccentRed, 3f, Offset(airplaneX + 50f, planeY + 7f))
}

// === PERSONAJE ===
private fun DrawScope.drawCharacter(state: CharacterState, textMeasurer: TextMeasurer) {
    val cx = state.x; val cy = state.y

    drawText(textMeasurer, "CAPITÁN", Offset(cx - 28f, cy - 62f),
        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentGold))
    drawOval(shadowColor, Offset(cx - 18f, cy + 38f), Size(36f, 12f))

    // Gorra
    drawRoundRect(CaptainHat, Offset(cx - 18f, cy - 38f), Size(36f, 14f), CornerRadius(6f))
    drawRoundRect(captainHatVisera, Offset(cx - 22f, cy - 26f), Size(44f, 5f), CornerRadius(2f))
    drawRect(CaptainHatStripe, Offset(cx - 16f, cy - 30f), Size(32f, 3f))

    // Cabeza
    drawCircle(CaptainSkin, 13f, Offset(cx, cy - 14f))
    drawCircle(Color.Black, 2f, Offset(cx - 5f, cy - 16f))
    drawCircle(Color.Black, 2f, Offset(cx + 5f, cy - 16f))
    drawArc(Color.Black, 10f, 160f, false, Offset(cx - 5f, cy - 12f), Size(10f, 6f), style = smileStroke)

    // Cuerpo
    drawRoundRect(CaptainUniform, Offset(cx - 14f, cy), Size(28f, 30f), CornerRadius(4f))
    for (i in 0..2) drawCircle(AccentGold, 1.5f, Offset(cx, cy + 6f + i * 8f))

    // Corbata
    val tie = Path().apply {
        moveTo(cx - 3f, cy + 1f); lineTo(cx + 3f, cy + 1f)
        lineTo(cx + 1.5f, cy + 14f); lineTo(cx - 1.5f, cy + 14f); close()
    }
    drawPath(tie, CaptainTie)

    // Piernas
    val leg = if (state.isMoving) {
        when (state.animationFrame) { 0 -> 0f; 1 -> -6f; 2 -> 0f; 3 -> 6f; else -> 0f }
    } else 0f
    drawRoundRect(CaptainLegs, Offset(cx - 10f, cy + 30f + leg), Size(8f, 16f), CornerRadius(2f))
    drawRoundRect(CaptainLegs, Offset(cx + 2f, cy + 30f - leg), Size(8f, 16f), CornerRadius(2f))
    drawRoundRect(Color.Black, Offset(cx - 11f, cy + 44f + leg), Size(10f, 4f), CornerRadius(2f))
    drawRoundRect(Color.Black, Offset(cx + 1f, cy + 44f - leg), Size(10f, 4f), CornerRadius(2f))
}

// === BONOS FLOTANTES ===
private fun DrawScope.drawFloatingBonus(bonus: FloatingBonus, textMeasurer: TextMeasurer) {
    drawCircle(bonusGlowColor, 25f, Offset(bonus.x + 15f, bonus.y + 10f))
    drawText(textMeasurer, "$", Offset(bonus.x, bonus.y),
        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentGreen))
}
