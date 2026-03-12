package com.example.juegoaerolinea.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.juegoaerolinea.util.NumberFormatter

@Composable
fun OfflineEarningsDialog(
    earnings: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "✈️ ¡Bienvenido de vuelta!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Text(
                text = "Mientras no estabas, tu aerolínea generó:\n\n💰 ${NumberFormatter.format(earnings)}",
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("¡Genial!")
            }
        }
    )
}
