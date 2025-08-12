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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.ModismosAdapter
import com.proyecto.modismos.models.Modismo

class AnalysisActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ANALYSIS_TYPE = "analysis_type"
        private const val EXTRA_TEXT_CONTENT = "text_content"
        private const val EXTRA_AUDIO_PATH = "audio_path"

        const val TYPE_TEXT = "text"
        const val TYPE_AUDIO = "audio"

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
    }

    private lateinit var tvAnalysisTitle: TextView
    private lateinit var tvTextContent: TextView
    private lateinit var audioPlayerCard: LinearLayout
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var seekBarAudio: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var recyclerViewModismos: RecyclerView
    private lateinit var btnClose: ImageView

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private lateinit var modismosAdapter: ModismosAdapter

    private var analysisType: String = TYPE_TEXT
    private var textContent: String = ""
    private var audioPath: String? = null

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
    }

    private fun getIntentData() {
        analysisType = intent.getStringExtra(EXTRA_ANALYSIS_TYPE) ?: TYPE_TEXT
        textContent = intent.getStringExtra(EXTRA_TEXT_CONTENT) ?: ""
        audioPath = intent.getStringExtra(EXTRA_AUDIO_PATH)
    }

    private fun initViews() {
        tvAnalysisTitle = findViewById(R.id.tvAnalysisTitle)
        tvTextContent = findViewById(R.id.tvTextContent)
        audioPlayerCard = findViewById(R.id.audioPlayerCard)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBarAudio = findViewById(R.id.seekBarAudio)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        recyclerViewModismos = findViewById(R.id.recyclerViewModismos)
        btnClose = findViewById(R.id.btn_close)
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            finish() // Cerrar la activity
        }

        btnPlayPause.setOnClickListener {
            togglePlayPause()
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
        // Datos de ejemplo - estos deberían venir de tu backend o lógica de análisis
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
        // Configurar título según el tipo
        tvAnalysisTitle.text = if (analysisType == TYPE_AUDIO) "Transcripción" else "Texto ingresado"

        // Mostrar el contenido del texto
        tvTextContent.text = textContent

        // Si es análisis de audio, configurar el reproductor
        if (analysisType == TYPE_AUDIO && !audioPath.isNullOrEmpty()) {
            audioPlayerCard.isVisible = true
            setupAudioPlayer(audioPath!!)
        } else {
            audioPlayerCard.isVisible = false
        }

        // Aquí podrías analizar el texto y extraer los modismos reales
        // Por ahora usamos datos de ejemplo en setupRecyclerView()
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
    }
}