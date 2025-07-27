package com.proyecto.modismos.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.proyecto.modismos.R

class TermsConditionsActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageView
    private lateinit var acceptButton: Button
    private lateinit var termsCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_conditions)
        supportActionBar?.hide()

        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        bindViews()

        if (intent.getBooleanExtra("checkTerms", false)) {
            termsCheckbox = findViewById(R.id.termsCheckbox)
            termsCheckbox.isChecked = true
        }

        setupListeners()
    }


    private fun bindViews() {
        closeButton  = findViewById(R.id.closeButton)
        acceptButton = findViewById(R.id.acceptButton)
    }

    private fun setupListeners() {
        // “X”: vuelve sin marcar
        closeButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // “Aceptar”: vuelve marcando checkbox
        acceptButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("checkTerms", true)
            startActivity(intent)
            finish()
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
}
