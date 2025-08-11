package com.proyecto.modismos.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.proyecto.modismos.R

class TermsConditionsActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private lateinit var acceptButton: Button
    private var currentCheckState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_conditions)
        supportActionBar?.hide()

        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        bindViews()

        // Obtenemos el estado actual del checkbox que viene desde RegisterActivity
        currentCheckState = intent.getBooleanExtra("currentCheckState", false)

        setupListeners()
        setupBackPressedCallback()
    }

    private fun bindViews() {
        closeButton  = findViewById(R.id.closeButton)
        acceptButton = findViewById(R.id.acceptButton)
    }

    private fun setupListeners() {
        // "X": vuelve manteniendo el estado anterior del checkbox
        closeButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("checkTerms", currentCheckState)
            setResult(RESULT_OK, resultIntent)
            finish()

        }

        // "Aceptar": vuelve marcando el checkbox como true
        acceptButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("checkTerms", true)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val resultIntent = Intent()
                resultIntent.putExtra("checkTerms", currentCheckState)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
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
}