package com.proyecto.modismos.activities

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.ModismosAdapter
import com.proyecto.modismos.models.Modismo
import org.json.JSONObject
import java.io.File
import java.util.Locale

class AnalysisActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AnalysisActivity"
        private const val EXTRA_API_RESPONSE = "api_response"
        private const val EXTRA_TEXT_CONTENT = "text_content"
        private const val EXTRA_AUDIO_PATH = "audio_path"
    }

    private lateinit var tvTextContent: TextView
    private lateinit var audioPlayerCard: LinearLayout
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var seekBarAudio: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var recyclerViewModismos: RecyclerView
    private lateinit var btnClose: ImageView
    private lateinit var btnOriginal: TextView
    private lateinit var btnNeutral: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private lateinit var modismosAdapter: ModismosAdapter

    private var textContent: String = ""
    private var audioPath: String? = null
    private var isNeutralSelected = false
    private var detectedModismos: List<Modismo> = emptyList()
    private var neutralizedSentence: String = ""

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Mapa para guardar las definiciones que detectó BETO
    private val betoDefinitions = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        Log.d(TAG, "=== AnalysisActivity iniciada ===")

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Ocultar ActionBar y configurar barras transparentes
        supportActionBar?.hide()
        setupTransparentBars()

        // Inicializar vistas PRIMERO
        initViews()
        setupClickListeners()
        setupRecyclerView()

        // DESPUÉS obtener datos del intent y parsear
        getIntentData()

        // Procesar respuesta de API
        processApiResponse()

        updateToggleButtons()
    }

    private fun getIntentData() {
        val apiResponse = intent.getStringExtra(EXTRA_API_RESPONSE) ?: ""
        textContent = intent.getStringExtra(EXTRA_TEXT_CONTENT) ?: ""
        audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH)

        Log.d(TAG, "Intent data:")
        Log.d(TAG, "  textContent: $textContent")
        Log.d(TAG, "  audioPath: $audioPath")
        Log.d(TAG, "  apiResponse length: ${apiResponse.length}")

        // Parsear respuesta de API
        if (apiResponse.isNotEmpty()) {
            parseApiResponse(apiResponse)
        }
    }

    private fun initViews() {
        tvTextContent = findViewById(R.id.tvTextContent)
        audioPlayerCard = findViewById(R.id.audioPlayerCard)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBarAudio = findViewById(R.id.seekBarAudio)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        recyclerViewModismos = findViewById(R.id.recyclerViewModismos)
        btnClose = findViewById(R.id.btn_close)
        btnOriginal = findViewById(R.id.btnOriginal)
        btnNeutral = findViewById(R.id.btnNeutral)

        Log.d(TAG, "Vistas inicializadas correctamente")
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            finish()
        }

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnOriginal.setOnClickListener {
            if (isNeutralSelected) {
                isNeutralSelected = false
                updateToggleButtons()
                updateContent()
            }
        }

        btnNeutral.setOnClickListener {
            if (!isNeutralSelected) {
                isNeutralSelected = true
                updateToggleButtons()
                updateContent()
            }
        }

        seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    updateCurrentTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupRecyclerView() {
        modismosAdapter = ModismosAdapter(emptyList())
        recyclerViewModismos.apply {
            layoutManager = LinearLayoutManager(this@AnalysisActivity)
            adapter = modismosAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun parseApiResponse(jsonResponse: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)

            Log.d(TAG, "=== PARSEANDO RESPUESTA DE API ===")
            Log.d(TAG, "JSON completo: $jsonResponse")

            // Obtener frase neutral
            neutralizedSentence = jsonObject.optString("frase_neutral", "")
            Log.d(TAG, "Frase neutral: $neutralizedSentence")

            // Limpiar mapa de definiciones de BETO
            betoDefinitions.clear()

            val palabrasDetectadas = mutableListOf<String>()

            // Procesar modismos detallados PRIMERO (prioridad)
            if (jsonObject.has("modismos_detallados")) {
                val modismosArray = jsonObject.getJSONArray("modismos_detallados")
                Log.d(TAG, "Encontrado modismos_detallados con ${modismosArray.length()} elementos")

                for (i in 0 until modismosArray.length()) {
                    val modismoObj = modismosArray.getJSONObject(i)
                    val palabra = modismoObj.optString("palabra", "")
                    val significadoDetectado = modismoObj.optString("significado_detectado", "")

                    if (palabra.isNotEmpty()) {
                        palabrasDetectadas.add(palabra)
                        // Guardar la definición que detectó BETO
                        betoDefinitions[palabra.lowercase()] = significadoDetectado
                        Log.d(TAG, "BETO detectó '$palabra': '$significadoDetectado'")
                    }
                }

                Log.d(TAG, "Palabras detectadas de modismos_detallados: $palabrasDetectadas")
            }
            // Fallback: procesar modismos_detected si no hay detallados
            else if (jsonObject.has("modismos_detected")) {
                val modismosObject = jsonObject.getJSONObject("modismos_detected")
                Log.d(TAG, "Usando fallback: modismos_detected")

                val keys = modismosObject.keys()
                while (keys.hasNext()) {
                    val palabra = keys.next()
                    val significado = modismosObject.getString(palabra)

                    if (palabra.isNotEmpty()) {
                        palabrasDetectadas.add(palabra)
                        // Guardar la definición que detectó BETO
                        betoDefinitions[palabra.lowercase()] = significado
                        Log.d(TAG, "BETO detectó '$palabra': '$significado'")
                    }
                }

                Log.d(TAG, "Palabras detectadas de modismos_detected: $palabrasDetectadas")
            } else {
                Log.w(TAG, "No se encontraron ni modismos_detallados ni modismos_detected")
            }

            // Filtrar palabras que realmente aparecen en la frase
            val palabrasEnFrase = palabrasDetectadas
            Log.d(TAG, "Palabras filtradas presentes en la frase: $palabrasEnFrase")

            // Cargar datos completos desde Firebase
            if (palabrasEnFrase.isNotEmpty()) {
                loadModismosFromFirestore(palabrasEnFrase)

                // Guardar palabras detectadas para el usuario
                saveDetectedWordsToFirestore(palabrasEnFrase)
            } else {
                Log.d(TAG, "No hay palabras presentes en la frase para cargar")
                updateRecyclerViewWithModismos(emptyList())
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta de API", e)
            e.printStackTrace()
            updateRecyclerViewWithModismos(emptyList())
        }
    }

    private fun filterWordsInSentence(palabras: List<String>, sentence: String): List<String> {
        val sentenceLower = sentence.lowercase()
        return palabras.filter { palabra ->
            sentenceLower.contains(palabra.lowercase())
        }.distinct() // Evitar duplicados
    }

    private fun loadModismosFromFirestore(palabras: List<String>) {
        if (palabras.isEmpty()) {
            Log.d(TAG, "No hay palabras para cargar desde Firebase")
            updateRecyclerViewWithModismos(emptyList())
            return
        }

        Log.d(TAG, "Cargando ${palabras.size} palabras desde Firebase: $palabras")

        val modismosFinales = mutableListOf<Modismo>()
        var loadedCount = 0
        val totalWords = palabras.size

        palabras.forEach { palabra ->
            firestore.collection("palabras")
                .whereEqualTo("palabra", capitalizeFirstLetter(palabra))
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]

                        Log.d(TAG, "Documento encontrado para '$palabra': ${doc.id}")

                        // Obtener la definición que BETO detectó para esta palabra
                        val definicionBeto = betoDefinitions[palabra.lowercase()] ?: ""

                        // Obtener sinónimos de Firebase
                        val sinonimos = doc.get("sinonimos") as? List<String> ?: emptyList()
                        val region = doc.getString("region") ?: "Colombia"
                        val tipo = doc.getString("tipo") ?: "Modismo"

                        val modismo = Modismo(
                            palabra = palabra,
                            tipo = tipo,
                            definiciones = listOf(definicionBeto), // Solo la definición de BETO
                            sinonimos = sinonimos
                        )

                        modismosFinales.add(modismo)

                        Log.d(TAG, "Modismo agregado: ${modismo.palabra} - Definición BETO: '$definicionBeto'")
                    } else {
                        Log.w(TAG, "No se encontró documento para la palabra: $palabra")

                        // Si no está en Firebase, usar solo lo que detectó BETO
                        val definicionBeto = betoDefinitions[palabra.lowercase()] ?: ""
                        if (definicionBeto.isNotEmpty()) {
                            val modismo = Modismo(
                                palabra = palabra,
                                tipo = "Modismo",
                                definiciones = listOf(definicionBeto),
                                sinonimos = emptyList()
                            )
                            modismosFinales.add(modismo)
                            Log.d(TAG, "Modismo agregado (sin Firebase): ${modismo.palabra}")
                        }
                    }

                    loadedCount++
                    Log.d(TAG, "Progreso de carga: $loadedCount/$totalWords")

                    // Cuando se hayan cargado todas las palabras
                    if (loadedCount == totalWords) {
                        Log.d(TAG, "Carga completa. Total modismos: ${modismosFinales.size}")
                        detectedModismos = modismosFinales
                        updateRecyclerViewWithModismos(modismosFinales)

                        // Actualizar el contenido con highlighting
                        updateContent()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error cargando palabra '$palabra' desde Firebase", e)

                    // Si hay error, usar solo lo que detectó BETO
                    val definicionBeto = betoDefinitions[palabra.lowercase()] ?: ""
                    if (definicionBeto.isNotEmpty()) {
                        val modismo = Modismo(
                            palabra = palabra,
                            tipo = "Modismo",
                            definiciones = listOf(definicionBeto),
                            sinonimos = emptyList()
                        )
                        modismosFinales.add(modismo)
                        Log.d(TAG, "Modismo agregado (error Firebase): ${modismo.palabra}")
                    }

                    loadedCount++

                    if (loadedCount == totalWords) {
                        Log.d(TAG, "Carga completa con errores. Total modismos: ${modismosFinales.size}")
                        detectedModismos = modismosFinales
                        updateRecyclerViewWithModismos(modismosFinales)
                        updateContent()
                    }
                }
        }
    }

    private fun saveDetectedWordsToFirestore(palabras: List<String>) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "Usuario no autenticado, no se guardarán las palabras")
            return
        }

        if (palabras.isEmpty()) {
            Log.d(TAG, "No hay palabras para guardar")
            return
        }

        val uid = currentUser.uid
        Log.d(TAG, "Guardando ${palabras.size} palabras para usuario: $uid")

        // Capitalizar palabras
        val palabrasCapitalizadas = palabras.map { capitalizeFirstLetter(it) }
        val userDocRef = firestore.collection("usuarios").document(uid)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)

            if (snapshot.exists()) {
                // Usuario existe, agregar solo palabras nuevas
                val currentPalabras = snapshot.get("palabras") as? List<String> ?: emptyList()
                val palabrasExistentes = currentPalabras.map { it.lowercase() }.toSet()

                val nuevasPalabras = palabrasCapitalizadas.filter { palabra ->
                    !palabrasExistentes.contains(palabra.lowercase())
                }

                if (nuevasPalabras.isNotEmpty()) {
                    transaction.update(userDocRef, "palabras", FieldValue.arrayUnion(*nuevasPalabras.toTypedArray()))
                    Log.d(TAG, "Agregando ${nuevasPalabras.size} palabras nuevas: $nuevasPalabras")
                }

                nuevasPalabras.size
            } else {
                // Usuario nuevo, crear documento
                val userData = mapOf(
                    "uid" to uid,
                    "email" to (currentUser.email ?: ""),
                    "palabras" to palabrasCapitalizadas
                )
                transaction.set(userDocRef, userData)
                Log.d(TAG, "Creando nuevo documento de usuario con ${palabrasCapitalizadas.size} palabras")
                palabrasCapitalizadas.size
            }
        }.addOnSuccessListener { nuevasAgregadas ->
            if (nuevasAgregadas > 0) {
                Log.d(TAG, "✅ $nuevasAgregadas palabras nuevas guardadas exitosamente")
                Toast.makeText(
                    this,
                    "¡$nuevasAgregadas palabra(s) nueva(s) agregada(s) a tu diccionario!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.d(TAG, "No se agregaron palabras nuevas (ya estaban en el diccionario)")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "❌ Error guardando palabras en Firebase", e)
            Toast.makeText(this, "Error al guardar palabras", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processApiResponse() {
        // Configurar audio si existe
        if (!audioPath.isNullOrEmpty()) {
            audioPlayerCard.isVisible = true
            setupAudioPlayer(audioPath!!)
        } else {
            audioPlayerCard.isVisible = false
        }

        // Mostrar contenido inicial
        updateContent()
    }

    private fun updateToggleButtons() {
        if (isNeutralSelected) {
            btnNeutral.apply {
                setBackgroundResource(R.drawable.toggle_button_selected)
                setTextColor(getColor(R.color.white))
                animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(150).start()
            }
            btnOriginal.apply {
                setBackgroundResource(R.drawable.toggle_button_unselected)
                setTextColor(getColor(R.color.gray))
                animate().scaleX(0.95f).scaleY(0.95f).alpha(0.7f).setDuration(150).start()
            }
        } else {
            btnOriginal.apply {
                setBackgroundResource(R.drawable.toggle_button_selected)
                setTextColor(getColor(R.color.white))
                animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(150).start()
            }
            btnNeutral.apply {
                setBackgroundResource(R.drawable.toggle_button_unselected)
                setTextColor(getColor(R.color.gray))
                animate().scaleX(0.95f).scaleY(0.95f).alpha(0.7f).setDuration(150).start()
            }
        }
    }

    private fun updateContent() {
        tvTextContent.animate().alpha(0.3f).setDuration(100).withEndAction {
            if (isNeutralSelected) {
                tvTextContent.text = neutralizedSentence
            } else {
                tvTextContent.text = highlightDetectedWords(textContent)
            }
            tvTextContent.animate().alpha(1.0f).setDuration(150).start()
        }.start()
    }

    private fun highlightDetectedWords(text: String): SpannableString {
        val spannable = SpannableString(text)

        if (detectedModismos.isEmpty() || text.isBlank()) {
            return spannable
        }

        val highlightColor = ContextCompat.getColor(this, R.color.highlight_word)

        for (modismo in detectedModismos) {
            val palabra = modismo.palabra
            val textLower = text.lowercase()
            val palabraLower = palabra.lowercase()

            // Buscar coincidencias exactas primero
            var startIndex = 0
            while (startIndex < text.length) {
                val index = textLower.indexOf(palabraLower, startIndex)
                if (index == -1) break

                val endIndex = index + palabra.length

                spannable.setSpan(
                    ForegroundColorSpan(highlightColor),
                    index,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    index,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                startIndex = endIndex
            }

            // Si no hubo coincidencias exactas, buscar palabras que comiencen con la raíz
            if (startIndex == 0) {
                // Obtener raíz de la palabra (mínimo 4 caracteres para evitar falsos positivos)
                val raiz = if (palabraLower.length > 4) {
                    palabraLower.substring(0, palabraLower.length - 2)
                } else {
                    palabraLower
                }

                // Buscar palabras en el texto que comiencen con la raíz
                val palabrasEnTexto = text.split(Regex("\\s+"))
                var currentPosition = 0

                for (palabraTexto in palabrasEnTexto) {
                    val palabraTextoLower = palabraTexto.lowercase()
                        .replace(Regex("[^a-záéíóúñ]"), "") // Quitar puntuación

                    // Verificar si la palabra comienza con la raíz
                    if (palabraTextoLower.startsWith(raiz) && palabraTextoLower.length >= raiz.length) {
                        // Encontrar la posición exacta en el texto original
                        val posicionEnTexto = textLower.indexOf(palabraTextoLower, currentPosition)

                        if (posicionEnTexto != -1) {
                            // Encontrar los límites exactos de la palabra (considerando puntuación)
                            var startWord = posicionEnTexto
                            var endWord = posicionEnTexto + palabraTextoLower.length

                            // Ajustar para incluir la palabra completa sin puntuación
                            while (endWord < text.length && text[endWord].isLetter()) {
                                endWord++
                            }

                            spannable.setSpan(
                                ForegroundColorSpan(highlightColor),
                                startWord,
                                endWord,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            spannable.setSpan(
                                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                startWord,
                                endWord,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }

                    // Actualizar posición para la siguiente búsqueda
                    currentPosition = textLower.indexOf(palabraTexto.lowercase(), currentPosition) + palabraTexto.length
                }
            }
        }

        return spannable
    }

    private fun updateRecyclerViewWithModismos(modismos: List<Modismo>) {
        Log.d(TAG, "Actualizando RecyclerView con ${modismos.size} modismos")

        modismosAdapter = ModismosAdapter(modismos)
        recyclerViewModismos.adapter = modismosAdapter

        // Hacer visible el RecyclerView
        recyclerViewModismos.isVisible = modismos.isNotEmpty()

        if (modismos.isNotEmpty()) {
            Log.d(TAG, "Modismos para mostrar:")
            modismos.forEachIndexed { index, modismo ->
                Log.d(TAG, "  [$index] ${modismo.palabra} - Def: ${modismo.definiciones.firstOrNull()}")
            }
        }
    }

    private fun capitalizeFirstLetter(text: String): String {
        return if (text.isNotEmpty()) {
            text.substring(0, 1).uppercase(Locale.getDefault()) + text.substring(1)
        } else {
            text
        }
    }

    private fun setupAudioPlayer(path: String) {
        try {
            releaseMediaPlayer()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()

                setOnCompletionListener {
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                    seekBarAudio.progress = 0
                    updateCurrentTime(0)
                    stopProgressUpdate()
                    this@AnalysisActivity.isPlaying = false
                }

                setOnPreparedListener {
                    val duration = duration
                    seekBarAudio.max = duration
                    updateTotalTime(duration)
                    updateCurrentTime(0)
                }
            }

            btnPlayPause.setImageResource(R.drawable.ic_play)
            isPlaying = false

            Log.d(TAG, "Reproductor configurado: $path")

        } catch (e: Exception) {
            Log.e(TAG, "Error configurando reproductor", e)
            audioPlayerCard.isVisible = false
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                btnPlayPause.setImageResource(R.drawable.ic_play)
                stopProgressUpdate()
            } else {
                player.start()
                btnPlayPause.setImageResource(R.drawable.ic_pause)
                startProgressUpdate()
            }
            isPlaying = !isPlaying
        }
    }

    private fun startProgressUpdate() {
        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition
                        seekBarAudio.progress = currentPosition
                        updateCurrentTime(currentPosition)
                        updateHandler.postDelayed(this, 100)
                    }
                }
            }
        }
        updateRunnable?.let { updateHandler.post(it) }
    }

    private fun stopProgressUpdate() {
        updateRunnable?.let {
            updateHandler.removeCallbacks(it)
            updateRunnable = null
        }
    }

    private fun updateCurrentTime(timeMs: Int) {
        tvCurrentTime.text = formatTime(timeMs)
    }

    private fun updateTotalTime(timeMs: Int) {
        tvTotalTime.text = formatTime(timeMs)
    }

    private fun formatTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
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

    private fun releaseMediaPlayer() {
        stopProgressUpdate()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            togglePlayPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "=== AnalysisActivity destruida ===")
        releaseMediaPlayer()
    }
}