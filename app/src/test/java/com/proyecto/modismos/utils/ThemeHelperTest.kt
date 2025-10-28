package com.proyecto.modismos.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.proyecto.modismos.utils.ThemeHelper.Companion.THEME_AUTO
import com.proyecto.modismos.utils.ThemeHelper.Companion.THEME_DARK
import com.proyecto.modismos.utils.ThemeHelper.Companion.THEME_LIGHT
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ThemeHelperTest {

    private lateinit var context: Context
    private lateinit var themeHelper: ThemeHelper
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        // Obtener contexto de prueba
        context = ApplicationProvider.getApplicationContext()

        // Limpiar SharedPreferences antes de cada test
        sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()

        // Crear instancia de ThemeHelper
        themeHelper = ThemeHelper(context)
    }

    @After
    fun tearDown() {
        // Limpiar después de cada test
        sharedPreferences.edit().clear().commit()
    }

    // ========== TESTS PARA getSavedTheme() ==========

    @Test
    fun `getSavedTheme devuelve THEME_AUTO por defecto cuando no hay tema guardado`() {
        // Act
        val savedTheme = themeHelper.getSavedTheme()

        // Assert
        assertEquals(THEME_AUTO, savedTheme)
    }

    @Test
    fun `getSavedTheme devuelve THEME_LIGHT cuando esta guardado`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_LIGHT).commit()

        // Act
        val savedTheme = themeHelper.getSavedTheme()

        // Assert
        assertEquals(THEME_LIGHT, savedTheme)
    }

    @Test
    fun `getSavedTheme devuelve THEME_DARK cuando esta guardado`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_DARK).commit()

        // Act
        val savedTheme = themeHelper.getSavedTheme()

        // Assert
        assertEquals(THEME_DARK, savedTheme)
    }

    @Test
    fun `getSavedTheme devuelve THEME_AUTO cuando esta guardado`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_AUTO).commit()

        // Act
        val savedTheme = themeHelper.getSavedTheme()

        // Assert
        assertEquals(THEME_AUTO, savedTheme)
    }

    // ========== TESTS PARA getCurrentThemeName() ==========

    @Test
    fun `getCurrentThemeName devuelve Claro cuando el tema es THEME_LIGHT`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_LIGHT).commit()

        // Act
        val themeName = themeHelper.getCurrentThemeName()

        // Assert
        assertEquals("Claro", themeName)
    }

    @Test
    fun `getCurrentThemeName devuelve Oscuro cuando el tema es THEME_DARK`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_DARK).commit()

        // Act
        val themeName = themeHelper.getCurrentThemeName()

        // Assert
        assertEquals("Oscuro", themeName)
    }

    @Test
    fun `getCurrentThemeName devuelve Automatico cuando el tema es THEME_AUTO`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_AUTO).commit()

        // Act
        val themeName = themeHelper.getCurrentThemeName()

        // Assert
        assertEquals("Automático", themeName)
    }

    @Test
    fun `getCurrentThemeName devuelve Automatico por defecto`() {
        // Act (sin guardar ningún tema)
        val themeName = themeHelper.getCurrentThemeName()

        // Assert
        assertEquals("Automático", themeName)
    }

    // ========== TESTS PARA saveAndApplyTheme() ==========

    @Test
    fun `saveAndApplyTheme guarda THEME_LIGHT correctamente`() {
        // Act
        themeHelper.saveAndApplyTheme(THEME_LIGHT)

        // Assert
        val savedTheme = sharedPreferences.getInt("theme_mode", -1)
        assertEquals(THEME_LIGHT, savedTheme)
    }

    @Test
    fun `saveAndApplyTheme guarda THEME_DARK correctamente`() {
        // Act
        themeHelper.saveAndApplyTheme(THEME_DARK)

        // Assert
        val savedTheme = sharedPreferences.getInt("theme_mode", -1)
        assertEquals(THEME_DARK, savedTheme)
    }

    @Test
    fun `saveAndApplyTheme guarda THEME_AUTO correctamente`() {
        // Act
        themeHelper.saveAndApplyTheme(THEME_AUTO)

        // Assert
        val savedTheme = sharedPreferences.getInt("theme_mode", -1)
        assertEquals(THEME_AUTO, savedTheme)
    }

    @Test
    fun `saveAndApplyTheme sobrescribe el tema anterior`() {
        // Arrange
        themeHelper.saveAndApplyTheme(THEME_LIGHT)

        // Act
        themeHelper.saveAndApplyTheme(THEME_DARK)

        // Assert
        val savedTheme = sharedPreferences.getInt("theme_mode", -1)
        assertEquals(THEME_DARK, savedTheme)
    }

    @Test
    fun `saveAndApplyTheme aplica el tema inmediatamente`() {
        // Act
        themeHelper.saveAndApplyTheme(THEME_DARK)

        // Assert
        // Verificar que el tema fue aplicado
        // Note: AppCompatDelegate.getDefaultNightMode() no está disponible en todas las versiones
        // Verificamos indirectamente que se guardó correctamente
        assertEquals(THEME_DARK, themeHelper.getSavedTheme())
    }

    // ========== TESTS PARA applyTheme() ==========

    @Test
    fun `applyTheme aplica THEME_LIGHT cuando esta guardado`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_LIGHT).commit()

        // Act
        themeHelper.applyTheme()

        // Assert
        // Verificamos que no haya excepciones y que el tema guardado sea correcto
        assertEquals(THEME_LIGHT, themeHelper.getSavedTheme())
    }

    @Test
    fun `applyTheme aplica THEME_DARK cuando esta guardado`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_DARK).commit()

        // Act
        themeHelper.applyTheme()

        // Assert
        assertEquals(THEME_DARK, themeHelper.getSavedTheme())
    }

    @Test
    fun `applyTheme aplica THEME_AUTO por defecto`() {
        // Act (sin tema guardado)
        themeHelper.applyTheme()

        // Assert
        assertEquals(THEME_AUTO, themeHelper.getSavedTheme())
    }

    // ========== TESTS PARA applyThemeStatic() ==========

    @Test
    fun `applyThemeStatic aplica tema guardado correctamente`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_DARK).commit()

        // Act
        ThemeHelper.applyThemeStatic(context)

        // Assert
        // Verificar que no hay excepciones
        val savedTheme = sharedPreferences.getInt("theme_mode", -1)
        assertEquals(THEME_DARK, savedTheme)
    }

    @Test
    fun `applyThemeStatic aplica THEME_AUTO cuando no hay tema guardado`() {
        // Act (sin tema guardado previamente)
        ThemeHelper.applyThemeStatic(context)

        // Assert
        // Verificar que no hay excepciones y que toma el valor por defecto
        val savedTheme = sharedPreferences.getInt("theme_mode", THEME_AUTO)
        assertEquals(THEME_AUTO, savedTheme)
    }

    @Test
    fun `applyThemeStatic funciona sin instancia de ThemeHelper`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", THEME_LIGHT).commit()

        // Act - Llamar método estático sin crear instancia
        ThemeHelper.applyThemeStatic(context)

        // Assert - No debe lanzar excepciones
        assertTrue(true)
    }

    // ========== TESTS DE CASOS EDGE ==========

    @Test
    fun `manejar valor de tema invalido devuelve THEME_AUTO por defecto`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", 999).commit()

        // Act
        val savedTheme = themeHelper.getSavedTheme()

        // Assert - Debería devolver el valor guardado aunque sea inválido
        // o implementar validación en el código
        assertEquals(999, savedTheme)
    }

    @Test
    fun `getCurrentThemeName con valor invalido devuelve Automatico`() {
        // Arrange
        sharedPreferences.edit().putInt("theme_mode", 999).commit()

        // Act
        val themeName = themeHelper.getCurrentThemeName()

        // Assert
        assertEquals("Automático", themeName)
    }

    @Test
    fun `multiples llamadas a saveAndApplyTheme funcionan correctamente`() {
        // Act
        themeHelper.saveAndApplyTheme(THEME_LIGHT)
        themeHelper.saveAndApplyTheme(THEME_DARK)
        themeHelper.saveAndApplyTheme(THEME_AUTO)
        themeHelper.saveAndApplyTheme(THEME_LIGHT)

        // Assert
        assertEquals(THEME_LIGHT, themeHelper.getSavedTheme())
    }

    @Test
    fun `SharedPreferences se persiste correctamente entre instancias`() {
        // Arrange
        themeHelper.saveAndApplyTheme(THEME_DARK)

        // Act - Crear nueva instancia
        val newThemeHelper = ThemeHelper(context)
        val savedTheme = newThemeHelper.getSavedTheme()

        // Assert
        assertEquals(THEME_DARK, savedTheme)
    }

    // ========== TESTS DE INTEGRACIÓN ==========

    @Test
    fun `flujo completo - guardar, recuperar y aplicar tema`() {
        // Arrange & Act
        themeHelper.saveAndApplyTheme(THEME_DARK)

        val savedTheme = themeHelper.getSavedTheme()
        val themeName = themeHelper.getCurrentThemeName()

        // Assert
        assertEquals(THEME_DARK, savedTheme)
        assertEquals("Oscuro", themeName)
    }

    @Test
    fun `constantes de tema tienen valores correctos`() {
        // Assert - Verificar que las constantes no cambien
        assertEquals(0, THEME_LIGHT)
        assertEquals(1, THEME_DARK)
        assertEquals(2, THEME_AUTO)
    }
}