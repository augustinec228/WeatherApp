package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Включить местоположение?")
        dialog.setMessage("Местоположение выключено, хотите включить?")
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Включить") { _, _ ->
            listener.onClick()
            dialog.dismiss()


        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена") { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener {
        fun onClick()
    }

}