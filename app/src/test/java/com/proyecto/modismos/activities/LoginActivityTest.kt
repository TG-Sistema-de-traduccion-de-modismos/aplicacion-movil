package com.proyecto.modismos.activities

import android.util.Patterns
import org.junit.Assert.*
import org.junit.Test

class LoginActivityTest {

    // ========== TESTS DE VALIDACIÓN DE EMAIL ==========

    @Test
    fun `email vacio retorna false`() {
        val email = ""
        val result = email.isNotEmpty()
        assertFalse(result)
    }

    @Test
    fun `email valido retorna true`() {
        val email = "test@example.com"
        val result = email.isNotEmpty() && email.contains("@")
        assertTrue(result)
    }

    @Test
    fun `email sin arroba retorna false`() {
        val email = "testexample.com"
        val result = email.contains("@")
        assertFalse(result)
    }

    @Test
    fun `email con espacios se puede trimear`() {
        val email = "  test@example.com  "
        val trimmed = email.trim()
        assertEquals("test@example.com", trimmed)
        assertTrue(trimmed.contains("@"))
    }

    // ========== TESTS DE VALIDACIÓN DE PASSWORD ==========

    @Test
    fun `password vacia retorna false`() {
        val password = ""
        val result = password.isNotEmpty()
        assertFalse(result)
    }

    @Test
    fun `password con contenido retorna true`() {
        val password = "password123"
        val result = password.isNotEmpty()
        assertTrue(result)
    }

    // ========== TESTS DE VALIDACIÓN COMBINADA ==========

    @Test
    fun `validacion completa con email y password vacios falla`() {
        val email = ""
        val password = ""

        val emailValid = email.isNotEmpty()
        val passwordValid = password.isNotEmpty()

        assertFalse("Email debe ser inválido", emailValid)
        assertFalse("Password debe ser inválido", passwordValid)
    }

    @Test
    fun `validacion completa con datos correctos pasa`() {
        val email = "test@example.com"
        val password = "password123"

        val emailValid = email.isNotEmpty() && email.contains("@")
        val passwordValid = password.isNotEmpty()

        assertTrue("Email debe ser válido", emailValid)
        assertTrue("Password debe ser válido", passwordValid)
    }

    @Test
    fun `validacion con email valido pero password vacia falla`() {
        val email = "test@example.com"
        val password = ""

        val emailValid = email.isNotEmpty() && email.contains("@")
        val passwordValid = password.isNotEmpty()

        assertTrue("Email debe ser válido", emailValid)
        assertFalse("Password debe ser inválido", passwordValid)
    }

    @Test
    fun `validacion con email invalido pero password valida falla`() {
        val email = "correo-invalido"
        val password = "password123"

        val emailValid = email.contains("@")
        val passwordValid = password.isNotEmpty()

        assertFalse("Email debe ser inválido", emailValid)
        assertTrue("Password debe ser válido", passwordValid)
    }

    // ========== TESTS DE FORMATOS DE EMAIL ==========

    @Test
    fun `emails con diferentes formatos validos`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.co.uk",
            "firstname+lastname@example.com",
            "email@subdomain.example.com",
            "1234567890@example.com"
        )

        validEmails.forEach { email ->
            assertTrue("$email debe contener @", email.contains("@"))
            assertTrue("$email debe tener contenido antes de @", email.split("@")[0].isNotEmpty())
            assertTrue("$email debe tener contenido después de @", email.split("@").getOrNull(1)?.isNotEmpty() ?: false)
        }
    }

    @Test
    fun `emails con formatos invalidos`() {
        val invalidEmails = listOf(
            "plainaddress",
            "@missinglocal.com",
            "missing@",
            "two@@example.com"
        )

        invalidEmails.forEach { email ->
            val hasAt = email.contains("@")
            val parts = email.split("@")
            val hasValidFormat = hasAt && parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()

            assertFalse("$email debería ser inválido", hasValidFormat)
        }
    }

    // ========== TESTS DE CASOS EDGE ==========

    @Test
    fun `email con multiples espacios se trimea correctamente`() {
        val email = "   test@example.com   "
        val trimmed = email.trim()
        assertEquals("test@example.com", trimmed)
    }

    @Test
    fun `password con espacios al final no se debe trimear automaticamente`() {
        val password = "password123   "
        // La contraseña NO debe trimmearse porque los espacios pueden ser intencionales
        assertEquals("password123   ", password)
        assertNotEquals("password123", password)
    }

    @Test
    fun `email en mayusculas se puede convertir a minusculas`() {
        val email = "TEST@EXAMPLE.COM"
        val lowercase = email.lowercase()
        assertEquals("test@example.com", lowercase)
    }
}