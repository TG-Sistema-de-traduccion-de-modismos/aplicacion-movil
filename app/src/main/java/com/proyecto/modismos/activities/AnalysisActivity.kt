package com.proyecto.modismos.activities

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.ModismosAdapter
import com.proyecto.modismos.models.Modismo
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class AnalysisActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ANALYSIS_TYPE = "analysis_type"
        private const val EXTRA_TEXT_CONTENT = "text_content"
        private const val EXTRA_AUDIO_PATH = "audio_path"
        private const val EXTRA_AUTO_TRANSCRIBE = "auto_transcribe"

        const val TYPE_TEXT = "text"
        const val TYPE_AUDIO = "audio"

        private const val WHISPER_SERVER_URL = "http://10.34.1.50:17041/transcribe"

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

    // Cliente HTTP para las peticiones al servidor
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

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
                tvTextContent.text = ""
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

    private fun setupRecyclerView() {
        val modismosList = listOf(
            Modismo(
                "bacano",
                "Modismo",
                listOf("Algo o alguien que es muy bueno, excelente, genial o atractivo."),
                listOf("Genial", "estupendo", "agradable")
            ),
            Modismo(
                "chevere",
                "Modismo",
                listOf("Expresión para indicar que algo está bien, es agradable o divertido."),
                listOf("Bueno", "agradable", "genial")
            )
        )

        modismosAdapter = ModismosAdapter(modismosList)
        recyclerViewModismos.apply {
            layoutManager = LinearLayoutManager(this@AnalysisActivity)
            adapter = modismosAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadContent() {
        // Si es análisis de audio, configurar el reproductor
        if (analysisType == TYPE_AUDIO && !audioPath.isNullOrEmpty()) {
            audioPlayerCard.isVisible = true
            setupAudioPlayer(audioPath!!)

            // Si necesita transcripción automática, iniciarla
            if (shouldAutoTranscribe) {
                startTranscription()
            }
        } else {
            audioPlayerCard.isVisible = false
        }

        updateContent()
    }

    private fun startTranscription() {
        if (audioPath.isNullOrEmpty() || isTranscribing) return

        isTranscribing = true
        showLoadingState(true)

        val audioFile = File(audioPath!!)
        if (!audioFile.exists()) {
            showError("No se encontró el archivo de audio")
            return
        }

        // Crear el request multipart
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(WHISPER_SERVER_URL)
            .post(requestBody)
            .build()

        // Ejecutar la petición en background
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    isTranscribing = false
                    showLoadingState(false)
                    showError("Error de conexión: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() // leer inmediatamente
                    runOnUiThread {
                        isTranscribing = false
                        showLoadingState(false)

                        if (response.isSuccessful) {
                            responseBody?.let { body ->
                                parseTranscriptionResponse(body)
                            } ?: showError("Respuesta vacía del servidor")
                        } else {
                            showError("Error del servidor: ${response.code}")
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        isTranscribing = false
                        showLoadingState(false)
                        showError("Error al procesar la respuesta: ${e.message}")
                    }
                }
            }


        })
    }

    private fun parseTranscriptionResponse(jsonResponse: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)

            // Intentar diferentes formatos de respuesta comunes en servidores Whisper
            val transcription = when {
                jsonObject.has("text") -> jsonObject.getString("text")
                jsonObject.has("transcription") -> jsonObject.getString("transcription")
                jsonObject.has("result") -> jsonObject.getString("result")
                else -> {
                    // Si no encuentra ningún campo conocido, usar toda la respuesta como texto
                    jsonResponse
                }
            }

            if (transcription.isNotBlank()) {
                textContent = transcription.trim()
                updateContent()
                Toast.makeText(this, "Transcripción completada", Toast.LENGTH_SHORT).show()
            } else {
                showError("No se pudo obtener la transcripción")
            }
        } catch (e: Exception) {
            // Si no es JSON válido, intentar usar la respuesta directamente como texto
            if (jsonResponse.isNotBlank()) {
                textContent = jsonResponse.trim()
                updateContent()
                Toast.makeText(this, "Transcripción completada", Toast.LENGTH_SHORT).show()
            } else {
                showError("Error al procesar la transcripción: ${e.message}")
            }
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.isVisible = true
            tvLoadingMessage.isVisible = true
            tvLoadingMessage.text = "Transcribiendo audio..."

            // Animación de aparición
            progressIndicator.alpha = 0f
            tvLoadingMessage.alpha = 0f
            progressIndicator.animate().alpha(1f).setDuration(300).start()
            tvLoadingMessage.animate().alpha(1f).setDuration(300).start()
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
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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

        } catch (e: Exception) {
            e.printStackTrace()
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
        releaseMediaPlayer()
        // Cancelar cualquier petición HTTP pendiente
        httpClient.dispatcher.cancelAll()
    }
}