package com.example.juegoaerolinea.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GameDarkColorScheme = darkColorScheme(
    primary = AccentSkyBlue,
    onPrimary = Color.White,
    primaryContainer = CaptainUniform,
    secondary = AccentGold,
    onSecondary = Color.Black,
    secondaryContainer = DarkCard,
    tertiary = AccentGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun JuegoAerolineaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = GameDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
