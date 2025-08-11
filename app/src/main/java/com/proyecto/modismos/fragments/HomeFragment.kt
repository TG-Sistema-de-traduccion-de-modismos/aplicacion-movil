package com.proyecto.modismos.fragments

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.proyecto.modismos.activities.AudioRecorderActivity
import com.proyecto.modismos.R
import com.proyecto.modismos.activities.UserMainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var fabMicrophone: FloatingActionButton
    private lateinit var fabUpload: FloatingActionButton
    private lateinit var audioPlayerCard: LinearLayout
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnDelete: ImageView
    private lateinit var seekBarAudio: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvAudioName: TextView
    private lateinit var btnDictionary: MaterialButton
    private lateinit var etTexto: EditText
    private lateinit var btnAnalizarTexto: MaterialButton
    private lateinit var btnAnalizarAudio: MaterialButton

    private var recordedAudioPath: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    private val audioRecorderLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            recordedAudioPath = result.data?.getStringExtra(AudioRecorderActivity.EXTRA_AUDIO_PATH)
            val audioUri = result.data?.getStringExtra(AudioRecorderActivity.EXTRA_AUDIO_URI)

            recordedAudioPath?.let { path ->
                val fileName = path.substringAfterLast("/")
                Toast.makeText(requireContext(), "Audio grabado: $fileName", Toast.LENGTH_SHORT).show()

                // Mostrar el reproductor de audio
                showAudioPlayer(path)
            }
        }
    }

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            handleUploadedAudio(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()
    }

    private fun initViews(view: View) {
        fabMicrophone = view.findViewById(R.id.fabMicrophone)
        fabUpload = view.findViewById(R.id.fabUpload)
        audioPlayerCard = view.findViewById(R.id.audioPlayerCard)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        btnDelete = view.findViewById(R.id.btnDelete)
        seekBarAudio = view.findViewById(R.id.seekBarAudio)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        tvAudioName = view.findViewById(R.id.tvAudioName)
        btnDictionary = view.findViewById(R.id.btnDiccionario)
        etTexto = view.findViewById(R.id.etTexto)
        btnAnalizarTexto = view.findViewById(R.id.btnAnalizarTexto)
        btnAnalizarAudio = view.findViewById(R.id.btnAnalizarAudio)

    }

    private fun setupClickListeners() {

        btnDictionary.setOnClickListener {
            (activity as? UserMainActivity)?.let { mainActivity ->
                mainActivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_dictionary
            }
        }

        fabMicrophone.setOnClickListener {
            openAudioRecorder()
        }

        fabUpload.setOnClickListener {
            openAudioPicker()
        }

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnDelete.setOnClickListener {
            deleteAudio()
        }

        btnAnalizarTexto.setOnClickListener {
            analizarTexto()
        }

        btnAnalizarAudio.setOnClickListener {
            analizarAudio()
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

    private fun analizarTexto() {
        val texto = etTexto.text.toString().trim()

        if (texto.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa un texto para analizar", Toast.LENGTH_SHORT).show()
            return
        }

        // Navegar al AnalysisFragment con el texto
        val analysisFragment = AnalysisFragment.newInstance(
            type = AnalysisFragment.TYPE_TEXT,
            content = texto
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.viewPager, analysisFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun analizarAudio() {
        if (recordedAudioPath.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Por favor graba o sube un audio primero", Toast.LENGTH_SHORT).show()
            return
        }

        // Aquí simularemos una transcripción del audio
        // En producción, esto debería enviarse a un servicio de transcripción
        val transcripcionSimulada = "oye viste el nuevo lugar que abrieron en el centro si fui ayer y esta bacano la comida y el ambiente es super chevere deberíamos ir un día de estos claro me avisas cuando puedes y cuadramos algo de una"

        // Navegar al AnalysisFragment con la transcripción y el path del audio
        val analysisFragment = AnalysisFragment.newInstance(
            type = AnalysisFragment.TYPE_AUDIO,
            content = transcripcionSimulada,
            audioPath = recordedAudioPath
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.viewPager, analysisFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openAudioRecorder() {
        val intent = Intent(requireContext(), AudioRecorderActivity::class.java)
        audioRecorderLauncher.launch(intent)
    }

    private fun openAudioPicker() {
        audioPickerLauncher.launch("audio/*")
    }

    private fun handleUploadedAudio(uri: Uri) {
        try {
            // Copiar el audio al almacenamiento interno de la app
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val audioDir = File(requireContext().filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            val fileName = "uploaded_${System.currentTimeMillis()}.${getFileExtension(uri)}"
            val audioFile = File(audioDir, fileName)

            inputStream?.use { input ->
                audioFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            recordedAudioPath = audioFile.absolutePath
            showAudioPlayer(audioFile.absolutePath)
            Toast.makeText(requireContext(), "Audio subido correctamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al subir el audio: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                displayName.substringAfterLast(".", "m4a")
            } else "m4a"
        } ?: "m4a"
    }

    private fun showAudioPlayer(audioPath: String) {
        try {
            // Limpiar MediaPlayer anterior si existe
            releaseMediaPlayer()

            // Crear nuevo MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()

                setOnCompletionListener {
                    btnPlayPause.setImageResource(R.drawable.ic_play)
                    seekBarAudio.progress = 0
                    updateCurrentTime(0)
                    stopProgressUpdate()
                }

                setOnPreparedListener {
                    // Configurar UI
                    val duration = duration
                    seekBarAudio.max = duration
                    updateTotalTime(duration)
                    updateCurrentTime(0)

                    // Mostrar nombre del archivo
                    val fileName = audioPath.substringAfterLast("/").substringBeforeLast(".")
                    tvAudioName.text = if (fileName.startsWith("recording_")) {
                        "Grabación - ${formatTimestamp(fileName.substringAfter("recording_"))}"
                    } else if (fileName.startsWith("uploaded_")) {
                        "Audio subido - ${formatTimestamp(fileName.substringAfter("uploaded_"))}"
                    } else {
                        fileName
                    }
                }
            }

            // Mostrar el reproductor
            audioPlayerCard.isVisible = true
            btnPlayPause.setImageResource(R.drawable.ic_play)
            isPlaying = false

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al cargar el audio: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun deleteAudio() {
        recordedAudioPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }

        releaseMediaPlayer()
        audioPlayerCard.isVisible = false
        recordedAudioPath = null
        Toast.makeText(requireContext(), "Audio eliminado", Toast.LENGTH_SHORT).show()
    }

    private fun startProgressUpdate() {
        updateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition
                        seekBarAudio.progress = currentPosition
                        updateCurrentTime(currentPosition)
                        updateHandler.postDelayed(this, 1000)
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

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val time = timestamp.toLong()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(time))
        } catch (e: Exception) {
            "Audio"
        }
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