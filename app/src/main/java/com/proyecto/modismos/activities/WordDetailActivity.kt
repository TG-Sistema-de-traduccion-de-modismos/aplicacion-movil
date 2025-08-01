package com.proyecto.modismos.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.proyecto.modismos.R

class WordDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)
        supportActionBar?.hide()

        setupTransparentBars()

        // Obtener datos del intent
        val palabra = intent.getStringExtra("palabra") ?: ""
        val tipo = intent.getStringExtra("tipo") ?: ""
        val definiciones = intent.getStringArrayListExtra("definiciones") ?: arrayListOf()
        val sinonimos = intent.getStringArrayListExtra("sinonimos") ?: arrayListOf()

        setupUI(palabra, tipo, definiciones, sinonimos)
        setupCloseButton()
    }

    private fun setupUI(palabra: String, tipo: String, definiciones: List<String>, sinonimos: List<String>) {
        // Configurar título
        findViewById<TextView>(R.id.tv_word_title).text = palabra
        findViewById<TextView>(R.id.tv_word_type).text = tipo

        // Configurar definiciones con nuevo diseño
        val definicionesContainer = findViewById<LinearLayout>(R.id.definiciones_container)
        definiciones.forEachIndexed { index, definicion ->
            val definicionView = LayoutInflater.from(this)
                .inflate(R.layout.item_definition, definicionesContainer, false)

            val numeroTextView = definicionView.findViewById<TextView>(R.id.btnNumeroDefinicion)
            val definicionTextView = definicionView.findViewById<TextView>(R.id.tv_definicion)

            numeroTextView.text = (index + 1).toString()
            definicionTextView.text = definicion

            definicionesContainer.addView(definicionView)
        }

        // Configurar sinónimos con nuevo diseño
        val sinonimosContainer = findViewById<LinearLayout>(R.id.sinonimos_container)
        sinonimos.forEach { sinonimo ->
            val sinonimoView = LayoutInflater.from(this)
                .inflate(R.layout.item_sinonimo, sinonimosContainer, false)

            val textView = sinonimoView.findViewById<TextView>(R.id.tv_sinonimo)
            textView.text = sinonimo

            sinonimosContainer.addView(sinonimoView)
        }
    }

    private fun setupTransparentBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        val isNightMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        insetsController.isAppearanceLightStatusBars = !isNightMode
        insetsController.isAppearanceLightNavigationBars = !isNightMode
    }

    private fun setupCloseButton() {
        findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            finish()
        }
    }
}