package com.proyecto.modismos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var confirmPasswordEt: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var createAccountBtn: Button
    private lateinit var termsLinkTv: TextView
    private lateinit var loginLinkTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        bindViews()

        if (intent.getBooleanExtra("checkTerms", false)) {
            termsCheckbox.isChecked = true
        }

        setupListeners()
    }


    private fun bindViews() {
        emailEt           = findViewById(R.id.emailEditText)
        passwordEt        = findViewById(R.id.passwordEditText)
        confirmPasswordEt = findViewById(R.id.confirmPasswordEditText)
        termsCheckbox     = findViewById(R.id.termsCheckbox)
        createAccountBtn  = findViewById(R.id.createAccountButton)
        termsLinkTv       = findViewById(R.id.termsLinkTextView)
        loginLinkTv       = findViewById(R.id.loginLinkTextView)
    }

    private fun setupListeners() {
        // Al pulsar en “Términos y condiciones”
        termsLinkTv.setOnClickListener {
            val intent = Intent(this, TermsConditionsActivity::class.java)
            startActivity(intent)
        }

        // Al pulsar en “Crear cuenta”
        createAccountBtn.setOnClickListener {
            val intent = Intent(this, VerifyIdentityActivity::class.java)
            startActivity(intent)
        }

        // Al pulsar en “¿Ya tienes cuenta? Inicia sesión”
        loginLinkTv.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupTransparentBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        val isNight = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        insetsController.isAppearanceLightStatusBars = !isNight
        insetsController.isAppearanceLightNavigationBars = !isNight
    }
}
