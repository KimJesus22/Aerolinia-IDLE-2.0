package com.example.juegoaerolinea.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.domain.model.Upgrade
import com.example.juegoaerolinea.ui.theme.*
import com.example.juegoaerolinea.util.NumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradesSheet(
    upgrades: List<Upgrade>,
    money: Double,
    onPurchase: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔧 Mejoras Globales",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            upgrades.forEach { upgrade ->
                UpgradeItem(
                    upgrade = upgrade,
                    canAfford = money >= upgrade.upgradeCost && !upgrade.isMaxLevel,
                    onPurchase = { onPurchase(upgrade.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun UpgradeItem(
    upgrade: Upgrade,
    canAfford: Boolean,
    onPurchase: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${upgrade.emoji} ${upgrade.name}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = upgrade.description,
                fontSize = 13.sp,
                color = AccentSkyBlue.copy(alpha = 0.7f)
            )
            Text(
                text = "Nivel: ${upgrade.level}/${upgrade.maxLevel}",
                fontSize = 12.sp,
                color = if (upgrade.isMaxLevel) AccentGold else Color.Gray
            )
        }

        if (upgrade.isMaxLevel) {
            Text(
                text = "MAX",
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                fontSize = 16.sp
            )
        } else {
            Button(
                onClick = onPurchase,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) AccentGreen else Color(0xFF555555),
                    disabledContainerColor = Color(0xFF555555)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = NumberFormatter.format(upgrade.upgradeCost),
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
