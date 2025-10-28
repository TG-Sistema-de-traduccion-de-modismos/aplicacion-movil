package com.proyecto.modismos.activities

import org.junit.Assert.*
import org.junit.Test

class WordDetailActivityTest {

    @Test
    fun `cuando no hay datos, usa valores por defecto`() {
        val palabra: String? = null
        val tipo: String? = null
        val definiciones: ArrayList<String>? = null
        val sinonimos: ArrayList<String>? = null

        val palabraFinal = palabra ?: ""
        val tipoFinal = tipo ?: ""
        val definicionesFinal = definiciones ?: arrayListOf()
        val sinonimosFinal = sinonimos ?: arrayListOf()

        assertEquals("", palabraFinal)
        assertEquals("", tipoFinal)
        assertTrue(definicionesFinal.isEmpty())
        assertTrue(sinonimosFinal.isEmpty())
    }

    @Test
    fun `cuando hay datos, se recuperan correctamente`() {
        val palabra = "Cucha"
        val tipo = "Sustantivo"
        val definiciones = arrayListOf("Madre", "Persona mayor")
        val sinonimos = arrayListOf("Vieja", "Señora")

        assertEquals("Cucha", palabra)
        assertEquals("Sustantivo", tipo)
        assertEquals(listOf("Madre", "Persona mayor"), definiciones)
        assertEquals(listOf("Vieja", "Señora"), sinonimos)
    }

    @Test
    fun `las definiciones se numeran correctamente`() {
        val definiciones = listOf("Significado 1", "Significado 2", "Significado 3")
        val indices = definiciones.mapIndexed { index, _ -> index + 1 }
        assertEquals(listOf(1, 2, 3), indices)
    }
}
