package com.proyecto.modismos.fragments

import org.junit.Assert.*
import org.junit.Test

class ProfileFragmentTest {
    private fun validatePasswordChange(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String {
        return when {
            currentPassword.isEmpty() -> "Ingresa tu contraseña actual"
            newPassword.isEmpty() -> "Ingresa la nueva contraseña"
            newPassword.length < 6 -> "La nueva contraseña debe tener al menos 6 caracteres"
            newPassword != confirmPassword -> "Las contraseñas no coinciden"
            else -> "OK"
        }
    }

    // Simulamos la lógica de selección de tema
    private enum class ThemeOption { LIGHT, DARK, AUTO }

    private fun getThemeMessage(selection: ThemeOption): String {
        return when (selection) {
            ThemeOption.LIGHT -> "Tema claro aplicado"
            ThemeOption.DARK -> "Tema oscuro aplicado"
            ThemeOption.AUTO -> "Tema automático aplicado"
        }
    }

    // ========== TESTS DE VALIDACIÓN DE CONTRASEÑA ==========

    @Test
    fun `valida que contraseña actual vacía muestre mensaje correcto`() {
        val result = validatePasswordChange("", "abcdef", "abcdef")
        assertEquals("Ingresa tu contraseña actual", result)
    }

    @Test
    fun `valida que nueva contraseña vacía muestre mensaje correcto`() {
        val result = validatePasswordChange("old", "", "")
        assertEquals("Ingresa la nueva contraseña", result)
    }

    @Test
    fun `valida que nueva contraseña corta muestre mensaje correcto`() {
        val result = validatePasswordChange("old", "123", "123")
        assertEquals("La nueva contraseña debe tener al menos 6 caracteres", result)
    }

    @Test
    fun `valida que contraseñas distintas muestre mensaje correcto`() {
        val result = validatePasswordChange("old", "abcdef", "abcdeg")
        assertEquals("Las contraseñas no coinciden", result)
    }

    @Test
    fun `valida que todo correcto retorne OK`() {
        val result = validatePasswordChange("old", "abcdef", "abcdef")
        assertEquals("OK", result)
    }

    // ========== TESTS DE MENSAJE DE TEMA ==========

    @Test
    fun `mensaje para tema claro es correcto`() {
        val result = getThemeMessage(ThemeOption.LIGHT)
        assertEquals("Tema claro aplicado", result)
    }

    @Test
    fun `mensaje para tema oscuro es correcto`() {
        val result = getThemeMessage(ThemeOption.DARK)
        assertEquals("Tema oscuro aplicado", result)
    }

    @Test
    fun `mensaje para tema automático es correcto`() {
        val result = getThemeMessage(ThemeOption.AUTO)
        assertEquals("Tema automático aplicado", result)
    }
}
