package com.example.weatherapp.utils

import android.app.AlertDialog
import android.content.Context

object DialogManager {

    /* AlertDialog: shows when location is disable (not to be confused with permission)
    * Using which extension fun of Fragment */
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Enable location?")
        dialog.setMessage("Location disabled. Do you want to enable location?")
        // positive button open geolocation setting. Will override in extension of Fragment
        // _,_ variables that we don't need, but we need to give them a name
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _,_ ->
            listener.onClick(null) // handle OK click. Name doesn't matter
            dialog.dismiss()
        }
        // negative button close dialog nothing to do
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){ _,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    /* interface of clocking on dialog buttons from fragment */
    interface Listener{
        fun onClick(name: String?)
    }
}