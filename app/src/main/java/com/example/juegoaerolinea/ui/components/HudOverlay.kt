package com.example.juegoaerolinea.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.ui.theme.*
import com.example.juegoaerolinea.util.NumberFormatter

@Composable
fun HudOverlay(
    money: Double,
    earnPerSec: Double,
    prestigeTokens: Int,
    canPrestige: Boolean,
    onUpgradesClick: () -> Unit,
    onPrestigeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Shimmer animation for money
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Money display
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(DarkBackground.copy(alpha = 0.92f), DarkSurface.copy(alpha = 0.92f))
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "💰 ${NumberFormatter.format(money)}",
                color = AccentGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(shimmerAlpha)
            )
            Text(
                text = "📈 ${NumberFormatter.format(earnPerSec)}/s",
                color = AccentGreen,
                fontSize = 14.sp
            )
            if (prestigeTokens > 0) {
                Text(
                    text = "⭐ x${String.format("%.0f", (1.0 + prestigeTokens * 0.25) * 100)}%",
                    color = AccentPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row {
            // Upgrades button
            IconButton(
                onClick = onUpgradesClick,
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(listOf(AccentSkyBlue, CaptainUniform)),
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = "Mejoras",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Prestige button
            IconButton(
                onClick = onPrestigeClick,
                modifier = Modifier
                    .background(
                        if (canPrestige)
                            Brush.verticalGradient(listOf(AccentPurple, Color(0xFF9C27B0)))
                        else
                            Brush.verticalGradient(listOf(Color(0xFF555555), Color(0xFF333333))),
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Prestigio",
                    tint = if (canPrestige) AccentGold else Color.Gray
                )
            }
        }
    }
}
