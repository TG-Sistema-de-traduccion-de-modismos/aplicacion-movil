package com.proyecto.modismos.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.proyecto.modismos.R
import com.proyecto.modismos.adapters.ModismosAdapter
import com.proyecto.modismos.models.Modismo

class AnalysisFragment : Fragment() {

    companion object {
        const val ARG_ANALYSIS_TYPE = "analysis_type"
        const val ARG_TEXT_CONTENT = "text_content"
        const val ARG_AUDIO_PATH = "audio_path"

        const val TYPE_TEXT = "text"
        const val TYPE_AUDIO = "audio"

        fun newInstance(type: String, content: String, audioPath: String? = null): AnalysisFragment {
            return AnalysisFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ANALYSIS_TYPE, type)
                    putString(ARG_TEXT_CONTENT, content)
                    putString(ARG_AUDIO_PATH, audioPath)
                }
            }
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
    private var  updateRunnable: Runnable? = null
    private lateinit var modismosAdapter: ModismosAdapter

    private var analysisType: String = TYPE_TEXT
    private var textContent: String = ""
    private var audioPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            analysisType = it.getString(ARG_ANALYSIS_TYPE, TYPE_TEXT)
            textContent = it.getString(ARG_TEXT_CONTENT, "")
            audioPath = it.getString(ARG_AUDIO_PATH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadContent()
    }

    private fun initViews(view: View) {
        tvAnalysisTitle = view.findViewById(R.id.tvAnalysisTitle)
        tvTextContent = view.findViewById(R.id.tvTextContent)
        audioPlayerCard = view.findViewById(R.id.audioPlayerCard)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        seekBarAudio = view.findViewById(R.id.seekBarAudio)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        recyclerViewModismos = view.findViewById(R.id.recyclerViewModismos)
        btnClose = view.findViewById(R.id.btn_close)
    }

    private fun setupClickListeners() {


        btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
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
            layoutManager = LinearLayoutManager(context)
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
                    this@AnalysisFragment.isPlaying = false
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