// 1. HomeFragment.kt - Agregar botón de ayuda
package com.proyecto.modismos.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.modismos.R
import com.proyecto.modismos.activities.AnalysisActivity
import com.proyecto.modismos.activities.AudioRecorderActivity
import com.proyecto.modismos.activities.UserMainActivity
import com.proyecto.modismos.handlers.AnalysisHandler
import com.proyecto.modismos.network.ApiService
import com.proyecto.modismos.utils.DialogManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
        private const val MAX_AUDIO_DURATION_MS = 50000
        private const val MAX_TEXT_LENGTH = 400
    }

    // Views
    private lateinit var fabMicrophone: FloatingActionButton
    private lateinit var fabUpload: FloatingActionButton
    private lateinit var audioPlayerCard: LinearLayout
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnDelete: ImageView
    private lateinit var seekBarAudio: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvAudioName: TextView
    private lateinit var btnHelp: ImageView // NUEVO
    private lateinit var etTexto: EditText
    private lateinit var tvCharacterCount: TextView
    private lateinit var btnAnalizarTexto: MaterialButton
    private lateinit var btnAnalizarAudio: MaterialButton
    private lateinit var btnClearText: ImageView

    // Audio
    private var recordedAudioPath: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // Services
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var apiService: ApiService
    private lateinit var dialogManager: DialogManager
    private val analysisHandler = AnalysisHandler()

    // Launchers
    private val audioRecorderLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            recordedAudioPath = result.data?.getStringExtra(AudioRecorderActivity.EXTRA_AUDIO_PATH)
            recordedAudioPath?.let { path ->
                if (validateAudioDuration(path)) {
                    val fileName = path.substringAfterLast("/")
                    Toast.makeText(requireContext(), "Audio grabado: $fileName", Toast.LENGTH_SHORT).show()
                    showAudioPlayer(path)
                } else {
                    File(path).delete()
                    recordedAudioPath = null
                    dialogManager.showAudioDurationErrorDialog()
                }
            }
        }
    }

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleUploadedAudio(it) }
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
        initServices()
        initViews(view)
        setupClickListeners()
    }

    private fun initServices() {
        firebaseAuth = FirebaseAuth.getInstance()
        apiService = ApiService()
        dialogManager = DialogManager(requireContext())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews(view: View) {
        fabMicrophone = view.findViewById(R.id.fabMicrophone)
        fabUpload = view.findViewById(R.id.fabUpload)
        audioPlayerCard = view.findViewById(R.id.audioPlayerCard)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        btnDelete = view.findViewById(R.id.btnDelete)
        btnClearText = view.findViewById(R.id.btnClearText)
        seekBarAudio = view.findViewById(R.id.seekBarAudio)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        tvAudioName = view.findViewById(R.id.tvAudioName)
        btnHelp = view.findViewById(R.id.btnHelp) // NUEVO
        etTexto = view.findViewById(R.id.etTexto)
        tvCharacterCount = view.findViewById(R.id.tvCharacterCount)
        btnAnalizarTexto = view.findViewById(R.id.btnAnalizarTexto)
        btnAnalizarAudio = view.findViewById(R.id.btnAnalizarAudio)

        etTexto.setOnTouchListener { v, event ->
            if (v.id == R.id.etTexto) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        v.performClick()
                    }
                }
            }
            false
        }

        etTexto.movementMethod = ScrollingMovementMethod()
        etTexto.isVerticalScrollBarEnabled = true

        setupCharacterCounter()
    }

    private fun setupClickListeners() {

        btnClearText.setOnClickListener {
            etTexto.text?.clear()
        }

        // NUEVO: Botón de ayuda
        btnHelp.setOnClickListener {
            showHelpDialog()
        }

        fabMicrophone.setOnClickListener { openAudioRecorder() }
        fabUpload.setOnClickListener { openAudioPicker() }
        btnPlayPause.setOnClickListener { togglePlayPause() }
        btnDelete.setOnClickListener { deleteAudio() }
        btnAnalizarTexto.setOnClickListener { analizarTexto() }
        btnAnalizarAudio.setOnClickListener { analizarAudio() }

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

    // NUEVO: Mostrar diálogo de ayuda
    private fun showHelpDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_help)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        // Configurar botón de cerrar
        dialog.findViewById<ImageView>(R.id.btnCloseHelp)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<MaterialButton>(R.id.btnGotIt)?.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun setupCharacterCounter() {
        etTexto.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                updateCharacterCount(currentLength)

                btnClearText.isVisible = currentLength > 0
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun updateCharacterCount(count: Int) {
        tvCharacterCount.text = "$count/$MAX_TEXT_LENGTH"

        tvCharacterCount.setTextColor(
            when {
                count > MAX_TEXT_LENGTH -> {
                    requireContext().getColor(R.color.red_primary)
                }
                count >= MAX_TEXT_LENGTH * 0.9 -> {
                    requireContext().getColor(android.R.color.holo_orange_dark)
                }
                else -> {
                    requireContext().getColor(android.R.color.darker_gray)
                }
            }
        )
    }

    // ========== AUTHENTICATION ==========

    private fun getFirebaseIdToken(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val user = firebaseAuth.currentUser

        if (user == null) {
            Log.e(TAG, "Usuario no autenticado")
            activity?.runOnUiThread {
                dialogManager.dismissLoadingDialog()
                dialogManager.showErrorDialog("Sesión expirada", "Por favor inicia sesión nuevamente")
            }
            onFailure()
            return
        }

        user.getIdToken(true)
            .addOnSuccessListener { result ->
                result.token?.let { onSuccess(it) } ?: onFailure()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error obteniendo token", e)
                activity?.runOnUiThread {
                    dialogManager.dismissLoadingDialog()
                    dialogManager.showErrorDialog("Error de autenticación", "No se pudo verificar tu sesión")
                }
                onFailure()
            }
    }

    // ========== ANALYSIS ==========

    private fun analizarTexto() {
        val texto = etTexto.text.toString().trim()

        when {
            texto.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    "Por favor ingresa un texto para analizar",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            texto.length > MAX_TEXT_LENGTH -> {
                Toast.makeText(
                    requireContext(),
                    "El texto no puede superar los $MAX_TEXT_LENGTH caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        Log.d(TAG, "Iniciando análisis de texto: $texto (${texto.length} caracteres)")
        dialogManager.showLoadingDialog()

        getFirebaseIdToken(
            onSuccess = { token -> sendTextToAnalysis(texto, token) },
            onFailure = { dialogManager.dismissLoadingDialog() }
        )
    }

    private fun analizarAudio() {
        if (recordedAudioPath.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Por favor graba o sube un audio primero", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Iniciando análisis de audio: $recordedAudioPath")
        dialogManager.showLoadingDialog()

        getFirebaseIdToken(
            onSuccess = { token -> sendAudioToAnalysis(recordedAudioPath!!, token) },
            onFailure = { dialogManager.dismissLoadingDialog() }
        )
    }

    private fun sendTextToAnalysis(text: String, idToken: String) {
        apiService.analyzeText(text, idToken, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                activity?.runOnUiThread {
                    handleAnalysisResponse(response, text, null)
                }
            }

            override fun onError(errorType: ApiService.ErrorType, message: String) {
                activity?.runOnUiThread {
                    dialogManager.showErrorDialog(
                        when (errorType) {
                            ApiService.ErrorType.NETWORK_ERROR -> "Error de conexión"
                            ApiService.ErrorType.UNAUTHORIZED -> "Sesión expirada"
                            ApiService.ErrorType.SERVER_ERROR -> "Error del servidor"
                            else -> "Error"
                        },
                        message
                    )
                }
            }
        })
    }

    private fun sendAudioToAnalysis(audioPath: String, idToken: String) {
        apiService.analyzeAudio(audioPath, idToken, object : ApiService.ApiCallback {
            override fun onSuccess(response: String) {
                activity?.runOnUiThread {
                    handleAnalysisResponse(response, "", audioPath)
                }
            }

            override fun onError(errorType: ApiService.ErrorType, message: String) {
                activity?.runOnUiThread {
                    dialogManager.showErrorDialog(
                        when (errorType) {
                            ApiService.ErrorType.NETWORK_ERROR -> "Error de conexión"
                            ApiService.ErrorType.UNAUTHORIZED -> "Sesión expirada"
                            ApiService.ErrorType.SERVER_ERROR -> "Error del servidor"
                            ApiService.ErrorType.FILE_NOT_FOUND -> "Error"
                            else -> "Error"
                        },
                        message
                    )
                }
            }
        })
    }

    private fun handleAnalysisResponse(jsonResponse: String, originalText: String, audioPath: String?) {
        analysisHandler.handleAnalysisResponse(
            jsonResponse,
            originalText,
            audioPath,
            object : AnalysisHandler.AnalysisCallback {
                override fun onSuccess(result: AnalysisHandler.AnalysisResult, jsonResponse: String) {
                    dialogManager.dismissLoadingDialog()
                    navigateToAnalysisActivity(jsonResponse, result.transcription, audioPath)
                }

                override fun onNoModismos() {
                    dialogManager.showNoModismosDialog()
                }

                override fun onPartialService(serviceName: String) {
                    dialogManager.showPartialServiceDialog(serviceName)
                }

                override fun onError(message: String) {
                    dialogManager.showErrorDialog("Respuesta inesperada", message)
                }
            }
        )
    }

    private fun navigateToAnalysisActivity(apiResponse: String, textContent: String, audioPath: String?) {
        val intent = Intent(requireContext(), AnalysisActivity::class.java).apply {
            putExtra("api_response", apiResponse)
            putExtra("text_content", textContent)
            audioPath?.let { putExtra("audio_path", it) }
        }
        startActivity(intent)
    }

    // ========== AUDIO MANAGEMENT ==========

    private fun validateAudioDuration(audioPath: String): Boolean {
        var tempPlayer: MediaPlayer? = null
        return try {
            tempPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
            }
            val duration = tempPlayer.duration
            Log.d(TAG, "Audio duration: ${duration}ms (Max: ${MAX_AUDIO_DURATION_MS}ms)")
            duration <= MAX_AUDIO_DURATION_MS
        } catch (e: Exception) {
            Log.e(TAG, "Error validating audio duration", e)
            false
        } finally {
            tempPlayer?.release()
        }
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
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val audioDir = File(requireContext().filesDir, "audio_recordings")
            if (!audioDir.exists()) audioDir.mkdirs()

            val fileName = "uploaded_${System.currentTimeMillis()}.${getFileExtension(uri)}"
            val audioFile = File(audioDir, fileName)

            inputStream?.use { input ->
                audioFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (validateAudioDuration(audioFile.absolutePath)) {
                recordedAudioPath = audioFile.absolutePath
                showAudioPlayer(audioFile.absolutePath)
                Toast.makeText(requireContext(), "Audio subido correctamente", Toast.LENGTH_SHORT).show()
            } else {
                audioFile.delete()
                dialogManager.showAudioDurationErrorDialog()
            }

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

    // ========== AUDIO PLAYER ==========

    private fun showAudioPlayer(audioPath: String) {
        try {
            releaseMediaPlayer()

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
                    seekBarAudio.max = duration
                    updateTotalTime(duration)
                    updateCurrentTime(0)

                    val fileName = audioPath.substringAfterLast("/").substringBeforeLast(".")
                    tvAudioName.text = when {
                        fileName.startsWith("recording_") -> "Grabación - ${formatTimestamp(fileName.substringAfter("recording_"))}"
                        fileName.startsWith("uploaded_") -> "Audio subido - ${formatTimestamp(fileName.substringAfter("uploaded_"))}"
                        else -> fileName
                    }
                }
            }

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
            if (file.exists()) file.delete()
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
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        isPlaying = false
    }

    // ========== LIFECYCLE ==========

    override fun onPause() {
        super.onPause()
        if (isPlaying) togglePlayPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        apiService.cancelAllRequests()
        dialogManager.dismissLoadingDialog()
    }
}