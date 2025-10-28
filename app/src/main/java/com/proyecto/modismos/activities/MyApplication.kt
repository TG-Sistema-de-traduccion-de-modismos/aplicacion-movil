package com.proyecto.modismos.activities

import android.app.Application
import android.util.Log
import com.proyecto.modismos.utils.ThemeHelper

class MyApplication : Application() {

    class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            Log.d("MyApplication", "=== Aplicando tema al iniciar app ===")
            ThemeHelper.applyThemeStatic(this)
            Log.d("MyApplication", "=== Tema aplicado ===")
        }
    }
}