package com.proyecto.modismos.activities

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AudioRecorderActivityTest {

    companion object {
        const val MAX_RECORDING_TIME = 30000L // 30 segundos
    }

    // ---------- Helpers lógicos puros ----------

    private fun formatTimer(elapsedMillis: Long): String {
        val minutes = (elapsedMillis / 1000) / 60
        val seconds = (elapsedMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun isRecordingValid(file: File?, wasCancelled: Boolean): Boolean {
        if (wasCancelled) return false
        if (file == null || !file.exists()) return false
        if (file.length() <= 0) return false
        return true
    }

    private fun remainingTime(startTime: Long, currentTime: Long): Long {
        val elapsed = currentTime - startTime
        return (MAX_RECORDING_TIME - elapsed).coerceAtLeast(0)
    }

    private fun getRecordingState(isRecording: Boolean, isPaused: Boolean): String {
        return when {
            !isRecording -> "Inactivo"
            isPaused -> "Pausado"
            else -> "Grabando"
        }
    }

    // ========== TESTS DE FORMATEO DE TIEMPO ==========

    @Test
    fun `formatea correctamente el tiempo transcurrido`() {
        val result = formatTimer(12500) // 12.5 segundos
        assertEquals("00:12", result)
    }

    @Test
    fun `formatea minutos correctamente`() {
        val result = formatTimer(61000) // 61 segundos
        assertEquals("01:01", result)
    }

    // ========== TESTS DE TIEMPO RESTANTE ==========

    @Test
    fun `tiempo restante correcto al inicio`() {
        val result = remainingTime(startTime = 0L, currentTime = 0L)
        assertEquals(30000L, result)
    }

    @Test
    fun `tiempo restante no negativo incluso si excede el máximo`() {
        val result = remainingTime(startTime = 0L, currentTime = 40000L)
        assertEquals(0L, result)
    }

    // ========== TESTS DE VALIDACIÓN DE ARCHIVO ==========

    @Test
    fun `grabación válida cuando archivo existe y tiene datos`() {
        val tempFile = File.createTempFile("test_audio", ".m4a")
        tempFile.writeText("contenido")
        val result = isRecordingValid(tempFile, wasCancelled = false)
        assertTrue(result)
        tempFile.delete()
    }

    @Test
    fun `grabación inválida cuando fue cancelada`() {
        val result = isRecordingValid(File("test.m4a"), wasCancelled = true)
        assertFalse(result)
    }

    @Test
    fun `grabación inválida cuando archivo no existe`() {
        val file = File("no_existe.m4a")
        val result = isRecordingValid(file, wasCancelled = false)
        assertFalse(result)
    }

    @Test
    fun `grabación inválida cuando archivo vacío`() {
        val tempFile = File.createTempFile("vacio", ".m4a")
        val result = isRecordingValid(tempFile, wasCancelled = false)
        assertFalse(result)
        tempFile.delete()
    }

    // ========== TESTS DE ESTADOS DE GRABACIÓN ==========

    @Test
    fun `estado cuando no se graba es inactivo`() {
        val result = getRecordingState(isRecording = false, isPaused = false)
        assertEquals("Inactivo", result)
    }

    @Test
    fun `estado cuando se pausa es pausado`() {
        val result = getRecordingState(isRecording = true, isPaused = true)
        assertEquals("Pausado", result)
    }

    @Test
    fun `estado cuando se graba activamente es grabando`() {
        val result = getRecordingState(isRecording = true, isPaused = false)
        assertEquals("Grabando", result)
    }
}
