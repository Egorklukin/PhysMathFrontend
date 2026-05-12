package com.example.physmath.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    fun applyTheme(context: Context, mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

        AppCompatDelegate.setDefaultNightMode(nightMode)

        if (context is Activity) {
            context.recreate()
        }
    }

    fun getCurrentMode(): ThemeMode {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> ThemeMode.LIGHT
            AppCompatDelegate.MODE_NIGHT_YES -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }
}