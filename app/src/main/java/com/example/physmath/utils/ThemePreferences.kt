package com.example.physmath.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

object ThemePreferences {

    private val THEME_KEY = stringPreferencesKey("theme_mode")

    fun getThemeMode(context: Context): Flow<ThemeMode> {
        return context.dataStore.data
            .map { preferences ->
                when (preferences[THEME_KEY]) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
            }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }
}