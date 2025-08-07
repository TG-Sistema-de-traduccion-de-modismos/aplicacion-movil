package com.proyecto.modismos.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.proyecto.modismos.R

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerLinkTv: TextView
    private lateinit var forgotPasswordTv: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya está logueado
        if (auth.currentUser != null) {
            redirectToMainActivity()
            return
        }

        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        emailEt = findViewById(R.id.emailEditText)
        passwordEt = findViewById(R.id.passwordEditText)
        loginBtn = findViewById(R.id.loginButton)
        registerLinkTv = findViewById(R.id.registerLink)
        forgotPasswordTv = findViewById(R.id.forgotPasswordLink)
    }

    private fun setupListeners() {
        registerLinkTv.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            loginUser()
        }

        forgotPasswordTv.setOnClickListener {
            resetPassword()
        }
    }

    private fun loginUser() {
        val email = emailEt.text.toString().trim()
        val password = passwordEt.text.toString()

        // Validaciones
        if (!validateInput(email, password)) {
            return
        }

        // Mostrar loading
        loginBtn.isEnabled = false
        loginBtn.text = "Iniciando sesión..."

        // Autenticar con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginBtn.isEnabled = true
                loginBtn.text = "Iniciar sesión"

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Bienvenido ${user?.email}", Toast.LENGTH_SHORT).show()
                    redirectToMainActivity()

                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailEt.error = "El correo es obligatorio"
            emailEt.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.error = "Ingresa un correo válido"
            emailEt.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordEt.error = "La contraseña es obligatoria"
            passwordEt.requestFocus()
            return false
        }

        return true
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "No existe una cuenta con este correo electrónico"
            is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
            else -> "Error al iniciar sesión: ${exception?.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun resetPassword() {
        val email = emailEt.text.toString().trim()

        if (email.isEmpty()) {
            emailEt.error = "Ingresa tu correo para recuperar la contraseña"
            emailEt.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.error = "Ingresa un correo válido"
            emailEt.requestFocus()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Se ha enviado un enlace de recuperación a tu correo", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de recuperación", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, VerifyIdentityActivity::class.java) // Cambia por tu MainActivity principal
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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