package com.proyecto.modismos.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.modismos.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var confirmPasswordEt: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var createAccountBtn: Button
    private lateinit var termsLinkTv: TextView
    private lateinit var loginLinkTv: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Launcher para manejar el resultado de TermsConditionsActivity
    private val termsActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val shouldCheckTerms = result.data?.getBooleanExtra("checkTerms", false) ?: false
            termsCheckbox.isChecked = shouldCheckTerms
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupTransparentBars()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        bindViews()
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
        // Al pulsar en "Términos y condiciones"
        termsLinkTv.setOnClickListener {
            val intent = Intent(this, TermsConditionsActivity::class.java)
            // Pasamos el estado actual del checkbox
            intent.putExtra("currentCheckState", termsCheckbox.isChecked)
            termsActivityLauncher.launch(intent)
        }

        // Al pulsar en "Crear cuenta"
        createAccountBtn.setOnClickListener {
            registerUser()
        }

        // Al pulsar en "¿Ya tienes cuenta? Inicia sesión"
        loginLinkTv.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val options = ActivityOptions
                .makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }
    }

    private fun registerUser() {
        val email = emailEt.text.toString().trim()
        val password = passwordEt.text.toString()
        val confirmPassword = confirmPasswordEt.text.toString()

        // Validaciones
        if (!validateInput(email, password, confirmPassword)) {
            return
        }

        // Mostrar loading (desactivar botón)
        createAccountBtn.isEnabled = false
        createAccountBtn.text = "Creando cuenta..."

        // Crear usuario con Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso, ahora crear documento en Firestore
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        createUserInFirestore(firebaseUser.uid, email)
                    } ?: run {
                        createAccountBtn.isEnabled = true
                        createAccountBtn.text = "Crear cuenta"
                        Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error en el registro
                    createAccountBtn.isEnabled = true
                    createAccountBtn.text = "Crear cuenta"
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun createUserInFirestore(userId: String, email: String) {
        // Crear objeto usuario con lista vacía de palabras
        val userData = hashMapOf(
            "email" to email,
            "palabras" to emptyList<String>()
        )

        // Guardar en la colección "usuarios" con el ID del usuario de Authentication
        firestore.collection("usuarios").document(userId)
            .set(userData)
            .addOnSuccessListener {
                // Usuario creado exitosamente en Firestore
                createAccountBtn.isEnabled = true
                createAccountBtn.text = "Crear cuenta"

                Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()

                // Redirigir a VerifyIdentityActivity o directamente al MainActivity
                val intent = Intent(this, VerifyIdentityActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val options = ActivityOptions
                    .makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
                startActivity(intent, options.toBundle())
            }
            .addOnFailureListener { exception ->
                // Error al crear el documento en Firestore
                createAccountBtn.isEnabled = true
                createAccountBtn.text = "Crear cuenta"

                Toast.makeText(
                    this,
                    "Error al crear perfil de usuario: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()

                // Opcional: Eliminar el usuario de Authentication si falló Firestore
                auth.currentUser?.delete()
            }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        // Validar email vacío
        if (email.isEmpty()) {
            emailEt.error = "El correo es obligatorio"
            emailEt.requestFocus()
            return false
        }

        // Validar formato del email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.error = "Ingresa un correo válido"
            emailEt.requestFocus()
            return false
        }

        // Validar contraseña vacía
        if (password.isEmpty()) {
            passwordEt.error = "La contraseña es obligatoria"
            passwordEt.requestFocus()
            return false
        }

        // Validar longitud mínima de contraseña
        if (password.length < 6) {
            passwordEt.error = "La contraseña debe tener al menos 6 caracteres"
            passwordEt.requestFocus()
            return false
        }

        // Validar confirmación de contraseña
        if (confirmPassword != password) {
            confirmPasswordEt.error = "Las contraseñas no coinciden"
            confirmPasswordEt.requestFocus()
            return false
        }

        // Validar términos y condiciones
        if (!termsCheckbox.isChecked) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
            is FirebaseAuthInvalidCredentialsException -> "El correo electrónico no es válido"
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este correo electrónico"
            else -> "Error al crear la cuenta: ${exception?.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
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