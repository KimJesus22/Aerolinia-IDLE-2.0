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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.domain.model.CharacterDirection
import com.example.juegoaerolinea.domain.model.CharacterState
import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.ui.screens.game.FloatingBonus
import com.example.juegoaerolinea.ui.screens.game.FlyingPlane
import com.example.juegoaerolinea.ui.theme.*
import kotlin.math.sin

// === COLORES PREALLOCADOS ===
private val gridStrokeColor = Color(0x15000000)
private val shadowColor = Color.Black.copy(alpha = 0.25f)
private val captainHatVisera = CaptainHat.copy(alpha = 0.9f)
private val bonusGlowColor = AccentGreen.copy(alpha = 0.25f)
private val planeWingColor = Color(0xFFB0BEC5)
private val lockBodyColor = Color(0xFFDAA520)
private val lockShackleColor = Color(0xFFB8860B)
private val stationShadowColor = Color.Black.copy(alpha = 0.3f)
private val barBgColor = Color.Black.copy(alpha = 0.3f)
private val barReadyColor = Color(0xFF4CAF50)
private val barFillColor = Color(0xFF81C784)

// Runway
private val runwayAsphalt = Color(0xFF2C2C2C)
private val runwayAsphaltLight = Color(0xFF3A3A3A)
private val runwayDashColor = Color.White.copy(alpha = 0.9f)
private val runwayEdgeColor = Color.Yellow.copy(alpha = 0.6f)
private val runwayLightColor = Color(0xFF42A5F5)

// Tiers
private val tierBasicGrad = listOf(Color(0xFF455A64), Color(0xFF37474F))
private val tierBlueGrad = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
private val tierGoldGrad = listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
private val tierMaxGrad = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
private val tierBasicBorder = Color(0xFF78909C)
private val tierBlueBorder = Color(0xFF64B5F6)
private val tierGoldBorder = Color(0xFFFFD700)
private val tierMaxBorder = Color(0xFFFFE082)

// Decorations
private val trunkColor = Color(0xFF795548)
private val leafColor = Color(0xFF2E7D32)
private val leafLightColor = Color(0xFF4CAF50)
private val seatColor = Color(0xFF1565C0)
private val seatBackColor = Color(0xFF0D47A1)
private val luggageColor = Color(0xFF8D6E63)
private val luggageHandleColor = Color(0xFF5D4037)
private val beltColor = Color(0xFF616161)

// Captain
private val hairColor = Color(0xFF5D4037)
private val eyeWhiteColor = Color.White
private val pupilColor = Color(0xFF333333)
private val skinColor = Color(0xFFFFCC80)
private val uniformColor = Color(0xFF1565C0)
private val hatColor = Color(0xFF0D47A1)
private val hatBadgeColor = Color(0xFFFFD54F)
private val tieColor = Color(0xFFE53935)
private val pantColor = Color(0xFF0D3B6E)
private val shoeColor = Color(0xFF212121)

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
    flyingPlanes: List<FlyingPlane>,
    bonusProgress: Float,
    textMeasurer: TextMeasurer,
    onMapTap: (Float, Float) -> Unit,
    onStationTap: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    val bonusStationIds = remember(floatingBonuses) {
        floatingBonuses.map { it.stationId }.toSet()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val clickedStation = stations.find {
                        Math.hypot((it.x - offset.x).toDouble(), (it.y - offset.y).toDouble()) < 80f
                    }
                    if (clickedStation != null) onStationTap(clickedStation)
                    else onMapTap(offset.x, offset.y)
                }
            }
            // === CAPA 1: FONDO ESTÁTICO ===
            .drawWithCache {
                val w = size.width; val h = size.height
                val tileSize = 120f
                val tilesX = (w / tileSize).toInt() + 2
                val tilesY = (h / tileSize).toInt() + 2

                onDrawBehind {
                    // Baldosas
                    for (x in 0 until tilesX) {
                        for (y in 0 until tilesY) {
                            val c = if ((x + y) % 2 == 0) TileLight else TileDark
                            val tl = Offset(x * tileSize, y * tileSize)
                            val sz = Size(tileSize, tileSize)
                            drawRect(c, tl, sz)
                            drawRect(gridStrokeColor, tl, sz, style = gridStroke)
                        }
                    }

                    // === PISTA DE ATERRIZAJE MEJORADA ===
                    val rY = h - 110f; val rH = 90f

                    // Asfalto con textura (dos tonos)
                    drawRect(runwayAsphalt, Offset(0f, rY), Size(w, rH))
                    // Franjas de textura
                    var tx = 0f
                    while (tx < w) {
                        drawRect(runwayAsphaltLight, Offset(tx, rY), Size(30f, rH))
                        tx += 60f
                    }

                    // Líneas blancas discontinuas centro
                    var dashX = 15f
                    while (dashX < w) {
                        drawRoundRect(runwayDashColor, Offset(dashX, rY + rH / 2 - 2.5f), Size(45f, 5f), CornerRadius(2f))
                        dashX += 70f
                    }

                    // Bordes amarillos
                    drawLine(runwayEdgeColor, Offset(0f, rY + 4f), Offset(w, rY + 4f), 3f)
                    drawLine(runwayEdgeColor, Offset(0f, rY + rH - 4f), Offset(w, rY + rH - 4f), 3f)

                    // Luces azules en bordes (circulitos)
                    var lx = 25f
                    while (lx < w) {
                        drawCircle(runwayLightColor, 3f, Offset(lx, rY + 2f))
                        drawCircle(runwayLightColor.copy(alpha = 0.3f), 6f, Offset(lx, rY + 2f)) // glow
                        drawCircle(runwayLightColor, 3f, Offset(lx, rY + rH - 2f))
                        drawCircle(runwayLightColor.copy(alpha = 0.3f), 6f, Offset(lx, rY + rH - 2f))
                        lx += 50f
                    }

                    // Flechas de dirección (→)
                    val arrowY = rY + rH / 2
                    for (ax in listOf(w * 0.2f, w * 0.5f, w * 0.8f)) {
                        val arrow = Path().apply {
                            moveTo(ax - 12f, arrowY - 6f)
                            lineTo(ax + 6f, arrowY)
                            lineTo(ax - 12f, arrowY + 6f)
                        }
                        drawPath(arrow, Color.White.copy(alpha = 0.4f), style = Stroke(width = 2f))
                    }

                    // Avión estacionado (dibujado con Canvas)
                    drawParkedPlane(w * 0.85f, rY + rH / 2)
                }
            }
    ) {
        // === CAPA 2+3: DINÁMICOS ===
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Decoraciones Canvas
            drawCanvasDecorations()

            // Aviones volando con sombra
            flyingPlanes.forEach { plane ->
                drawFlyingPlaneWithShadow(plane)
            }

            // Estaciones
            stations.forEach { station ->
                drawStation(station, bonusProgress, bonusStationIds.contains(station.id), textMeasurer)
            }

            // Personaje
            drawCharacter(characterState, textMeasurer)

            // Bonos flotantes
            floatingBonuses.forEach { bonus ->
                drawFloatingBonus(bonus, textMeasurer)
            }
        }
    }
}

// ============================================================
// DECORACIONES CANVAS (palmeras, sillas, maletas)
// ============================================================
private fun DrawScope.drawCanvasDecorations() {
    val w = size.width

    // 4 palmeras en las esquinas
    drawPalmTree(60f, 160f)
    drawPalmTree(w - 60f, 160f)
    drawPalmTree(60f, 460f)
    drawPalmTree(w - 60f, 460f)

    // Sillas de espera (fila izquierda)
    for (i in 0..2) drawSeat(110f + i * 28f, 365f)

    // Sillas de espera (fila derecha)
    for (i in 0..2) drawSeat(w - 170f + i * 28f, 365f)

    // Maletas
    drawLuggage(w / 2 - 60f, 200f)
    drawLuggage(w / 2 + 40f, 200f)

    // Banda de equipaje
    drawBaggageBelt(w / 2 - 50f, 530f, 100f)
}

private fun DrawScope.drawPalmTree(cx: Float, cy: Float) {
    // Tronco
    drawRoundRect(trunkColor, Offset(cx - 4f, cy - 10f), Size(8f, 35f), CornerRadius(3f))
    // Tronco rayas
    for (i in 0..4) {
        drawLine(Color(0xFF5D4037), Offset(cx - 4f, cy - 6f + i * 7f), Offset(cx + 4f, cy - 6f + i * 7f), 1f)
    }
    // Hojas (arcos verdes)
    val leafPositions = listOf(-40f to -5f, 40f to -5f, -25f to -20f, 25f to -20f, 0f to -28f)
    leafPositions.forEach { (ox, oy) ->
        val leafPath = Path().apply {
            moveTo(cx, cy - 10f)
            quadraticBezierTo(cx + ox * 0.5f, cy + oy - 15f, cx + ox, cy + oy)
            quadraticBezierTo(cx + ox * 0.5f, cy + oy - 8f, cx, cy - 10f)
        }
        drawPath(leafPath, if (ox == 0f) leafLightColor else leafColor)
    }
}

private fun DrawScope.drawSeat(x: Float, y: Float) {
    // Respaldo
    drawRoundRect(seatBackColor, Offset(x, y - 12f), Size(20f, 5f), CornerRadius(2f))
    // Asiento
    drawRoundRect(seatColor, Offset(x, y - 7f), Size(20f, 8f), CornerRadius(2f))
    // Patas
    drawLine(Color(0xFF424242), Offset(x + 3f, y + 1f), Offset(x + 3f, y + 6f), 2f)
    drawLine(Color(0xFF424242), Offset(x + 17f, y + 1f), Offset(x + 17f, y + 6f), 2f)
}

private fun DrawScope.drawLuggage(x: Float, y: Float) {
    // Cuerpo maleta
    drawRoundRect(luggageColor, Offset(x, y), Size(22f, 16f), CornerRadius(3f))
    // Asa
    drawRoundRect(luggageHandleColor, Offset(x + 7f, y - 5f), Size(8f, 6f), CornerRadius(2f),
        style = Stroke(width = 2f))
    // Rueditas
    drawCircle(Color(0xFF424242), 2f, Offset(x + 5f, y + 16f))
    drawCircle(Color(0xFF424242), 2f, Offset(x + 17f, y + 16f))
    // Rayas decorativas
    drawLine(luggageHandleColor, Offset(x + 4f, y + 5f), Offset(x + 18f, y + 5f), 1f)
    drawLine(luggageHandleColor, Offset(x + 4f, y + 10f), Offset(x + 18f, y + 10f), 1f)
}

private fun DrawScope.drawBaggageBelt(x: Float, y: Float, width: Float) {
    // Base de la banda
    drawRoundRect(beltColor, Offset(x, y), Size(width, 14f), CornerRadius(3f))
    // Rodillos
    val time = (System.nanoTime() / 100_000_000) % 20
    for (i in 0..4) {
        val rx = x + 8f + i * 20f + (time % 20) * 1f
        if (rx < x + width - 8f) {
            drawLine(Color(0xFF757575), Offset(rx, y + 2f), Offset(rx, y + 12f), 2f)
        }
    }
    // Bordes metálicos
    drawRoundRect(Color(0xFF9E9E9E), Offset(x, y), Size(width, 14f), CornerRadius(3f),
        style = Stroke(width = 1.5f))
}

// ============================================================
// AVIÓN ESTACIONADO (en la pista)
// ============================================================
private fun DrawScope.drawParkedPlane(cx: Float, cy: Float) {
    // Fuselaje
    drawRoundRect(Color.White, Offset(cx - 30f, cy - 8f), Size(60f, 16f), CornerRadius(8f))
    // Ala
    drawRoundRect(Color(0xFFE0E0E0), Offset(cx - 10f, cy - 22f), Size(20f, 14f), CornerRadius(4f))
    drawRoundRect(Color(0xFFE0E0E0), Offset(cx - 10f, cy + 8f), Size(20f, 14f), CornerRadius(4f))
    // Cola
    val tail = Path().apply {
        moveTo(cx - 30f, cy); lineTo(cx - 42f, cy - 14f); lineTo(cx - 26f, cy - 4f); close()
    }
    drawPath(tail, AccentSkyBlue)
    // Ventanas
    for (i in 0..4) drawCircle(AccentSkyBlue, 2f, Offset(cx - 14f + i * 8f, cy - 2f))
    // Cabina
    drawCircle(Color(0xFF81D4FA), 4f, Offset(cx + 28f, cy))
}

// ============================================================
// AVIONES VOLANDO (con sombra en el suelo)
// ============================================================
private fun DrawScope.drawFlyingPlaneWithShadow(plane: FlyingPlane) {
    val px = plane.x; val py = plane.y; val sc = plane.scale

    // Sombra en el suelo (más abajo y transparente)
    drawOval(
        Color.Black.copy(alpha = 0.1f * sc),
        Offset(px + 20f, py + 200f),
        Size(40f * sc, 10f * sc)
    )

    // Avión escalado
    scale(sc, pivot = Offset(px + 25f, py + 7f)) {
        // Fuselaje
        drawRoundRect(Color.White, Offset(px, py), Size(50f, 14f), CornerRadius(7f))
        // Alas
        val wt = Path().apply {
            moveTo(px + 15f, py + 7f); lineTo(px + 10f, py - 12f)
            lineTo(px + 35f, py - 12f); lineTo(px + 30f, py + 7f); close()
        }
        drawPath(wt, planeWingColor)
        val wb = Path().apply {
            moveTo(px + 15f, py + 7f); lineTo(px + 10f, py + 26f)
            lineTo(px + 35f, py + 26f); lineTo(px + 30f, py + 7f); close()
        }
        drawPath(wb, planeWingColor)
        // Cola
        val tail = Path().apply {
            moveTo(px, py + 7f); lineTo(px - 10f, py - 8f); lineTo(px + 5f, py); close()
        }
        drawPath(tail, AccentSkyBlue)
        // Ventanas + nariz
        for (i in 0..3) drawCircle(AccentSkyBlue, 2f, Offset(px + 18f + i * 7f, py + 5f))
        drawCircle(AccentRed, 3f, Offset(px + 50f, py + 7f))
    }
}

// ============================================================
// ESTACIONES
// ============================================================
private fun DrawScope.drawStation(
    station: Station, bonusProgress: Float, hasBonus: Boolean, textMeasurer: TextMeasurer
) {
    if (station.isUnlocked && station.level > 0) {
        drawBuiltStation(station, bonusProgress, hasBonus, textMeasurer)
    } else {
        drawLockedStation(station, textMeasurer)
    }
}

private fun DrawScope.drawLockedStation(station: Station, textMeasurer: TextMeasurer) {
    val s = 100f; val left = station.x - s / 2; val top = station.y - s / 2
    val corner = CornerRadius(12f)

    drawRoundRect(Color(0xFF2A2A2A).copy(alpha = 0.6f), Offset(left, top), Size(s, s), corner)
    drawRoundRect(Color(0xFF444444), Offset(left, top), Size(s, s), corner, style = thinBorderStroke)

    val lCx = station.x; val lCy = station.y - 8f
    drawArc(lockShackleColor, 180f, 180f, false, Offset(lCx - 10f, lCy - 18f), Size(20f, 20f), style = lockShackleStroke)
    drawLine(lockShackleColor, Offset(lCx - 10f, lCy - 8f), Offset(lCx - 10f, lCy), 3f)
    drawLine(lockShackleColor, Offset(lCx + 10f, lCy - 8f), Offset(lCx + 10f, lCy), 3f)
    drawRoundRect(lockBodyColor, Offset(lCx - 14f, lCy), Size(28f, 22f), CornerRadius(4f))
    drawCircle(Color(0xFF8B6914), 4f, Offset(lCx, lCy + 10f))
    drawLine(Color(0xFF6B4F10), Offset(lCx, lCy + 10f), Offset(lCx, lCy + 16f), 2f)

    drawText(textMeasurer, "\$${station.baseCost.toInt()}", Offset(station.x - 25f, station.y + 28f),
        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold))
}

private fun DrawScope.drawBuiltStation(
    station: Station, bonusProgress: Float, hasBonus: Boolean, textMeasurer: TextMeasurer
) {
    val s = 100f; val left = station.x - s / 2; val top = station.y - s / 2
    val corner = CornerRadius(12f); val level = station.level

    val tierGrad = when { level >= 20 -> tierMaxGrad; level >= 10 -> tierGoldGrad; level >= 5 -> tierBlueGrad; else -> tierBasicGrad }
    val borderColor = when { level >= 20 -> tierMaxBorder; level >= 10 -> tierGoldBorder; level >= 5 -> tierBlueBorder; else -> tierBasicBorder }
    val borderStroke = if (level >= 10) thickBorderStroke else thinBorderStroke

    drawRoundRect(stationShadowColor, Offset(left + 3f, top + 3f), Size(s, s), corner)
    drawRoundRect(Brush.verticalGradient(tierGrad, startY = top, endY = top + s), Offset(left, top), Size(s, s), corner)
    drawRoundRect(borderColor, Offset(left, top), Size(s, s), corner, style = borderStroke)

    if (level >= 10) drawRoundRect(borderColor.copy(alpha = 0.2f), Offset(left - 4f, top - 4f), Size(s + 8f, s + 8f), CornerRadius(16f))
    if (level >= 20) {
        val pulse = sin(System.nanoTime() / 300_000_000.0).toFloat() * 0.15f + 0.15f
        drawRoundRect(tierMaxBorder.copy(alpha = pulse), Offset(left - 6f, top - 6f), Size(s + 12f, s + 12f), CornerRadius(18f))
    }

    drawText(textMeasurer, station.emoji, Offset(station.x - 18f, station.y - 25f),
        style = TextStyle(fontSize = if (level >= 5) 36.sp else 32.sp))

    // Badge
    val bR = 13f; val bCx = left + s - bR - 3f; val bCy = top + bR + 3f
    val badgeCol = when { level >= 20 -> Color(0xFFFF6F00); level >= 10 -> Color(0xFFE65100); level >= 5 -> Color(0xFF1565C0); else -> Color(0xFF1B5E20) }
    drawCircle(badgeCol, bR, Offset(bCx, bCy))
    drawCircle(Color.White.copy(alpha = 0.2f), bR - 2f, Offset(bCx, bCy))
    drawText(textMeasurer, "$level", Offset(bCx - if (level >= 10) 9f else 5f, bCy - 8f),
        style = TextStyle(fontSize = if (level >= 10) 10.sp else 11.sp, fontWeight = FontWeight.Bold, color = Color.White))

    // Barra de progreso
    val barW = s - 16f; val barH = 5f; val barY = top + s - 12f
    drawRoundRect(barBgColor, Offset(left + 8f, barY), Size(barW, barH), CornerRadius(2f))
    val fillW = if (hasBonus) barW else barW * bonusProgress
    drawRoundRect(if (hasBonus) barReadyColor else barFillColor, Offset(left + 8f, barY), Size(fillW, barH), CornerRadius(2f))

    // Bono listo
    if (hasBonus) {
        val bob = sin(System.nanoTime() / 200_000_000.0).toFloat() * 4f
        drawCircle(AccentGreen.copy(alpha = 0.3f), 20f, Offset(station.x, top - 14f + bob))
        drawCircle(barReadyColor, 14f, Offset(station.x, top - 14f + bob))
        drawText(textMeasurer, "$", Offset(station.x - 6f, top - 23f + bob),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
    }
}

// ============================================================
// PERSONAJE CAPITÁN
// ============================================================
private fun DrawScope.drawCharacter(state: CharacterState, textMeasurer: TextMeasurer) {
    val x = state.x; val frameTick = state.animationFrame
    val bobY = if (state.isMoving) sin(frameTick * 0.3f) * 2f else 0f
    val cy = state.y + bobY

    drawText(textMeasurer, "CAPITÁN", Offset(x - 28f, state.y - 68f),
        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentGold))
    drawOval(Color.Black.copy(alpha = 0.25f), Offset(x - 16f, state.y + 18f), Size(32f, 10f))

    val legOff = if (state.isMoving) sin(frameTick * 0.4f) * 6f else 0f
    drawRoundRect(pantColor, Offset(x - 8f, cy + 16f), Size(7f, 10f + legOff), CornerRadius(2f))
    drawRoundRect(pantColor, Offset(x + 1f, cy + 16f), Size(7f, 10f - legOff), CornerRadius(2f))
    drawRoundRect(shoeColor, Offset(x - 9f, cy + 25f + legOff), Size(9f, 4f), CornerRadius(2f))
    drawRoundRect(shoeColor, Offset(x, cy + 25f - legOff), Size(9f, 4f), CornerRadius(2f))

    drawRoundRect(uniformColor, Offset(x - 12f, cy - 8f), Size(24f, 26f), CornerRadius(6f))
    for (i in 0..2) drawCircle(hatBadgeColor, 1.5f, Offset(x, cy - 2f + i * 6f))

    val armSwing = if (state.isMoving) sin(frameTick * 0.4f) * 4f else 0f
    drawRoundRect(uniformColor, Offset(x - 16f, cy - 4f + armSwing), Size(6f, 18f), CornerRadius(3f))
    drawRoundRect(uniformColor, Offset(x + 10f, cy - 4f - armSwing), Size(6f, 18f), CornerRadius(3f))
    drawCircle(skinColor, 3f, Offset(x - 13f, cy + 14f + armSwing))
    drawCircle(skinColor, 3f, Offset(x + 13f, cy + 14f - armSwing))

    val tie = Path().apply {
        moveTo(x - 2f, cy - 8f); lineTo(x + 2f, cy - 8f); lineTo(x + 3f, cy + 2f)
        lineTo(x, cy + 5f); lineTo(x - 3f, cy + 2f); close()
    }
    drawPath(tie, tieColor)
    drawRect(skinColor, Offset(x - 4f, cy - 12f), Size(8f, 6f))
    drawCircle(skinColor, 12f, Offset(x, cy - 18f))
    drawArc(hairColor, 30f, 120f, true, Offset(x - 12f, cy - 28f), Size(24f, 18f))

    drawRoundRect(hatColor, Offset(x - 14f, cy - 30f), Size(28f, 10f), CornerRadius(4f))
    drawRoundRect(hatColor, Offset(x - 10f, cy - 22f), Size(20f, 4f), CornerRadius(2f))
    drawRoundRect(hatBadgeColor, Offset(x - 6f, cy - 33f), Size(12f, 4f), CornerRadius(2f))
    drawCircle(hatBadgeColor, 3f, Offset(x, cy - 27f))

    val eyeOff = when (state.direction) { CharacterDirection.LEFT -> -2f; CharacterDirection.RIGHT -> 2f; else -> 0f }
    drawCircle(eyeWhiteColor, 3.5f, Offset(x - 4f + eyeOff, cy - 20f))
    drawCircle(eyeWhiteColor, 3.5f, Offset(x + 4f + eyeOff, cy - 20f))
    drawCircle(pupilColor, 2f, Offset(x - 3.5f + eyeOff, cy - 20f))
    drawCircle(pupilColor, 2f, Offset(x + 4.5f + eyeOff, cy - 20f))
    drawArc(pupilColor, 0f, 180f, false, Offset(x - 4f, cy - 16f), Size(8f, 5f), style = smileStroke)
}

// === BONOS FLOTANTES ===
private fun DrawScope.drawFloatingBonus(bonus: FloatingBonus, textMeasurer: TextMeasurer) {
    drawCircle(bonusGlowColor, 25f, Offset(bonus.x + 15f, bonus.y + 10f))
    drawText(textMeasurer, "$", Offset(bonus.x, bonus.y),
        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AccentGreen))
}
