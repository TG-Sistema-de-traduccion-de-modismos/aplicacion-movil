package com.proyecto.modismos.network

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiService {

    companion object {
        private const val TAG = "ApiService"
        private const val API_GATEWAY_BASE_URL = "https://modistra-api.duckdns.org"
        private const val ANALYZE_TEXT_ENDPOINT = "$API_GATEWAY_BASE_URL/text"
        private const val ANALYZE_AUDIO_ENDPOINT = "$API_GATEWAY_BASE_URL/audio"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    interface ApiCallback {
        fun onSuccess(response: String)
        fun onError(errorType: ErrorType, message: String)
    }

    enum class ErrorType {
        NETWORK_ERROR,
        UNAUTHORIZED,
        SERVER_ERROR,
        PARSING_ERROR,
        FILE_NOT_FOUND
    }

    fun analyzeText(text: String, idToken: String, callback: ApiCallback) {
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
            .addHeader("Authorization", "Bearer $idToken")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en análisis de texto", e)
                callback.onError(
                    ErrorType.NETWORK_ERROR,
                    "No se pudo conectar con el servidor. Verifica tu conexión a internet."
                )
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Respuesta análisis texto: ${response.code} - $responseBody")

                when {
                    response.isSuccessful -> callback.onSuccess(responseBody)
                    response.code == 401 -> callback.onError(
                        ErrorType.UNAUTHORIZED,
                        "Sesión expirada. Por favor inicia sesión nuevamente."
                    )
                    else -> callback.onError(
                        ErrorType.SERVER_ERROR,
                        "El servidor respondió con código: ${response.code}"
                    )
                }
            }
        })
    }

    fun analyzeAudio(audioPath: String, idToken: String, callback: ApiCallback) {
        val audioFile = File(audioPath)

        if (!audioFile.exists()) {
            callback.onError(ErrorType.FILE_NOT_FOUND, "No se encontró el archivo de audio")
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
            .addHeader("Authorization", "Bearer $idToken")
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error en análisis de audio", e)
                callback.onError(
                    ErrorType.NETWORK_ERROR,
                    "No se pudo conectar con el servidor. Verifica tu conexión a internet."
                )
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Respuesta análisis audio: ${response.code} - $responseBody")

                when {
                    response.isSuccessful -> callback.onSuccess(responseBody)
                    response.code == 401 -> callback.onError(
                        ErrorType.UNAUTHORIZED,
                        "Sesión expirada. Por favor inicia sesión nuevamente."
                    )
                    else -> callback.onError(
                        ErrorType.SERVER_ERROR,
                        "El servidor respondió con código: ${response.code}"
                    )
                }
            }
        })
    }

    fun cancelAllRequests() {
        httpClient.dispatcher.cancelAll()
    }
}