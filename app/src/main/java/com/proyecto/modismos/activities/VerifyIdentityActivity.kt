package com.proyecto.modismos.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.proyecto.modismos.R
import kotlinx.coroutines.*

class VerifyIdentityActivity : AppCompatActivity() {

    private val splashDelay = 5000L  // 5 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_verify_identity)

        // Configura barras transparentes y modo claro/oscuro
        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Lanzar splash y redirigir
        CoroutineScope(Dispatchers.Main).launch {
            delay(splashDelay)
            goToHome()
        }
    }

    private fun setupTransparentBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val isNight = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        controller.isAppearanceLightStatusBars     = !isNight
        controller.isAppearanceLightNavigationBars = !isNight
    }

    private fun goToHome() {
        startActivity(Intent(this, UserMainActivity::class.java))
        finish()
    }
}
