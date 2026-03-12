package com.example.juegoaerolinea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import androidx.room.Room
import com.example.juegoaerolinea.data.local.GameDatabase
import com.example.juegoaerolinea.data.preferences.GamePreferences
import com.example.juegoaerolinea.data.repository.GameRepository
import com.example.juegoaerolinea.ui.screens.game.GameViewModel

object Graph {
    lateinit var database: GameDatabase
        private set

    lateinit var preferences: GamePreferences
        private set

    val repository by lazy {
        GameRepository(
            stationDao = database.stationDao(),
            upgradeDao = database.upgradeDao(),
            prestigeDao = database.prestigeDao(),
            gamePreferences = preferences
        )
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(context, GameDatabase::class.java, "game.db")
            .fallbackToDestructiveMigration()
            .build()
        preferences = GamePreferences(context)
    }
}

class GameViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(Graph.repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
