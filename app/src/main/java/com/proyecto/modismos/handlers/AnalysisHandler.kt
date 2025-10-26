package com.proyecto.modismos.handlers

import android.util.Log
import org.json.JSONObject

class AnalysisHandler {

    companion object {
        private const val TAG = "AnalysisHandler"
    }

    data class AnalysisResult(
        val status: String,
        val totalModismos: Int,
        val betoAvailable: Boolean,
        val phiAvailable: Boolean,
        val transcription: String
    )

    interface AnalysisCallback {
        fun onSuccess(result: AnalysisResult, jsonResponse: String)
        fun onNoModismos()
        fun onPartialService(serviceName: String)
        fun onError(message: String)
    }

    fun handleAnalysisResponse(
        jsonResponse: String,
        originalText: String,
        audioPath: String?,
        callback: AnalysisCallback
    ) {
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

            Log.d(TAG, "Texto que se enviará: $transcription")
            Log.d(TAG, "Análisis - Status: $status, Modismos: $totalModismos, BETO: $betoAvailable, PHI: $phiAvailable")

            val result = AnalysisResult(
                status = status,
                totalModismos = totalModismos,
                betoAvailable = betoAvailable,
                phiAvailable = phiAvailable,
                transcription = transcription
            )

            when {
                status == "success" && totalModismos > 0 && betoAvailable && phiAvailable -> {
                    Log.d(TAG, "Caso exitoso: $totalModismos modismos detectados")
                    callback.onSuccess(result, jsonResponse)
                }

                status == "success" && totalModismos == 0 -> {
                    Log.d(TAG, "Caso sin modismos")
                    callback.onNoModismos()
                }

                status == "partial_success" || !betoAvailable || !phiAvailable -> {
                    Log.d(TAG, "Caso pipeline parcial")
                    val service = when {
                        !betoAvailable && !phiAvailable -> "BETO y PHI"
                        !betoAvailable -> "BETO"
                        !phiAvailable -> "PHI"
                        else -> "servicios"
                    }
                    callback.onPartialService(service)
                }

                else -> {
                    Log.w(TAG, "Caso no manejado - Status: $status")
                    callback.onError("El servidor devolvió una respuesta inválida")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta JSON", e)
            callback.onError("No se pudo interpretar la respuesta del servidor")
        }
    }
}