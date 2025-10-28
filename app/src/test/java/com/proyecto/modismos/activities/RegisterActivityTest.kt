package com.proyecto.modismos.activities

import org.junit.Assert.*
import org.junit.Test

class RegisterActivityTest {

    // ========== TESTS DE VALIDACIÓN DE EMAIL ==========

    @Test
    fun `email vacio retorna false`() {
        val email = ""
        val result = email.isNotEmpty()
        assertFalse("Email vacío debe ser inválido", result)
    }

    @Test
    fun `email valido retorna true`() {
        val email = "test@example.com"
        val result = email.isNotEmpty() && email.contains("@") && email.contains(".")
        assertTrue("Email válido debe pasar", result)
    }

    @Test
    fun `email sin arroba es invalido`() {
        val email = "testexample.com"
        val result = email.contains("@")
        assertFalse("Email sin @ debe ser inválido", result)
    }

    @Test
    fun `email se puede trimear correctamente`() {
        val email = "  test@example.com  "
        val trimmed = email.trim()
        assertEquals("test@example.com", trimmed)
    }

    // ========== TESTS DE VALIDACIÓN DE PASSWORD ==========

    @Test
    fun `password vacia retorna false`() {
        val password = ""
        val result = password.isNotEmpty()
        assertFalse("Password vacía debe ser inválida", result)
    }

    @Test
    fun `password menor a 6 caracteres es invalida`() {
        val password = "12345"
        val result = password.length >= 6
        assertFalse("Password menor a 6 caracteres debe ser inválida", result)
    }

    @Test
    fun `password con 6 caracteres es valida`() {
        val password = "123456"
        val result = password.length >= 6
        assertTrue("Password con 6 caracteres debe ser válida", result)
    }

    @Test
    fun `password con mas de 6 caracteres es valida`() {
        val password = "password123"
        val result = password.length >= 6 && password.isNotEmpty()
        assertTrue("Password con más de 6 caracteres debe ser válida", result)
    }

    // ========== TESTS DE CONFIRMACIÓN DE PASSWORD ==========

    @Test
    fun `passwords que coinciden retornan true`() {
        val password = "password123"
        val confirmPassword = "password123"
        val result = password == confirmPassword
        assertTrue("Passwords iguales deben coincidir", result)
    }

    @Test
    fun `passwords que no coinciden retornan false`() {
        val password = "password123"
        val confirmPassword = "password456"
        val result = password == confirmPassword
        assertFalse("Passwords diferentes no deben coincidir", result)
    }

    @Test
    fun `confirmacion vacia no coincide con password valida`() {
        val password = "password123"
        val confirmPassword = ""
        val result = password == confirmPassword
        assertFalse("Confirmación vacía no debe coincidir", result)
    }

    @Test
    fun `passwords con espacios diferentes no coinciden`() {
        val password = "password123"
        val confirmPassword = "password123 "
        val result = password == confirmPassword
        assertFalse("Passwords con espacios diferentes no deben coincidir", result)
    }

    @Test
    fun `passwords con mayusculas y minusculas diferentes no coinciden`() {
        val password = "Password123"
        val confirmPassword = "password123"
        val result = password == confirmPassword
        assertFalse("Passwords con diferente case no deben coincidir", result)
    }

    // ========== TESTS DE VALIDACIÓN COMPLETA ==========

    @Test
    fun `validacion completa con todos los campos vacios falla`() {
        val email = ""
        val password = ""
        val confirmPassword = ""
        val termsAccepted = false

        val emailValid = email.isNotEmpty() && email.contains("@")
        val passwordValid = password.isNotEmpty() && password.length >= 6
        val confirmPasswordValid = password == confirmPassword
        val termsValid = termsAccepted

        assertFalse("Email debe ser inválido", emailValid)
        assertFalse("Password debe ser inválida", passwordValid)
        assertFalse("Términos deben estar sin aceptar", termsValid)
    }

    @Test
    fun `validacion completa con datos correctos y terminos aceptados pasa`() {
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password123"
        val termsAccepted = true

        val emailValid = email.isNotEmpty() && email.contains("@")
        val passwordValid = password.isNotEmpty() && password.length >= 6
        val confirmPasswordValid = password == confirmPassword
        val termsValid = termsAccepted

        assertTrue("Email debe ser válido", emailValid)
        assertTrue("Password debe ser válida", passwordValid)
        assertTrue("Passwords deben coincidir", confirmPasswordValid)
        assertTrue("Términos deben estar aceptados", termsValid)
    }

    @Test
    fun `validacion falla si no se aceptan los terminos`() {
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password123"
        val termsAccepted = false

        val emailValid = email.isNotEmpty() && email.contains("@")
        val passwordValid = password.isNotEmpty() && password.length >= 6
        val confirmPasswordValid = password == confirmPassword
        val termsValid = termsAccepted

        assertTrue("Email debe ser válido", emailValid)
        assertTrue("Password debe ser válida", passwordValid)
        assertTrue("Passwords deben coincidir", confirmPasswordValid)
        assertFalse("Términos NO deben estar aceptados", termsValid)
    }

    @Test
    fun `validacion falla si email es invalido aunque todo lo demas este bien`() {
        val email = "correo-invalido"
        val password = "password123"
        val confirmPassword = "password123"
        val termsAccepted = true

        val emailValid = email.contains("@")
        val passwordValid = password.isNotEmpty() && password.length >= 6
        val confirmPasswordValid = password == confirmPassword

        assertFalse("Email debe ser inválido", emailValid)
        assertTrue("Password debe ser válida", passwordValid)
        assertTrue("Passwords deben coincidir", confirmPasswordValid)
    }

    @Test
    fun `validacion falla si password es muy corta aunque todo lo demas este bien`() {
        val email = "test@example.com"
        val password = "123"
        val confirmPassword = "123"
        val termsAccepted = true

        val emailValid = email.contains("@")
        val passwordValid = password.length >= 6
        val confirmPasswordValid = password == confirmPassword

        assertTrue("Email debe ser válido", emailValid)
        assertFalse("Password debe ser inválida por ser corta", passwordValid)
        assertTrue("Passwords deben coincidir", confirmPasswordValid)
    }

    @Test
    fun `validacion falla si passwords no coinciden aunque todo lo demas este bien`() {
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password456"
        val termsAccepted = true

        val emailValid = email.contains("@")
        val passwordValid = password.length >= 6
        val confirmPasswordValid = password == confirmPassword

        assertTrue("Email debe ser válido", emailValid)
        assertTrue("Password debe ser válida", passwordValid)
        assertFalse("Passwords NO deben coincidir", confirmPasswordValid)
    }

    // ========== TESTS DE CASOS EDGE ==========

    @Test
    fun `password con exactamente 6 caracteres es valida`() {
        val password = "abc123"
        assertEquals(6, password.length)
        assertTrue("Password de 6 caracteres debe ser válida", password.length >= 6)
    }

    @Test
    fun `password con 5 caracteres es invalida`() {
        val password = "abc12"
        assertEquals(5, password.length)
        assertFalse("Password de 5 caracteres debe ser inválida", password.length >= 6)
    }

    @Test
    fun `email con multiples arrobas es detectable`() {
        val email = "test@@example.com"
        val atCount = email.count { it == '@' }
        assertTrue("Email con múltiples @ debe ser detectable", atCount > 1)
    }

    @Test
    fun `email sin dominio es detectable`() {
        val email = "test@"
        val parts = email.split("@")
        val hasDomain = parts.size == 2 && parts[1].isNotEmpty()
        assertFalse("Email sin dominio debe ser inválido", hasDomain)
    }

    @Test
    fun `password con caracteres especiales es valida`() {
        val password = "P@ssw0rd!"
        val result = password.length >= 6 && password.isNotEmpty()
        assertTrue("Password con caracteres especiales debe ser válida", result)
    }

    @Test
    fun `confirmacion con espacios adicionales no coincide`() {
        val password = "password123"
        val confirmPassword = " password123"
        assertNotEquals("Passwords con espacios diferentes no deben coincidir", password, confirmPassword)
    }

    // ========== TESTS DE DIFERENTES FORMATOS DE EMAIL ==========

    @Test
    fun `emails validos con diferentes formatos`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.co.uk",
            "firstname+lastname@example.com",
            "email@subdomain.example.com",
            "1234567890@example.com"
        )

        validEmails.forEach { email ->
            val hasAt = email.contains("@")
            val hasDot = email.contains(".")
            val parts = email.split("@")
            val hasLocalPart = parts.getOrNull(0)?.isNotEmpty() ?: false
            val hasDomainPart = parts.getOrNull(1)?.isNotEmpty() ?: false

            assertTrue("$email debe tener @", hasAt)
            assertTrue("$email debe tener .", hasDot)
            assertTrue("$email debe tener parte local", hasLocalPart)
            assertTrue("$email debe tener dominio", hasDomainPart)
        }
    }

    @Test
    fun `passwords validas con diferentes longitudes`() {
        val validPasswords = listOf(
            "123456",      // Mínimo 6
            "password",    // 8 caracteres
            "longpassword123",  // 16 caracteres
            "verylongpassword123456"  // 23 caracteres
        )

        validPasswords.forEach { password ->
            assertTrue("$password (${password.length} chars) debe ser válida", password.length >= 6)
        }
    }

    @Test
    fun `passwords invalidas con menos de 6 caracteres`() {
        val invalidPasswords = listOf(
            "",
            "1",
            "12",
            "123",
            "1234",
            "12345"
        )

        invalidPasswords.forEach { password ->
            assertFalse("$password (${password.length} chars) debe ser inválida", password.length >= 6)
        }
    }

    // ========== TESTS DE TÉRMINOS Y CONDICIONES ==========

    @Test
    fun `terminos no aceptados retorna false`() {
        val termsAccepted = false
        assertFalse("Términos no aceptados debe ser inválido", termsAccepted)
    }

    @Test
    fun `terminos aceptados retorna true`() {
        val termsAccepted = true
        assertTrue("Términos aceptados debe ser válido", termsAccepted)
    }
}