package com.example.juegoaerolinea.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.juegoaerolinea.domain.model.CharacterDirection
import com.example.juegoaerolinea.domain.model.CharacterState
import com.example.juegoaerolinea.domain.model.Station
import com.example.juegoaerolinea.ui.screens.game.FloatingBonus
import com.example.juegoaerolinea.ui.theme.*

@Composable
fun MapCanvas(
    characterState: CharacterState,
    stations: List<Station>,
    floatingBonuses: List<FloatingBonus>,
    airplaneX: Float,
    textMeasurer: TextMeasurer,
    onMapTap: (Float, Float) -> Unit,
    onStationTap: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
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
    ) {
        drawAirportFloor()
        drawRunway()
        drawFlyingAirplane(airplaneX)
        drawDecorations(textMeasurer)

        stations.forEach { station ->
            drawStation(station, textMeasurer)
        }

        drawCharacter(characterState, textMeasurer)

        floatingBonuses.forEach { bonus ->
            drawFloatingBonus(bonus, textMeasurer)
        }
    }
}

private fun DrawScope.drawAirportFloor() {
    val tileSize = 120f
    for (x in 0..(size.width / tileSize).toInt() + 1) {
        for (y in 0..(size.height / tileSize).toInt() + 1) {
            val color = if ((x + y) % 2 == 0) TileLight else TileDark
            drawRect(
                color = color,
                topLeft = Offset(x * tileSize, y * tileSize),
                size = Size(tileSize, tileSize)
            )
            drawRect(
                color = Color(0x15000000),
                topLeft = Offset(x * tileSize, y * tileSize),
                size = Size(tileSize, tileSize),
                style = Stroke(width = 1f)
            )
        }
    }
}

private fun DrawScope.drawRunway() {
    val runwayY = size.height - 100f
    val runwayHeight = 80f

    drawRect(
        color = RunwayColor,
        topLeft = Offset(0f, runwayY),
        size = Size(size.width, runwayHeight)
    )

    // Línea central discontinua
    val dashWidth = 40f
    val gapWidth = 30f
    var dashX = 10f
    while (dashX < size.width) {
        drawRect(
            color = RunwayStripe.copy(alpha = 0.8f),
            topLeft = Offset(dashX, runwayY + runwayHeight / 2 - 2f),
            size = Size(dashWidth, 4f)
        )
        dashX += dashWidth + gapWidth
    }

    // Bordes amarillos
    drawLine(
        color = Color.Yellow.copy(alpha = 0.6f),
        start = Offset(0f, runwayY + 4f),
        end = Offset(size.width, runwayY + 4f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color.Yellow.copy(alpha = 0.6f),
        start = Offset(0f, runwayY + runwayHeight - 4f),
        end = Offset(size.width, runwayY + runwayHeight - 4f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawFlyingAirplane(airplaneX: Float) {
    val runwayY = size.height - 100f
    val planeY = runwayY + 15f

    // Fuselaje
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(airplaneX, planeY),
        size = Size(50f, 14f),
        cornerRadius = CornerRadius(7f, 7f)
    )
    // Ala superior
    val wingPath = Path().apply {
        moveTo(airplaneX + 15f, planeY + 7f)
        lineTo(airplaneX + 10f, planeY - 12f)
        lineTo(airplaneX + 35f, planeY - 12f)
        lineTo(airplaneX + 30f, planeY + 7f)
        close()
    }
    drawPath(path = wingPath, color = Color(0xFFB0BEC5))
    // Ala inferior
    val wingPath2 = Path().apply {
        moveTo(airplaneX + 15f, planeY + 7f)
        lineTo(airplaneX + 10f, planeY + 26f)
        lineTo(airplaneX + 35f, planeY + 26f)
        lineTo(airplaneX + 30f, planeY + 7f)
        close()
    }
    drawPath(path = wingPath2, color = Color(0xFFB0BEC5))
    // Cola
    val tailPath = Path().apply {
        moveTo(airplaneX, planeY + 7f)
        lineTo(airplaneX - 10f, planeY - 8f)
        lineTo(airplaneX + 5f, planeY)
        close()
    }
    drawPath(path = tailPath, color = AccentSkyBlue)
    // Ventanas
    for (i in 0..3) {
        drawCircle(
            color = AccentSkyBlue,
            radius = 2f,
            center = Offset(airplaneX + 18f + i * 7f, planeY + 5f)
        )
    }
    // Nariz roja
    drawCircle(
        color = AccentRed,
        radius = 3f,
        center = Offset(airplaneX + 50f, planeY + 7f)
    )
}

private fun DrawScope.drawDecorations(textMeasurer: TextMeasurer) {
    val palmPositions = listOf(
        Offset(50f, 150f), Offset(size.width - 50f, 150f),
        Offset(50f, 450f), Offset(size.width - 50f, 450f)
    )
    palmPositions.forEach { pos ->
        drawText(
            textMeasurer = textMeasurer, text = "🌴",
            topLeft = Offset(pos.x - 15f, pos.y - 15f),
            style = TextStyle(fontSize = 28.sp)
        )
    }

    drawText(textMeasurer = textMeasurer, text = "💺",
        topLeft = Offset(120f, 350f), style = TextStyle(fontSize = 20.sp))
    drawText(textMeasurer = textMeasurer, text = "💺",
        topLeft = Offset(160f, 350f), style = TextStyle(fontSize = 20.sp))
    drawText(textMeasurer = textMeasurer, text = "🧳",
        topLeft = Offset(size.width - 120f, 350f), style = TextStyle(fontSize = 20.sp))
}

private fun DrawScope.drawStation(station: Station, textMeasurer: TextMeasurer) {
    val isActive = station.isUnlocked && station.level > 0
    val stationSize = 100f
    val left = station.x - stationSize / 2
    val top = station.y - stationSize / 2

    if (isActive) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(StationActiveBg, DarkCard),
                startY = top, endY = top + stationSize
            ),
            topLeft = Offset(left, top),
            size = Size(stationSize, stationSize),
            cornerRadius = CornerRadius(12f, 12f)
        )
        // Borde brillante
        drawRoundRect(
            color = StationActiveBorder,
            topLeft = Offset(left, top),
            size = Size(stationSize, stationSize),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 3f)
        )
        // Resplandor sutil
        drawRoundRect(
            color = StationActiveBorder.copy(alpha = 0.15f),
            topLeft = Offset(left - 4f, top - 4f),
            size = Size(stationSize + 8f, stationSize + 8f),
            cornerRadius = CornerRadius(16f, 16f)
        )
    } else {
        drawRoundRect(
            color = StationLockedBg,
            topLeft = Offset(left, top),
            size = Size(stationSize, stationSize),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFF555555),
            topLeft = Offset(left, top),
            size = Size(stationSize, stationSize),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2f)
        )
    }

    val emojiToDraw = if (isActive) station.emoji else "🔒"
    drawText(
        textMeasurer = textMeasurer,
        text = emojiToDraw,
        topLeft = Offset(station.x - 18f, station.y - 25f),
        style = TextStyle(fontSize = 32.sp)
    )

    val label = if (isActive) "Nv ${station.level}" else "\$${station.baseCost.toInt()}"
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(station.x - 25f, station.y + 20f),
        style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) AccentGold else Color.Gray
        )
    )
}

private fun DrawScope.drawCharacter(state: CharacterState, textMeasurer: TextMeasurer) {
    val cx = state.x
    val cy = state.y

    // === ETIQUETA "CAPITÁN" ===
    drawText(
        textMeasurer = textMeasurer,
        text = "CAPITÁN",
        topLeft = Offset(cx - 28f, cy - 62f),
        style = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold
        )
    )

    // === SOMBRA ===
    drawOval(
        color = Color.Black.copy(alpha = 0.35f),
        topLeft = Offset(cx - 18f, cy + 38f),
        size = Size(36f, 12f)
    )

    // === GORRA ===
    drawRoundRect(
        color = CaptainHat,
        topLeft = Offset(cx - 18f, cy - 38f),
        size = Size(36f, 14f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = CaptainHat.copy(alpha = 0.9f),
        topLeft = Offset(cx - 22f, cy - 26f),
        size = Size(44f, 5f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    // Franja dorada
    drawRect(
        color = CaptainHatStripe,
        topLeft = Offset(cx - 16f, cy - 30f),
        size = Size(32f, 3f)
    )

    // === CABEZA ===
    drawCircle(
        color = CaptainSkin,
        radius = 13f,
        center = Offset(cx, cy - 14f)
    )
    drawCircle(color = Color.Black, radius = 2f, center = Offset(cx - 5f, cy - 16f))
    drawCircle(color = Color.Black, radius = 2f, center = Offset(cx + 5f, cy - 16f))
    drawArc(
        color = Color.Black,
        startAngle = 10f, sweepAngle = 160f, useCenter = false,
        topLeft = Offset(cx - 5f, cy - 12f),
        size = Size(10f, 6f),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )

    // === CUERPO ===
    drawRoundRect(
        color = CaptainUniform,
        topLeft = Offset(cx - 14f, cy),
        size = Size(28f, 30f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    for (i in 0..2) {
        drawCircle(color = AccentGold, radius = 1.5f, center = Offset(cx, cy + 6f + i * 8f))
    }

    // === CORBATA ===
    val tiePath = Path().apply {
        moveTo(cx - 3f, cy + 1f)
        lineTo(cx + 3f, cy + 1f)
        lineTo(cx + 1.5f, cy + 14f)
        lineTo(cx - 1.5f, cy + 14f)
        close()
    }
    drawPath(path = tiePath, color = CaptainTie)

    // === PIERNAS ===
    val legOffset = if (state.isMoving) {
        when (state.animationFrame) {
            0 -> 0f; 1 -> -6f; 2 -> 0f; 3 -> 6f; else -> 0f
        }
    } else 0f

    drawRoundRect(
        color = CaptainLegs,
        topLeft = Offset(cx - 10f, cy + 30f + legOffset),
        size = Size(8f, 16f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = CaptainLegs,
        topLeft = Offset(cx + 2f, cy + 30f - legOffset),
        size = Size(8f, 16f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    // Zapatos
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(cx - 11f, cy + 44f + legOffset),
        size = Size(10f, 4f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(cx + 1f, cy + 44f - legOffset),
        size = Size(10f, 4f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

private fun DrawScope.drawFloatingBonus(bonus: FloatingBonus, textMeasurer: TextMeasurer) {
    drawCircle(
        color = AccentGreen.copy(alpha = 0.25f),
        radius = 25f,
        center = Offset(bonus.x + 15f, bonus.y + 10f)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = "$",
        topLeft = Offset(bonus.x, bonus.y),
        style = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGreen
        )
    )
}
