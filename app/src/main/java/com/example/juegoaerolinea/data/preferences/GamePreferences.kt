package com.example.juegoaerolinea.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "airline_tycoon_prefs")

class GamePreferences(private val context: Context) {
    companion object {
        val MONEY_KEY = doublePreferencesKey("player_money")
        val LAST_SESSION_TIMESTAMP = longPreferencesKey("last_session_timestamp")
    }

    val playerMoneyFlow: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[MONEY_KEY] ?: 50.0
        }

    suspend fun updateMoney(newAmount: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONEY_KEY] = newAmount
        }
    }

    suspend fun getLastSessionTimestamp(): Long {
        return context.dataStore.data.first()[LAST_SESSION_TIMESTAMP] ?: 0L
    }

    suspend fun saveSessionTimestamp(timestampMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SESSION_TIMESTAMP] = timestampMs
        }
    }

    suspend fun resetMoney(startingMoney: Double = 50.0) {
        context.dataStore.edit { preferences ->
            preferences[MONEY_KEY] = startingMoney
        }
    }
}
