package com.proyecto.modismos.activities

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.ModismosAdapter
import com.proyecto.modismos.models.Modismo
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.Locale

class AnalysisActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AnalysisActivity"
        private const val EXTRA_ANALYSIS_TYPE = "analysis_type"
        private const val EXTRA_TEXT_CONTENT = "text_content"
        private const val EXTRA_AUDIO_PATH = "audio_path"
        private const val EXTRA_AUTO_TRANSCRIBE = "auto_transcribe"

        const val TYPE_TEXT = "text"
        const val TYPE_AUDIO = "audio"

        // CAMBIO: URL de la API Gateway actualizada (reemplaza con tu URL de ngrok)
        private const val API_GATEWAY_BASE_URL = "https://09a1faf165e5.ngrok-free.app"
        private const val TRANSCRIBE_ENDPOINT = "$API_GATEWAY_BASE_URL/transcribe"
        private const val ANALYZE_TEXT_ENDPOINT = "$API_GATEWAY_BASE_URL/analyze-text"
        private const val ANALYZE_AUDIO_ENDPOINT = "$API_GATEWAY_BASE_URL/analyze-audio"

        fun startTextAnalysis(context: Context, textContent: String) {
            val intent = Intent(context, AnalysisActivity::class.java).apply {
                putExtra(EXTRA_ANALYSIS_TYPE, TYPE_TEXT)
                putExtra(EXTRA_TEXT_CONTENT, textContent)
            }
            context.startActivity(intent)
        }

        fun startAudioAnalysis(context: Context, textContent: String, audioPath: String) {
            val intent = Intent(context, AnalysisActivity::class.java).apply {
                putExtra(EXTRA_ANALYSIS_TYPE, TYPE_AUDIO)
                putExtra(EXTRA_TEXT_CONTENT, textContent)
                putExtra(EXTRA_AUDIO_PATH, audioPath)
            }
            context.startActivity(intent)
        }

        fun startAudioAnalysisWithTranscription(context: Context, audioPath: String) {
            val intent = Intent(context, AnalysisActivity::class.java).apply {
                putExtra(EXTRA_ANALYSIS_TYPE, TYPE_AUDIO)
                putExtra(EXTRA_AUDIO_PATH, audioPath)
                putExtra(EXTRA_AUTO_TRANSCRIBE, true)
            }
            context.startActivity(intent)
        }
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
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvLoadingMessage: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private lateinit var modismosAdapter: ModismosAdapter

    private var analysisType: String = TYPE_TEXT
    private var textContent: String = ""
    private var audioPath: String? = null
    private var isNeutralSelected = false
    private var shouldAutoTranscribe = false
    private var isTranscribing = false

    // Variables para almacenar las respuestas completas
    private var lastApiResponse: String = ""
    private var detectedModismos: List<Modismo> = emptyList()

    // Cliente HTTP para las peticiones a la API Gateway
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

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

        // Obtener datos del intent
        getIntentData()

        // Inicializar vistas
        initViews()
        setupClickListeners()
        setupRecyclerView()
        loadContent()
        updateToggleButtons()
    }

    private fun getIntentData() {
        analysisType = intent.getStringExtra(EXTRA_ANALYSIS_TYPE) ?: TYPE_TEXT
        textContent = intent.getStringExtra(EXTRA_TEXT_CONTENT) ?: ""
        audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH)
        shouldAutoTranscribe = intent.getBooleanExtra(EXTRA_AUTO_TRANSCRIBE, false)

        Log.d(TAG, "Intent data:")
        Log.d(TAG, "  analysisType: $analysisType")
        Log.d(TAG, "  textContent: $textContent")
        Log.d(TAG, "  audioPath: $audioPath")
        Log.d(TAG, "  shouldAutoTranscribe: $shouldAutoTranscribe")
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
        progressIndicator = findViewById(R.id.progressIndicator)
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage)

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

    private fun updateToggleButtons() {
        if (isNeutralSelected) {
            // Neutral seleccionado
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
            // Original seleccionado
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
                // Mostrar versión neutral basada en la respuesta de BETO
                tvTextContent.text = generateNeutralText()
            } else {
                if (analysisType == TYPE_TEXT) {
                    tvTextContent.text = textContent
                } else {
                    // Para audio, mostrar la transcripción o un mensaje
                    if (isTranscribing) {
                        tvTextContent.text = ""
                    } else {
                        tvTextContent.text = textContent
                    }
                }
            }
            tvTextContent.animate().alpha(1.0f).setDuration(150).start()
        }.start()
    }

    private fun generateNeutralText(): String {
        return ("Versión neutral")
    }

    private fun setupRecyclerView() {
        // Inicializar con lista vacía
        modismosAdapter = ModismosAdapter(emptyList())
        recyclerViewModismos.apply {
            layoutManager = LinearLayoutManager(this@AnalysisActivity)
            adapter = modismosAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun updateRecyclerViewWithModismos(modismos: List<Modismo>) {
        detectedModismos = modismos
        modismosAdapter = ModismosAdapter(modismos)
        recyclerViewModismos.adapter = modismosAdapter

        Log.d(TAG, "RecyclerView actualizado con ${modismos.size} modismos")
        for (modismo in modismos) {
            Log.d(TAG, "  - ${modismo.palabra}: ${modismo.definiciones.joinToString()}")
        }
    }

    private fun loadContent() {
        Log.d(TAG, "=== Cargando contenido ===")

        // Si es análisis de audio, configurar el reproductor
        if (analysisType == TYPE_AUDIO && !audioPath.isNullOrEmpty()) {
            audioPlayerCard.isVisible = true
            setupAudioPlayer(audioPath!!)

            // Si necesita transcripción automática, usar el pipeline completo
            if (shouldAutoTranscribe) {
                startCompleteAudioAnalysis()
            }
        } else {
            audioPlayerCard.isVisible = false

            // Si es análisis de texto, enviar a la API Gateway
            if (analysisType == TYPE_TEXT && textContent.isNotBlank()) {
                sendTextToGateway(textContent)
            }
        }

        updateContent()
    }

    private fun startCompleteAudioAnalysis() {
        if (audioPath.isNullOrEmpty() || isTranscribing) return

        isTranscribing = true
        showLoadingState(true, "Procesando....")

        Log.d(TAG, "=== Iniciando pipeline completo de audio ===")
        Log.d(TAG, "Audio path: $audioPath")

        val audioFile = File(audioPath!!)
        if (!audioFile.exists()) {
            Log.e(TAG, "Archivo de audio no encontrado: $audioPath")
            showError("No se encontró el archivo de audio")
            return
        }

        // Crear request multipart para el pipeline completo
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(ANALYZE_AUDIO_ENDPOINT)
            .post(requestBody)
            .build()

        Log.d(TAG, "Enviando audio al pipeline completo: $ANALYZE_AUDIO_ENDPOINT")

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en pipeline completo de audio", e)
                runOnUiThread {
                    isTranscribing = false
                    showLoadingState(false)
                    showError("Error de conexión con pipeline completo: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "=== RESPUESTA COMPLETA DEL PIPELINE ===")
                Log.d(TAG, "Status code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                runOnUiThread {
                    isTranscribing = false
                    showLoadingState(false)

                    try {
                        if (response.isSuccessful) {
                            parseCompleteAudioResponse(responseBody)
                        } else {
                            Log.e(TAG, "Error del pipeline: ${response.code}")
                            showError("Error del pipeline completo: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando respuesta del pipeline", e)
                        showError("Error procesando respuesta del pipeline: ${e.message}")
                    }
                }
            }
        })
    }

    // Función para enviar texto a la API Gateway
    private fun sendTextToGateway(text: String) {

        Log.d(TAG, "=== Enviando texto a API Gateway ===")
        Log.d(TAG, "Texto: $text")
        Log.d(TAG, "Endpoint: $ANALYZE_TEXT_ENDPOINT")

        val jsonBody = JSONObject().apply {
            put("text", text)
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonBody.toString()
        )

        val request = Request.Builder()
            .url(ANALYZE_TEXT_ENDPOINT)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error conectando con API Gateway para texto", e)
                runOnUiThread {
                    showLoadingState(false)
                    showError("Error conectando con API Gateway: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "=== RESPUESTA DE ANÁLISIS DE TEXTO ===")
                Log.d(TAG, "Status code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                runOnUiThread {
                    showLoadingState(false)

                    try {
                        if (response.isSuccessful) {
                            parseTextAnalysisResponse(responseBody)
                        } else {
                            Log.e(TAG, "Error de API Gateway: ${response.code}")
                            showError("Error de API Gateway: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando respuesta de texto", e)
                        showError("Error procesando respuesta: ${e.message}")
                    }
                }
            }
        })
    }

    // Función para capitalizar la primera letra
    private fun capitalizeFirstLetter(text: String): String {
        return if (text.isNotEmpty()) {
            text.substring(0, 1).uppercase(Locale.getDefault()) +
                    text.substring(1).lowercase(Locale.getDefault())
        } else {
            text
        }
    }

    // Función para guardar modismos detectados en Firestore
    private fun saveDetectedModismosToFirestore(modismos: List<Modismo>) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "Usuario no autenticado, no se pueden guardar modismos")
            return
        }

        if (modismos.isEmpty()) {
            Log.d(TAG, "No hay modismos para guardar")
            return
        }

        val uid = currentUser.uid
        Log.d(TAG, "Guardando ${modismos.size} modismos para usuario: $uid")

        // Preparar solo las palabras para guardar
        val palabrasData = modismos.map { modismo ->
            capitalizeFirstLetter(modismo.palabra)
        }

        // Referencia al documento del usuario
        val userDocRef = firestore.collection("usuarios").document(uid)

        // Usar transacción para asegurar consistencia
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)

            if (snapshot.exists()) {
                // El documento del usuario existe, agregar a la lista palabras
                Log.d(TAG, "Usuario encontrado, agregando palabras a lista existente")

                // Obtener lista actual de palabras
                val currentPalabras = snapshot.get("palabras") as? List<String> ?: emptyList()
                Log.d(TAG, "Lista actual tiene ${currentPalabras.size} palabras")

                // Filtrar palabras que no estén ya en la lista
                val palabrasExistentes = currentPalabras.map { it.lowercase() }.toSet()

                val nuevasPalabras = palabrasData.filter { palabra ->
                    !palabrasExistentes.contains(palabra.lowercase())
                }

                if (nuevasPalabras.isNotEmpty()) {
                    Log.d(TAG, "Agregando ${nuevasPalabras.size} palabras nuevas")

                    // Agregar las nuevas palabras usando FieldValue.arrayUnion
                    transaction.update(userDocRef, "palabras", FieldValue.arrayUnion(*nuevasPalabras.toTypedArray()))
                } else {
                    Log.d(TAG, "Todas las palabras ya existen en la lista")
                }

                nuevasPalabras.size
            } else {
                // El documento del usuario no existe, crearlo con las palabras
                Log.d(TAG, "Creando nuevo documento de usuario con palabras")

                val userData = mapOf(
                    "uid" to uid,
                    ("email" to currentUser.email) as Pair<*, *>,
                    "palabras" to palabrasData
                )

                transaction.set(userDocRef, userData)
                palabrasData.size
            }
        }.addOnSuccessListener { nuevasAgregadas ->
            Log.d(TAG, " Palabras guardadas exitosamente: $nuevasAgregadas nuevas")

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error guardando palabras en Firestore", exception)

            runOnUiThread {
                Toast.makeText(
                    this,
                    " Error guardando palabras: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Función para procesar respuesta del pipeline completo de audio
    private fun parseCompleteAudioResponse(jsonResponse: String) {
        try {
            lastApiResponse = jsonResponse
            val jsonObject = JSONObject(jsonResponse)

            Log.d(TAG, "=== PARSEANDO RESPUESTA COMPLETA DEL PIPELINE ===")

            // Obtener transcripción
            val transcription = when {
                jsonObject.has("transcription") -> jsonObject.getString("transcription")
                jsonObject.has("text") -> jsonObject.getString("text")
                else -> ""
            }

            Log.d(TAG, "Transcripción obtenida: $transcription")

            if (transcription.isNotBlank()) {
                textContent = transcription.trim()
                updateContent()
            }

            // Obtener información de análisis BETO
            val analysis = jsonObject.optString("analysis", "")
            val status = jsonObject.optString("status", "")
            val pipeline = jsonObject.optString("pipeline", "")

            Log.d(TAG, "Análisis: $analysis")
            Log.d(TAG, "Status: $status")
            Log.d(TAG, "Pipeline: $pipeline")

            // Procesar modismos detectados
            if (jsonObject.has("modismos_detected")) {
                val modismosDetected = jsonObject.getJSONObject("modismos_detected")
                Log.d(TAG, "Modismos detectados (objeto): $modismosDetected")

                // Convertir a lista de Modismo
                val modismosList = parseModismosFromBeto(modismosDetected, jsonObject)
                updateRecyclerViewWithModismos(modismosList)

                // Guardar modismos en Firestore
                if (modismosList.isNotEmpty()) {
                    saveDetectedModismosToFirestore(modismosList)
                }
            }

            // Procesar modismos detallados si están disponibles
            if (jsonObject.has("modismos_detallados")) {
                val modismosDetallados = jsonObject.getJSONArray("modismos_detallados")
                Log.d(TAG, "Modismos detallados: $modismosDetallados")

                val modismosList = parseDetailedModismos(modismosDetallados)
                updateRecyclerViewWithModismos(modismosList)

                // Guardar modismos en Firestore
                if (modismosList.isNotEmpty()) {
                    saveDetectedModismosToFirestore(modismosList)
                }
            }

            // Mostrar información adicional
            val totalModismos = jsonObject.optInt("total_modismos", 0)
            val message = when {
                totalModismos > 0 -> "Pipeline completo: $totalModismos modismos detectados"
                transcription.isNotBlank() -> "Transcripción completada (sin modismos detectados)"
                else -> "Pipeline parcial completado"
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta completa del pipeline", e)

            // Si no es JSON válido, intentar extraer transcripción simple
            if (jsonResponse.isNotBlank()) {
                textContent = jsonResponse.trim()
                updateContent()
                Toast.makeText(this, "Pipeline completado (respuesta simple)", Toast.LENGTH_SHORT).show()
            } else {
                showError("Error parseando respuesta del pipeline: ${e.message}")
            }
        }
    }

    // Función para procesar respuesta de análisis de texto
    private fun parseTextAnalysisResponse(jsonResponse: String) {
        try {
            lastApiResponse = jsonResponse
            val jsonObject = JSONObject(jsonResponse)

            Log.d(TAG, "=== PARSEANDO RESPUESTA DE ANÁLISIS DE TEXTO ===")

            // Obtener análisis
            val analysis = jsonObject.optString("analysis", "")
            val status = jsonObject.optString("status", "")
            val betoAvailable = jsonObject.optBoolean("beto_available", false)

            Log.d(TAG, "Análisis: $analysis")
            Log.d(TAG, "Status: $status")
            Log.d(TAG, "BETO disponible: $betoAvailable")

            // Procesar modismos detectados
            if (jsonObject.has("modismos_detected")) {
                val modismosDetected = jsonObject.getJSONObject("modismos_detected")
                Log.d(TAG, "Modismos detectados: $modismosDetected")

                val modismosList = parseModismosFromBeto(modismosDetected, jsonObject)
                updateRecyclerViewWithModismos(modismosList)

                // Guardar modismos en Firestore
                if (modismosList.isNotEmpty()) {
                    saveDetectedModismosToFirestore(modismosList)
                }
            }

            // Procesar modismos detallados si están disponibles
            if (jsonObject.has("modismos_detallados")) {
                val modismosDetallados = jsonObject.getJSONArray("modismos_detallados")
                Log.d(TAG, "Modismos detallados: $modismosDetallados")

                val modismosList = parseDetailedModismos(modismosDetallados)
                updateRecyclerViewWithModismos(modismosList)

                // Guardar modismos en Firestore
                if (modismosList.isNotEmpty()) {
                    saveDetectedModismosToFirestore(modismosList)
                }
            }

            val totalModismos = jsonObject.optInt("total_modismos", 0)
            val message = if (betoAvailable) {
                if (totalModismos > 0) {
                    "$totalModismos modismos detectados"
                } else {
                    "No se detectaron modismos"
                }
            } else {
                "(BETO no disponible)"
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta de análisis de texto", e)
            Toast.makeText(this, "Respuesta procesada con advertencias", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para convertir modismos detectados por BETO a objetos Modismo
    private fun parseModismosFromBeto(modismosObject: JSONObject, fullResponse: JSONObject): List<Modismo> {
        val modismosList = mutableListOf<Modismo>()

        try {
            val keys = modismosObject.keys()
            while (keys.hasNext()) {
                val palabra = keys.next()
                val significado = modismosObject.getString(palabra)

                Log.d(TAG, "Procesando modismo: $palabra -> $significado")

                // Crear objeto Modismo con la información disponible
                val modismo = Modismo(
                    palabra = palabra,
                    tipo = "Modismo",
                    definiciones = listOf("Significado detectado: $significado"),
                    sinonimos = listOf(significado)
                )

                modismosList.add(modismo)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando modismos de BETO", e)
        }

        return modismosList
    }

    // Función para procesar modismos detallados
    private fun parseDetailedModismos(modismosArray: JSONArray): List<Modismo> {
        val modismosList = mutableListOf<Modismo>()

        try {
            for (i in 0 until modismosArray.length()) {
                val modismoObj = modismosArray.getJSONObject(i)

                val palabra = modismoObj.optString("palabra", "")
                val significado = modismoObj.optString("significado_detectado", "")

                Log.d(TAG, "Modismo detallado: $palabra -> $significado")

                val modismo = Modismo(
                    palabra = palabra,
                    tipo = "Modismo Colombiano",
                    definiciones = listOf("$significado "),
                    sinonimos = listOf(significado)
                )

                modismosList.add(modismo)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando modismos detallados", e)
        }

        return modismosList
    }

    // Función legacy para transcripción simple (mantenida por compatibilidad)
    private fun startTranscription() {
        if (audioPath.isNullOrEmpty() || isTranscribing) return

        isTranscribing = true
        showLoadingState(true, "Transcribiendo audio...")

        Log.d(TAG, "=== Iniciando transcripción simple ===")

        val audioFile = File(audioPath!!)
        if (!audioFile.exists()) {
            showError("No se encontró el archivo de audio")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(TRANSCRIBE_ENDPOINT)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en transcripción simple", e)
                runOnUiThread {
                    isTranscribing = false
                    showLoadingState(false)
                    showError("Error de conexión: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "=== RESPUESTA DE TRANSCRIPCIÓN SIMPLE ===")
                Log.d(TAG, "Status code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                runOnUiThread {
                    isTranscribing = false
                    showLoadingState(false)

                    try {
                        if (response.isSuccessful) {
                            parseTranscriptionResponse(responseBody)
                        } else {
                            showError("Error de transcripción: ${response.code}")
                        }
                    } catch (e: Exception) {
                        showError("Error procesando transcripción: ${e.message}")
                    }
                }
            }
        })
    }

    private fun parseTranscriptionResponse(jsonResponse: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)

            val transcription = when {
                jsonObject.has("text") -> jsonObject.getString("text")
                jsonObject.has("transcription") -> jsonObject.getString("transcription")
                jsonObject.has("result") -> jsonObject.getString("result")
                else -> jsonResponse
            }

            if (transcription.isNotBlank()) {
                textContent = transcription.trim()
                updateContent()
                Toast.makeText(this, "Transcripción completada", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Transcripción completada: $transcription")
            } else {
                showError("No se pudo obtener la transcripción")
            }
        } catch (e: Exception) {
            if (jsonResponse.isNotBlank()) {
                textContent = jsonResponse.trim()
                updateContent()
                Toast.makeText(this, "Transcripción completada", Toast.LENGTH_SHORT).show()
            } else {
                showError("Error procesando transcripción: ${e.message}")
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean, message: String = "Procesando...") {
        if (isLoading) {
            progressIndicator.isVisible = true
            tvLoadingMessage.isVisible = true
            tvLoadingMessage.text = message

            // Animación de aparición
            progressIndicator.alpha = 0f
            tvLoadingMessage.alpha = 0f
            progressIndicator.animate().alpha(1f).setDuration(300).start()
            tvLoadingMessage.animate().alpha(1f).setDuration(300).start()

            Log.d(TAG, "Mostrando estado de carga: $message")
        } else {
            progressIndicator.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    progressIndicator.isVisible = false
                }
                .start()

            tvLoadingMessage.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    tvLoadingMessage.isVisible = false
                }
                .start()

            Log.d(TAG, "Ocultando estado de carga")
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, "Error mostrado al usuario: $message")
        Toast.makeText(this, "$message", Toast.LENGTH_LONG).show()
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

            Log.d(TAG, "Reproductor de audio configurado para: $path")

        } catch (e: Exception) {
            Log.e(TAG, "Error configurando reproductor de audio", e)
            audioPlayerCard.isVisible = false
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                btnPlayPause.setImageResource(R.drawable.ic_play)
                stopProgressUpdate()
                Log.d(TAG, "Audio pausado")
            } else {
                player.start()
                btnPlayPause.setImageResource(R.drawable.ic_pause)
                startProgressUpdate()
                Log.d(TAG, "Audio reproduciendo")
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
        Log.d(TAG, "Última respuesta API guardada: $lastApiResponse")

        releaseMediaPlayer()
        // Cancelar cualquier petición HTTP pendiente
        httpClient.dispatcher.cancelAll()
    }
}