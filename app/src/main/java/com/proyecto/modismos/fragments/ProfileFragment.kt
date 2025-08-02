package com.proyecto.modismos.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.proyecto.modismos.activities.LoginActivity
import com.proyecto.modismos.R

class ProfileFragment : Fragment() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnTogglePassword: ImageView
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogout: ImageView

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
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnChangePassword.setOnClickListener {
            // Aquí iría la lógica para cambiar contraseña
        }

        btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupInitialData() {
        etEmail.setText("pablo@gmail.com")
        etPassword.setText("****************")

        etEmail.isEnabled = false
        etPassword.isEnabled = false
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            etPassword.setText("****************")
        } else {
            etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            etPassword.setText("miPassword123")
        }
        isPasswordVisible = !isPasswordVisible
        etPassword.setSelection(etPassword.text?.length ?: 0)
    }

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }
}
