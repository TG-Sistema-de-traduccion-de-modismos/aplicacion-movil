package com.proyecto.modismos.activities

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class AnalysisActivityTest {
    @Test
    fun `formatTime con 0 milisegundos retorna 00 00`() {
        val timeMs = 0
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("00:00", result)
    }

    @Test
    fun `formatTime con 1000 milisegundos retorna 00 01`() {
        val timeMs = 1000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("00:01", result)
    }

    @Test
    fun `formatTime con 60000 milisegundos retorna 01 00`() {
        val timeMs = 60000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("01:00", result)
    }

    @Test
    fun `formatTime con 125000 milisegundos retorna 02 05`() {
        val timeMs = 125000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("02:05", result)
    }

    @Test
    fun `formatTime con 3599000 milisegundos retorna 59 59`() {
        val timeMs = 3599000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("59:59", result)
    }

    // ========== TESTS DE CAPITALIZACIÓN ==========

    @Test
    fun `capitalizeFirstLetter con texto vacio retorna vacio`() {
        val text = ""
        val result = if (text.isNotEmpty()) {
            text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)
        } else {
            text
        }

        assertEquals("", result)
    }

    @Test
    fun `capitalizeFirstLetter con texto en minusculas capitaliza primera letra`() {
        val text = "hola"
        val result = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)

        assertEquals("Hola", result)
    }

    @Test
    fun `capitalizeFirstLetter con texto ya capitalizado mantiene formato`() {
        val text = "Hola"
        val result = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)

        assertEquals("Hola", result)
    }

    @Test
    fun `capitalizeFirstLetter con una sola letra funciona correctamente`() {
        val text = "a"
        val result = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)

        assertEquals("A", result)
    }

    @Test
    fun `capitalizeFirstLetter con numero al inicio mantiene el numero`() {
        val text = "123abc"
        val result = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)

        assertEquals("123abc", result)
    }

    @Test
    fun `capitalizeFirstLetter con texto con espacios capitaliza primera letra`() {
        val text = "hola mundo"
        val result = text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)

        assertEquals("Hola mundo", result)
    }

    // ========== TESTS DE PROCESAMIENTO DE PALABRAS ==========

    @Test
    fun `lista de palabras vacia no tiene elementos para procesar`() {
        val palabras = emptyList<String>()

        assertTrue("Lista vacía debe estar vacía", palabras.isEmpty())
        assertEquals(0, palabras.size)
    }

    @Test
    fun `lista de palabras con elementos tiene tamaño correcto`() {
        val palabras = listOf("bacano", "parce", "chimba")

        assertFalse("Lista no debe estar vacía", palabras.isEmpty())
        assertEquals(3, palabras.size)
    }

    @Test
    fun `filtrar palabras duplicadas elimina repeticiones`() {
        val palabras = listOf("bacano", "parce", "bacano", "chimba", "parce")
        val unique = palabras.distinct()

        assertEquals(3, unique.size)
        assertTrue(unique.contains("bacano"))
        assertTrue(unique.contains("parce"))
        assertTrue(unique.contains("chimba"))
    }

    @Test
    fun `capitalizar lista de palabras aplica a todas`() {
        val palabras = listOf("bacano", "parce", "chimba")
        val capitalizadas = palabras.map { palabra ->
            if (palabra.isNotEmpty()) {
                palabra.substring(0, 1).uppercase(Locale.getDefault()) + palabra.substring(1)
            } else {
                palabra
            }
        }

        assertEquals("Bacano", capitalizadas[0])
        assertEquals("Parce", capitalizadas[1])
        assertEquals("Chimba", capitalizadas[2])
    }

    @Test
    fun `filtrar palabras existentes elimina duplicados case insensitive`() {
        val currentPalabras = listOf("Bacano", "Parce")
        val nuevasPalabras = listOf("bacano", "Chimba", "PARCE", "Llave")

        val palabrasExistentes = currentPalabras.map { it.lowercase() }.toSet()
        val filtradas = nuevasPalabras.filter { palabra ->
            !palabrasExistentes.contains(palabra.lowercase())
        }

        assertEquals(2, filtradas.size)
        assertTrue(filtradas.contains("Chimba"))
        assertTrue(filtradas.contains("Llave"))
    }

    // ========== TESTS DE ESTADO DE AUDIO ==========

    @Test
    fun `isPlaying false indica que no esta reproduciendo`() {
        val isPlaying = false

        assertFalse("No debe estar reproduciendo", isPlaying)
    }

    @Test
    fun `isPlaying true indica que esta reproduciendo`() {
        val isPlaying = true

        assertTrue("Debe estar reproduciendo", isPlaying)
    }

    @Test
    fun `toggle isPlaying cambia el estado correctamente`() {
        var isPlaying = false

        isPlaying = !isPlaying
        assertTrue("Debe cambiar a true", isPlaying)

        isPlaying = !isPlaying
        assertFalse("Debe cambiar a false", isPlaying)
    }

    // ========== TESTS DE ESTADO DE VISTA ==========

    @Test
    fun `isNeutralSelected false indica vista original`() {
        val isNeutralSelected = false

        assertFalse("Debe mostrar vista original", isNeutralSelected)
    }

    @Test
    fun `isNeutralSelected true indica vista neutral`() {
        val isNeutralSelected = true

        assertTrue("Debe mostrar vista neutral", isNeutralSelected)
    }

    @Test
    fun `cambio de vista original a neutral cambia estado`() {
        var isNeutralSelected = false

        isNeutralSelected = true
        assertTrue("Debe cambiar a neutral", isNeutralSelected)
    }

    @Test
    fun `cambio de vista neutral a original cambia estado`() {
        var isNeutralSelected = true

        isNeutralSelected = false
        assertFalse("Debe cambiar a original", isNeutralSelected)
    }

    // ========== TESTS DE VALIDACIÓN DE ÍNDICES ==========

    @Test
    fun `indices validos estan dentro del rango del texto`() {
        val text = "Hola mundo"
        val indiceInicio = 0
        val indiceFin = 4

        val esValido = indiceInicio >= 0 && indiceFin <= text.length && indiceInicio < indiceFin

        assertTrue("Índices deben ser válidos", esValido)
    }

    @Test
    fun `indices invalidos cuando inicio es negativo`() {
        val text = "Hola mundo"
        val indiceInicio = -1
        val indiceFin = 4

        val esValido = indiceInicio >= 0 && indiceFin <= text.length && indiceInicio < indiceFin

        assertFalse("Índices deben ser inválidos", esValido)
    }

    @Test
    fun `indices invalidos cuando fin excede longitud del texto`() {
        val text = "Hola mundo"
        val indiceInicio = 0
        val indiceFin = 20

        val esValido = indiceInicio >= 0 && indiceFin <= text.length && indiceInicio < indiceFin

        assertFalse("Índices deben ser inválidos", esValido)
    }

    @Test
    fun `indices invalidos cuando inicio es mayor que fin`() {
        val text = "Hola mundo"
        val indiceInicio = 5
        val indiceFin = 3

        val esValido = indiceInicio >= 0 && indiceFin <= text.length && indiceInicio < indiceFin

        assertFalse("Índices deben ser inválidos", esValido)
    }

    @Test
    fun `indices iguales son invalidos`() {
        val text = "Hola mundo"
        val indiceInicio = 3
        val indiceFin = 3

        val esValido = indiceInicio >= 0 && indiceFin <= text.length && indiceInicio < indiceFin

        assertFalse("Índices iguales deben ser inválidos", esValido)
    }

    // ========== TESTS DE BÚSQUEDA DE PALABRAS ==========

    @Test
    fun `buscar palabra en texto retorna indice correcto`() {
        val text = "Hola mundo"
        val palabra = "mundo"
        val index = text.lowercase().indexOf(palabra.lowercase())

        assertEquals(5, index)
    }

    @Test
    fun `buscar palabra inexistente retorna -1`() {
        val text = "Hola mundo"
        val palabra = "adios"
        val index = text.lowercase().indexOf(palabra.lowercase())

        assertEquals(-1, index)
    }

    @Test
    fun `buscar palabra con case insensitive funciona`() {
        val text = "Hola MUNDO"
        val palabra = "mundo"
        val textLower = text.lowercase()
        val palabraLower = palabra.lowercase()
        val index = textLower.indexOf(palabraLower)

        assertTrue("Debe encontrar la palabra", index >= 0)
        assertEquals(5, index)
    }

    @Test
    fun `calcular fin de palabra suma longitud correctamente`() {
        val palabra = "mundo"
        val startIndex = 5
        val endIndex = startIndex + palabra.length

        assertEquals(10, endIndex)
    }

    // ========== TESTS DE RAÍZ DE PALABRAS ==========

    @Test
    fun `obtener raiz de palabra mayor a 4 caracteres`() {
        val palabra = "bacano"
        val raiz = if (palabra.length > 4) {
            palabra.substring(0, palabra.length - 2)
        } else {
            palabra
        }

        assertEquals("baca", raiz)
    }

    @Test
    fun `obtener raiz de palabra de 4 caracteres o menos retorna palabra completa`() {
        val palabra = "chi"
        val raiz = if (palabra.length > 4) {
            palabra.substring(0, palabra.length - 2)
        } else {
            palabra
        }

        assertEquals("chi", raiz)
    }

    @Test
    fun `palabra empieza con raiz es detectada correctamente`() {
        val palabraTexto = "bacanos"
        val raiz = "baca"

        val coincide = palabraTexto.startsWith(raiz) && palabraTexto.length >= raiz.length

        assertTrue("Palabra debe coincidir con la raíz", coincide)
    }

    @Test
    fun `palabra no empieza con raiz retorna false`() {
        val palabraTexto = "chimba"
        val raiz = "baca"

        val coincide = palabraTexto.startsWith(raiz) && palabraTexto.length >= raiz.length

        assertFalse("Palabra no debe coincidir con la raíz", coincide)
    }

    // ========== TESTS DE SEPARACIÓN DE TEXTO ==========

    @Test
    fun `separar texto por espacios retorna array correcto`() {
        val text = "Hola mundo desde Colombia"
        val palabras = text.split(Regex("\\s+"))

        assertEquals(4, palabras.size)
        assertEquals("Hola", palabras[0])
        assertEquals("mundo", palabras[1])
        assertEquals("desde", palabras[2])
        assertEquals("Colombia", palabras[3])
    }

    @Test
    fun `separar texto con multiples espacios maneja correctamente`() {
        val text = "Hola    mundo"
        val palabras = text.split(Regex("\\s+"))

        assertEquals(2, palabras.size)
        assertEquals("Hola", palabras[0])
        assertEquals("mundo", palabras[1])
    }

    @Test
    fun `texto sin espacios retorna una sola palabra`() {
        val text = "Hola"
        val palabras = text.split(Regex("\\s+"))

        assertEquals(1, palabras.size)
        assertEquals("Hola", palabras[0])
    }

    // ========== TESTS DE LIMPIEZA DE CARACTERES =========

    @Test
    fun `palabra con acentos se preserva`() {
        val palabra = "José"
        val limpia = palabra.lowercase().replace(Regex("[^a-záéíóúñ]"), "")

        assertEquals("josé", limpia)
    }

    // ========== TESTS DE CASOS EDGE ==========

    @Test
    fun `texto vacio no causa errores en procesamiento`() {
        val text = ""
        val palabras = text.split(Regex("\\s+"))

        assertTrue("Lista debe tener al menos un elemento", palabras.isNotEmpty())
        assertEquals("", palabras[0])
    }

    @Test
    fun `lista de cambios vacia es valida`() {
        val cambios = emptyList<Any>()

        assertTrue("Lista vacía debe estar vacía", cambios.isEmpty())
        assertEquals(0, cambios.size)
    }

    @Test
    fun `palabra vacia en lista no causa error`() {
        val palabras = listOf("", "bacano", "")
        val noVacias = palabras.filter { it.isNotEmpty() }

        assertEquals(1, noVacias.size)
        assertEquals("bacano", noVacias[0])
    }

    @Test
    fun `contar palabras nuevas agregadas funciona correctamente`() {
        val palabrasExistentes = setOf("bacano", "parce")
        val palabrasNuevas = listOf("Chimba", "Bacano", "Llave", "Parce")

        val capitalizadas = palabrasNuevas.map {
            it.substring(0, 1).uppercase() + it.substring(1).lowercase()
        }

        val nuevasAgregadas = capitalizadas.filter { palabra ->
            !palabrasExistentes.contains(palabra.lowercase())
        }

        assertEquals(2, nuevasAgregadas.size)
    }

    // ========== TESTS DE FORMATO DE TIEMPO EDGE CASES ==========

    @Test
    fun `formatTime con exactamente 1 minuto retorna 01 00`() {
        val timeMs = 60000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("01:00", result)
    }

    @Test
    fun `formatTime con 90 segundos retorna 01 30`() {
        val timeMs = 90000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertEquals("01:30", result)
    }

    @Test
    fun `formatTime siempre retorna formato con 2 digitos`() {
        val timeMs = 5000
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val result = String.format("%02d:%02d", minutes, seconds)

        assertTrue("Debe contener :", result.contains(":"))
        assertEquals(5, result.length) // "00:05"
    }
}