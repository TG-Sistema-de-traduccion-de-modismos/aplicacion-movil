package com.proyecto.modismos.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_THEME_MODE = "theme_mode"

        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_AUTO = 2
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun applyTheme() {
        val savedTheme = getSavedTheme()
        val nightMode = when (savedTheme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            THEME_AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        println("DEBUG: Aplicando tema $savedTheme -> nightMode: $nightMode")
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun saveAndApplyTheme(themeMode: Int) {
        println("DEBUG: Guardando tema: $themeMode")

        sharedPreferences.edit()
            .putInt(KEY_THEME_MODE, themeMode)
            .apply()

        // Aplicar inmediatamente
        applyTheme()
    }

    fun getSavedTheme(): Int {
        val theme = sharedPreferences.getInt(KEY_THEME_MODE, THEME_AUTO)
        println("DEBUG: Tema guardado: $theme")
        return theme
    }

    fun getCurrentThemeName(): String {
        return when (getSavedTheme()) {
            THEME_LIGHT -> "Claro"
            THEME_DARK -> "Oscuro"
            THEME_AUTO -> "Automático"
            else -> "Automático"
        }
    }
}
