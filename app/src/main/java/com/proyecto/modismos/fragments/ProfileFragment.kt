package com.proyecto.modismos.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.proyecto.modismos.activities.LoginActivity
import com.proyecto.modismos.R

class ProfileFragment : Fragment() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnTogglePassword: ImageView
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogout: ImageView

    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        initViews(view)
        setupClickListeners()
        setupUserData()
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnTogglePassword = view.findViewById(R.id.btnTogglePassword)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupUserData() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            etEmail.setText(currentUser.email)
            etPassword.setText("********")
        } else {
            redirectToLogin()
        }

        etEmail.isEnabled = false
        etPassword.isEnabled = false
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            etPassword.setText("********")
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            etPassword.setText("Oculta por seguridad")
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text?.length ?: 0)
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)

        val etCurrentPassword = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmNewPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar Contraseña")
            .setView(dialogView)
            .setPositiveButton("Cambiar") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmNewPassword = etConfirmNewPassword.text.toString()

                changePassword(currentPassword, newPassword, confirmNewPassword)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        // Validaciones
        if (currentPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa tu contraseña actual", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa la nueva contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(requireContext(), "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser?.email == null) {
            Toast.makeText(requireContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Reautenticar al usuario antes de cambiar la contraseña
        val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)

        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    // Cambiar la contraseña
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}