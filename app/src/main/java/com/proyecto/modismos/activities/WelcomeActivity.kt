package com.proyecto.modismos.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.modismos.R

class WelcomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_welcome)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya está logueado
        if (auth.currentUser != null) {
            redirectToVerifyIdentityActivity()
            return
        }

        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Obtener referencias a los botones
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupLink = findViewById<TextView>(R.id.signupLink)

        // Redirigir a la pantalla de login
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val options = ActivityOptions
                .makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }

        // Redirigir a la pantalla de crear cuenta
        signupLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            val options = ActivityOptions
                .makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }
    }

    private fun redirectToVerifyIdentityActivity() {
        val intent = Intent(this, VerifyIdentityActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val options = ActivityOptions
            .makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent, options.toBundle())
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
}