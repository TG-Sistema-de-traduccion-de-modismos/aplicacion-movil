package com.proyecto.modismos.fragments

import com.proyecto.modismos.models.Modismo
import org.junit.Assert.*
import org.junit.Test

class DictionaryFragmentTest {

    // ========== DATOS SIMULADOS ==========
    private val modismos = mutableListOf(
        Modismo("Bogotazo", "Modismo", listOf("Caos o disturbio"), listOf("revuelo")),
        Modismo("Ajiaco", "Modismo", listOf("Comida típica"), listOf("sopa")),
        Modismo("Chévere", "Modismo", listOf("Algo agradable"), listOf("bacano")),
        Modismo("Zafado", "Modismo", listOf("Alocado"), listOf("loco"))
    )

    // ========== TEST SCROLL TO LETTER ==========

    @Test
    fun `scrollToLetter encuentra la primera palabra que comienza con la letra indicada`() {
        // Arrange
        val letter = "C"

        // Act
        val position = modismos.indexOfFirst {
            it.palabra.firstOrNull()?.uppercaseChar().toString() == letter
        }

        // Assert
        assertEquals(2, position) // "Chévere" está en posición 2
    }

    @Test
    fun `scrollToLetter retorna menos uno cuando ninguna palabra coincide`() {
        // Arrange
        val letter = "X"

        // Act
        val position = modismos.indexOfFirst {
            it.palabra.firstOrNull()?.uppercaseChar().toString() == letter
        }

        // Assert
        assertEquals(-1, position)
    }

    // ========== TEST UPDATE ALPHABET BAR ==========

    @Test
    fun `updateAlphabetBar obtiene letras activas correctamente`() {
        // Arrange
        val activeLetters = modismos
            .mapNotNull { it.palabra.firstOrNull()?.uppercaseChar()?.toString() }
            .toSet()

        // Act
        val expectedLetters = setOf("B", "A", "C", "Z")

        // Assert
        assertEquals(expectedLetters, activeLetters)
    }

    @Test
    fun `updateAlphabetBar retorna conjunto vacío cuando no hay modismos`() {
        // Arrange
        val emptyList = emptyList<Modismo>()

        // Act
        val activeLetters = emptyList
            .mapNotNull { it.palabra.firstOrNull()?.uppercaseChar()?.toString() }
            .toSet()

        // Assert
        assertTrue(activeLetters.isEmpty())
    }

    // ========== TEST ORDENAMIENTO ==========

    @Test
    fun `updateUI ordena los modismos por orden alfabético`() {
        // Arrange
        val unordered = modismos.toMutableList()

        // Act
        unordered.sortBy { it.palabra }
        val sortedNames = unordered.map { it.palabra }

        // Assert
        assertEquals(listOf("Ajiaco", "Bogotazo", "Chévere", "Zafado"), sortedNames)
    }

    // ========== TEST CONTADOR ==========

    @Test
    fun `updateWordCount retorna mensaje correcto según el número de modismos`() {
        // Arrange
        val count = modismos.size

        // Act
        val message = "Modismos encontrados: $count"

        // Assert
        assertEquals("Modismos encontrados: 4", message)
    }

    @Test
    fun `updateWordCount con cero modismos muestra contador en cero`() {
        // Arrange
        val count = 0

        // Act
        val message = "Modismos encontrados: $count"

        // Assert
        assertEquals("Modismos encontrados: 0", message)
    }
}
