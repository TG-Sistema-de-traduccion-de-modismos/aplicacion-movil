package com.proyecto.modismos.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.proyecto.modismos.R

class DialogManager(private val context: Context) {

    private var loadingDialog: AlertDialog? = null
    private var dialogView: View? = null

    fun showLoadingDialog() {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading_analysis, null)

        loadingDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        loadingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog?.show()
    }

    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
        dialogView = null
    }

    fun showDialogState(
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
            val btnAction = view.findViewById<MaterialButton>(R.id.btnDialogAction)

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

    fun showNoModismosDialog() {
        showDialogState(
            title = "Sin modismos detectados",
            message = "No se encontraron modismos colombianos en el texto analizado.",
            showSpinner = false
        )
    }

    fun showPartialServiceDialog(serviceName: String) {
        showDialogState(
            title = "Servicio no disponible",
            message = "El servicio de $serviceName no está disponible temporalmente. Intenta más tarde.",
            showSpinner = false
        )
    }

    fun showErrorDialog(title: String, message: String) {
        showDialogState(
            title = title,
            message = message,
            showSpinner = false
        )
    }

    fun showAudioDurationErrorDialog() {
        AlertDialog.Builder(context)
            .setTitle("Audio muy largo")
            .setMessage("El audio no puede superar los 50 segundos. Por favor, graba o selecciona un audio más corto.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    fun isShowing(): Boolean = loadingDialog?.isShowing == true
}