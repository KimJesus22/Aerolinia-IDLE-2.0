package com.example.juegoaerolinea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.data.local.entity.PrestigeEntity
import com.example.juegoaerolinea.ui.theme.*
import com.example.juegoaerolinea.util.Constants

@Composable
fun PrestigeScreen(
    prestige: PrestigeEntity,
    canPrestige: Boolean,
    tokensToEarn: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground.copy(alpha = 0.97f), Color(0xFF0D0020))
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌟 PRESTIGIO 🌟",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPurple
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats card
        Column(
            modifier = Modifier
                .background(DarkCard, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tokens actuales: ⭐ ${prestige.tokens}",
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = "Multiplicador: x${String.format("%.0f", (1.0 + prestige.tokens * Constants.PRESTIGE_TOKEN_MULTIPLIER) * 100)}%",
                color = AccentGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Prestigios: ${prestige.prestigeCount}",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (canPrestige) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF0D3B0F))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ganarás ⭐ $tokensToEarn tokens",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val newMult = 1.0 + (prestige.tokens + tokensToEarn) * Constants.PRESTIGE_TOKEN_MULTIPLIER
                Text(
                    text = "Nuevo multiplicador: x${String.format("%.0f", newMult * 100)}%",
                    color = AccentGold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "⚠️ Se reinicia: dinero, estaciones, mejoras\n✅ Se conserva: tokens, contador de prestigios",
                textAlign = TextAlign.Center,
                color = AccentGold.copy(alpha = 0.8f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar", color = Color.White)
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text("¡Prestigiar!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = "Necesitas ganar \$1M en total para prestigiar",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = onDismiss) {
                Text("Volver", color = Color.White)
            }
        }
    }
}
