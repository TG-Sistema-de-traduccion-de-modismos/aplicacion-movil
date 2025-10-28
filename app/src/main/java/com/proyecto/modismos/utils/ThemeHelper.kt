package com.proyecto.modismos.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class ThemeHelper(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_THEME_MODE = "theme_mode"

        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_AUTO = 2

        // Método estático para aplicar tema sin necesidad de instancia
        fun applyThemeStatic(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedTheme = prefs.getInt(KEY_THEME_MODE, THEME_AUTO)

            Log.d("ThemeHelper", "Tema guardado: $savedTheme")

            val nightMode = when (savedTheme) {
                THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                THEME_AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            Log.d("ThemeHelper", "Aplicando nightMode: $nightMode")
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
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

        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun saveAndApplyTheme(themeMode: Int) {
        sharedPreferences.edit()
            .putInt(KEY_THEME_MODE, themeMode)
            .apply()

        // Aplicar inmediatamente
        applyTheme()
    }

    fun getSavedTheme(): Int {
        return sharedPreferences.getInt(KEY_THEME_MODE, THEME_AUTO)
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