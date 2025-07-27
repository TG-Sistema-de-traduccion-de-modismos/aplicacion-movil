package com.proyecto.modismos.activities

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.proyecto.modismos.R
import java.io.File

class AudioRecorderActivity : AppCompatActivity() {

    private lateinit var btnClose: ImageView
    private lateinit var tvTimer: TextView
    private lateinit var tvRecordingStatus: TextView
    private lateinit var fabStop: FloatingActionButton
    private lateinit var fabPause: FloatingActionButton
    private lateinit var circleOuter: View
    private lateinit var circleMiddle: View
    private lateinit var microphoneContainer: View

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var audioUri: Uri? = null
    private var isRecording = false
    private var isPaused = false
    private var startTime = 0L
    private var pausedTime = 0L
    private var wasCancelled = false


    private var timer: CountDownTimer? = null
    private var pulseAnimator: AnimatorSet? = null

    companion object {
        const val MAX_RECORDING_TIME = 60000L // 1 minuto en milisegundos
        const val EXTRA_AUDIO_PATH = "audio_path"
        const val EXTRA_AUDIO_URI = "audio_uri"
        private const val TAG = "AudioRecorderActivity"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            Toast.makeText(this, "Permiso de micrófono requerido", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_recorder)

        supportActionBar?.hide()
        setupTransparentBars()


        initViews()
        setupClickListeners()
        setupBackPressedHandler()
        checkPermissionAndStart()
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopRecordingAndFinish()
            }
        })
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

    private fun initViews() {
        btnClose = findViewById(R.id.btnClose)
        tvTimer = findViewById(R.id.tvTimer)
        tvRecordingStatus = findViewById(R.id.tvRecordingStatus)
        fabStop = findViewById(R.id.fabStop)
        fabPause = findViewById(R.id.fabPause)
        circleOuter = findViewById(R.id.circleOuter)
        circleMiddle = findViewById(R.id.circleMiddle)
        microphoneContainer = findViewById(R.id.microphoneContainer)
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            wasCancelled = true
            stopRecordingAndFinish()
        }

        fabStop.setOnClickListener {
            stopRecordingAndFinish()
        }

        fabPause.setOnClickListener {
            if (isPaused) {
                resumeRecording()
            } else {
                pauseRecording()
            }
        }
    }

    private fun checkPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startRecording() {
        try {
            // Crear archivo en almacenamiento interno (más seguro y no requiere permisos especiales)
            val audioDir = File(filesDir, "audio_recordings")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            audioFile = File(audioDir, "recording_${System.currentTimeMillis()}.m4a")

            // Para Android 10+ también podemos usar MediaStore si queremos guardar en almacenamiento compartido
            // pero para esta funcionalidad, el almacenamiento interno es suficiente

            // Configurar MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                setMaxDuration(MAX_RECORDING_TIME.toInt())

                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        runOnUiThread {
                            stopRecordingAndFinish()
                        }
                    }
                }

                try {
                    prepare()
                    start()
                } catch (e: Exception) {
                    Log.e(TAG, "Error preparing MediaRecorder", e)
                    throw e
                }
            }

            isRecording = true
            startTime = System.currentTimeMillis()
            startTimer()
            startPulseAnimation()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            Toast.makeText(this, "Error al iniciar grabación: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pauseRecording() {
        if (isRecording && !isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                isPaused = true
                pausedTime = System.currentTimeMillis()

                timer?.cancel()
                stopPulseAnimation()

                tvRecordingStatus.text = "Pausado"
                fabPause.setImageResource(R.drawable.ic_play)
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing recording", e)
                Toast.makeText(this, "Error al pausar grabación", Toast.LENGTH_SHORT).show()
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "Pausar no disponible en esta versión de Android", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resumeRecording() {
        if (isRecording && isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                isPaused = false

                // Ajustar el tiempo de inicio
                startTime += (System.currentTimeMillis() - pausedTime)

                startTimer()
                startPulseAnimation()

                tvRecordingStatus.text = "Grabando..."
                fabPause.setImageResource(R.drawable.ic_pause)
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming recording", e)
                Toast.makeText(this, "Error al reanudar grabación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecordingAndFinish() {
        stopRecording()

        if (wasCancelled) {
            setResult(RESULT_CANCELED)  // ← Esto no devuelve audio
        } else {
            // Devolver el path del archivo de audio solo si no fue cancelado
            audioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    intent.putExtra(EXTRA_AUDIO_PATH, file.absolutePath)
                    audioUri?.let { uri ->
                        intent.putExtra(EXTRA_AUDIO_URI, uri.toString())
                    }
                    setResult(RESULT_OK, intent)
                    Log.d(TAG, "Audio saved: ${file.absolutePath}, size: ${file.length()} bytes")
                } else {
                    Log.w(TAG, "Audio file is empty or doesn't exist")
                }
            }
        }

        finish()
    }


    private fun stopRecording() {
        timer?.cancel()
        stopPulseAnimation()

        try {
            mediaRecorder?.apply {
                if (isRecording) {
                    stop()
                    Log.d(TAG, "Recording stopped successfully")
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            // Si hay error al detener, aún podemos intentar guardar lo que se grabó
        }

        mediaRecorder = null
        isRecording = false
        isPaused = false
    }

    private fun startTimer() {
        val remainingTime = MAX_RECORDING_TIME - (System.currentTimeMillis() - startTime)

        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsedTime = MAX_RECORDING_TIME - millisUntilFinished
                val minutes = (elapsedTime / 1000) / 60
                val seconds = (elapsedTime / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                stopRecordingAndFinish()
            }
        }.start()
    }

    private fun startPulseAnimation() {
        // Animación de pulso para los círculos
        val pulseOuter = ObjectAnimator.ofFloat(circleOuter, "scaleX", 1f, 1.2f, 1f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val pulseOuterY = ObjectAnimator.ofFloat(circleOuter, "scaleY", 1f, 1.2f, 1f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val pulseMiddle = ObjectAnimator.ofFloat(circleMiddle, "scaleX", 1f, 1.15f, 1f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = 200
        }

        val pulseMiddleY = ObjectAnimator.ofFloat(circleMiddle, "scaleY", 1f, 1.15f, 1f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = 200
        }

        // Animación sutil del micrófono
        val micPulse = ObjectAnimator.ofFloat(microphoneContainer, "scaleX", 1f, 1.05f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val micPulseY = ObjectAnimator.ofFloat(microphoneContainer, "scaleY", 1f, 1.05f, 1f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        pulseAnimator = AnimatorSet().apply {
            playTogether(pulseOuter, pulseOuterY, pulseMiddle, pulseMiddleY, micPulse, micPulseY)
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}