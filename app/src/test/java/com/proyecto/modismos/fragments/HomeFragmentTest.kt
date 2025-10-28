package com.proyecto.modismos.fragments

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

// ========== TEST UNITARIOS DE LÓGICA PURA PARA HomeFragment ==========
class HomeFragmentTest {

    // Simulación de las funciones de lógica pura del fragmento
    private fun formatTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun getFileExtension(fileName: String?): String {
        return fileName?.substringAfterLast('.', "m4a") ?: "m4a"
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = Date(timestamp.toLong())
            sdf.format(date)
        } catch (e: Exception) {
            "Audio"
        }
    }

    private fun getCharacterCountColor(count: Int): Int {
        return when {
            count < 360 -> 0 // gris
            count in 360..400 -> 1 // naranja
            else -> 2 // rojo
        }
    }

    // ========== TESTS DE FORMATEO DE TIEMPO ==========

    @Test
    fun `formatTime con 0ms retorna 00 colon 00`() {
        val result = formatTime(0)
        assertEquals("00:00", result)
    }

    @Test
    fun `formatTime con 65000ms retorna 01 colon 05`() {
        val result = formatTime(65000)
        assertEquals("01:05", result)
    }

    @Test
    fun `formatTime con 123456ms retorna 02 colon 03`() {
        val result = formatTime(123456)
        assertEquals("02:03", result)
    }

    // ========== TESTS DE FORMATEO DE TIMESTAMP ==========

    @Test
    fun `formatTimestamp con timestamp valido retorna fecha legible`() {
        val timestamp = "1730100000000" // Ejemplo fijo
        val result = formatTimestamp(timestamp)
        assertTrue(result.contains("/"))
        assertTrue(result.contains(":"))
    }

    @Test
    fun `formatTimestamp con texto invalido retorna Audio`() {
        val result = formatTimestamp("no_es_valido")
        assertEquals("Audio", result)
    }

    // ========== TESTS DE EXTENSION DE ARCHIVO ==========

    @Test
    fun `getFileExtension con nombre m4a retorna m4a`() {
        val result = getFileExtension("recording_123.m4a")
        assertEquals("m4a", result)
    }

    @Test
    fun `getFileExtension sin extension retorna m4a por defecto`() {
        val result = getFileExtension("recording_123")
        assertEquals("m4a", result)
    }

    @Test
    fun `getFileExtension con null retorna m4a por defecto`() {
        val result = getFileExtension(null)
        assertEquals("m4a", result)
    }

    // ========== TESTS DE COLOR SEGÚN CANTIDAD DE CARACTERES ==========

    @Test
    fun `getCharacterCountColor menor a 360 retorna gris`() {
        val color = getCharacterCountColor(100)
        assertEquals(0, color)
    }

    @Test
    fun `getCharacterCountColor entre 360 y 400 retorna naranja`() {
        val color = getCharacterCountColor(380)
        assertEquals(1, color)
    }

    @Test
    fun `getCharacterCountColor mayor a 400 retorna rojo`() {
        val color = getCharacterCountColor(450)
        assertEquals(2, color)
    }
}
