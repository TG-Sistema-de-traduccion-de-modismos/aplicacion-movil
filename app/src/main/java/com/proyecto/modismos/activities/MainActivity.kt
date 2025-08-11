package com.proyecto.modismos.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.proyecto.modismos.R
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val splashDelay = 2000L  // 2 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        setupTransparentBars()

        // Inicia el splash y redirige después de 2 segundos
        CoroutineScope(Dispatchers.Main).launch {
            delay(splashDelay)
            goToNextScreen()
        }
    }

    private fun setupTransparentBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        val isNightMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        insetsController.isAppearanceLightStatusBars = !isNightMode
        insetsController.isAppearanceLightNavigationBars = !isNightMode
    }

    private fun goToNextScreen() {
        // verificar si la sesión está activa o no

        val intent = Intent(this, WelcomeActivity::class.java)
        val options = ActivityOptions
            .makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent, options.toBundle())
        finish()
    }
}
