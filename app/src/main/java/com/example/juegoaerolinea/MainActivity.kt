package com.example.juegoaerolinea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.juegoaerolinea.ui.screens.game.GameScreen
import com.example.juegoaerolinea.ui.screens.game.GameViewModel
import com.example.juegoaerolinea.ui.theme.JuegoAerolineaTheme

class MainActivity : ComponentActivity() {

    private var gameViewModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JuegoAerolineaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val factory = GameViewModelFactory()
                    val vm: GameViewModel = viewModel(factory = factory)
                    gameViewModel = vm

                    GameScreen(
                        viewModel = vm,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel?.saveSession()
    }
}
