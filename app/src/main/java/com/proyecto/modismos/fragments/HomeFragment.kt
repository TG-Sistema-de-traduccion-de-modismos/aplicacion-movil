package com.proyecto.modismos.fragments

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.proyecto.modismos.activities.AudioRecorderActivity
import com.proyecto.modismos.activities.AnalysisActivity
import com.proyecto.modismos.R
import com.proyecto.modismos.activities.UserMainActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
        private const val API_GATEWAY_BASE_URL = "https://modistra-api.duckdns.org"
        private const val ANALYZE_TEXT_ENDPOINT = "$API_GATEWAY_BASE_URL/text"
        private const val ANALYZE_AUDIO_ENDPOINT = "$API_GATEWAY_BASE_URL/audio"
        private const val MAX_AUDIO_DURATION_MS = 50000 // 50 segundos en milisegundos
    }

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

    // Cliente HTTP
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Diálogo de carga
    private var loadingDialog: AlertDialog? = null
    private var dialogView: View? = null

    private val audioRecorderLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            recordedAudioPath = result.data?.getStringExtra(AudioRecorderActivity.EXTRA_AUDIO_PATH)
            recordedAudioPath?.let { path ->
                // Validar duración antes de mostrar
                if (validateAudioDuration(path)) {
                    val fileName = path.substringAfterLast("/")
                    Toast.makeText(requireContext(), "Audio grabado: $fileName", Toast.LENGTH_SHORT).show()
                    showAudioPlayer(path)
                } else {
                    // Eliminar el archivo si excede el límite
                    File(path).delete()
                    recordedAudioPath = null
                    showAudioDurationErrorDialog()
                }
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

    /**
     * Valida que la duración del audio no exceda el límite de 50 segundos
     */
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

    /**
     * Muestra un diálogo cuando el audio excede el límite de duración
     */
    private fun showAudioDurationErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Audio muy largo")
            .setMessage("El audio no puede superar los 50 segundos. Por favor, graba o selecciona un audio más corto.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun analizarTexto() {
        val texto = etTexto.text.toString().trim()

        if (texto.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa un texto para analizar", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Iniciando análisis de texto: $texto")
        showLoadingDialog()

        sendTextToAnalysis(texto)
    }

    private fun analizarAudio() {
        if (recordedAudioPath.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Por favor graba o sube un audio primero", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Iniciando análisis de audio: $recordedAudioPath")
        showLoadingDialog()

        sendAudioToAnalysis(recordedAudioPath!!)
    }

    private fun sendTextToAnalysis(text: String) {
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
                Log.e(TAG, "Error en análisis de texto", e)
                activity?.runOnUiThread {
                    showErrorDialog("Error de conexión", "No se pudo conectar con el servidor. Verifica tu conexión a internet.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Respuesta análisis texto: ${response.code} - $responseBody")

                activity?.runOnUiThread {
                    try {
                        if (response.isSuccessful) {
                            handleAnalysisResponse(responseBody, text, null)
                        } else {
                            showErrorDialog("Error del servidor", "El servidor respondió con código: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando respuesta", e)
                        showErrorDialog("Error", "No se pudo procesar la respuesta del servidor")
                    }
                }
            }
        })
    }

    private fun sendAudioToAnalysis(audioPath: String) {
        val audioFile = File(audioPath)

        if (!audioFile.exists()) {
            dismissLoadingDialog()
            showErrorDialog("Error", "No se encontró el archivo de audio")
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
            .url(ANALYZE_AUDIO_ENDPOINT)
            .post(requestBody)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en análisis de audio", e)
                activity?.runOnUiThread {
                    showErrorDialog("Error de conexión", "No se pudo conectar con el servidor. Verifica tu conexión a internet.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Respuesta análisis audio: ${response.code} - $responseBody")

                activity?.runOnUiThread {
                    try {
                        if (response.isSuccessful) {
                            handleAnalysisResponse(responseBody, "", audioPath)
                        } else {
                            showErrorDialog("Error del servidor", "El servidor respondió con código: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando respuesta", e)
                        showErrorDialog("Error", "No se pudo procesar la respuesta del servidor")
                    }
                }
            }
        })
    }

    private fun handleAnalysisResponse(jsonResponse: String, originalText: String, audioPath: String?) {
        try {
            val jsonObject = JSONObject(jsonResponse)

            val status = jsonObject.optString("status", "unknown")
            val totalModismos = jsonObject.optInt("total_modismos", 0)
            val betoAvailable = jsonObject.optBoolean("beto_available", true)
            val phiAvailable = jsonObject.optBoolean("phi_available", true)

            val transcription = if (audioPath != null) {
                jsonObject.optString("original_text", "")
            } else {
                originalText
            }

            Log.d("HomeFragment", "Texto que se enviará a AnalysisActivity: $transcription")

            Log.d(TAG, "Análisis completado - Status: $status, Modismos: $totalModismos, BETO: $betoAvailable, PHI: $phiAvailable")

            when {
                status == "success" && totalModismos > 0 && betoAvailable && phiAvailable -> {
                    Log.d(TAG, "Caso exitoso: $totalModismos modismos detectados")
                    dismissLoadingDialog()
                    navigateToAnalysisActivity(jsonResponse, transcription, audioPath)
                }

                status == "success" && totalModismos == 0 -> {
                    Log.d(TAG, "Caso sin modismos")
                    showNoModismosDialog()
                }

                status == "partial_success" || !betoAvailable || !phiAvailable -> {
                    Log.d(TAG, "Caso pipeline parcial")
                    val service = when {
                        !betoAvailable && !phiAvailable -> "BETO y PHI"
                        !betoAvailable -> "BETO"
                        !phiAvailable -> "PHI"
                        else -> "servicios"
                    }
                    showPartialServiceDialog(service)
                }

                else -> {
                    Log.w(TAG, "Caso no manejado - Status: $status")
                    showErrorDialog("Respuesta inesperada", "El servidor devolvió una respuesta inválida")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta JSON", e)
            showErrorDialog("Error", "No se pudo interpretar la respuesta del servidor")
        }
    }

    private fun navigateToAnalysisActivity(apiResponse: String, textContent: String, audioPath: String?) {
        val intent = Intent(requireContext(), AnalysisActivity::class.java).apply {
            putExtra("api_response", apiResponse)
            putExtra("text_content", textContent)
            audioPath?.let { putExtra("audio_path", it) }
        }
        startActivity(intent)
    }

    private fun showLoadingDialog() {
        dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading_analysis, null)

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        loadingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
        dialogView = null
    }

    private fun showDialogState(
        title: String,
        message: String = "",
        showSpinner: Boolean = false,
        buttonText: String = "Aceptar",
        onButtonClick: (() -> Unit)? = null
    ) {
        dialogView?.let { view ->
            val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
            val tvMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
            val spinKit = view.findViewById<View>(R.id.spinKit)
            val btnAction = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDialogAction)

            tvTitle?.text = title

            if (message.isNotEmpty()) {
                tvMessage?.text = message
                tvMessage?.isVisible = true
            } else {
                tvMessage?.isVisible = false
            }

            spinKit?.isVisible = showSpinner

            btnAction?.isVisible = !showSpinner
            btnAction?.text = buttonText
            btnAction?.setOnClickListener {
                onButtonClick?.invoke() ?: dismissLoadingDialog()
            }
        }
    }

    private fun showNoModismosDialog() {
        showDialogState(
            title = "Sin modismos detectados",
            message = "No se encontraron modismos colombianos en el texto analizado.",
            showSpinner = false
        )
    }

    private fun showPartialServiceDialog(serviceName: String) {
        showDialogState(
            title = "Servicio no disponible",
            message = "El servicio de $serviceName no está disponible temporalmente. Intenta más tarde.",
            showSpinner = false
        )
    }

    private fun showErrorDialog(title: String, message: String) {
        showDialogState(
            title = title,
            message = message,
            showSpinner = false
        )
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

            // Validar duración del audio subido
            if (validateAudioDuration(audioFile.absolutePath)) {
                recordedAudioPath = audioFile.absolutePath
                showAudioPlayer(audioFile.absolutePath)
                Toast.makeText(requireContext(), "Audio subido correctamente", Toast.LENGTH_SHORT).show()
            } else {
                // Eliminar el archivo si excede el límite
                audioFile.delete()
                showAudioDurationErrorDialog()
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
                    val duration = duration
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
        httpClient.dispatcher.cancelAll()
        dismissLoadingDialog()
    }
}