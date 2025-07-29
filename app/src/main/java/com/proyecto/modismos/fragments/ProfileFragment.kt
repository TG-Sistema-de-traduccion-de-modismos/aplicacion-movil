package com.proyecto.modismos.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.proyecto.modismos.R

class ProfileFragment : Fragment() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnTogglePassword: ImageView
    private lateinit var tvChangePassword: TextView
    private lateinit var btnClose: ImageView

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
        initViews(view)
        setupClickListeners()
        setupInitialData()
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnTogglePassword = view.findViewById(R.id.btnTogglePassword)
        tvChangePassword = view.findViewById(R.id.tvChangePassword)
        btnClose = view.findViewById(R.id.btnClose)
    }

    private fun setupClickListeners() {
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        tvChangePassword.setOnClickListener {
            // Aquí iría la lógica para cambiar contraseña
            // Por ahora solo mostramos un mensaje
        }

        btnClose.setOnClickListener {
            // Cerrar el fragmento y volver al anterior
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupInitialData() {
        // Datos quemados como solicitas
        etEmail.setText("pablo@gmail.com")
        etPassword.setText("****************")

        // Email no editable
        etEmail.isEnabled = false
        etPassword.isEnabled = false
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            etPassword.setText("****************")
        } else {
            // Mostrar contraseña
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            etPassword.setText("miPassword123")
        }
        isPasswordVisible = !isPasswordVisible

        // Mover el cursor al final del texto
        etPassword.setSelection(etPassword.text?.length ?: 0)
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}