package com.example.weatherapp.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.weatherapp.utils.DialogManager

/* Check is permission name (String) granted. We will check ACCESS_FINE_LOCATION */
fun Fragment.isPermissionGranted(permissionName: String): Boolean {
    return ContextCompat.checkSelfPermission(
        activity as AppCompatActivity, permissionName) == PackageManager.PERMISSION_GRANTED
}

/* Show locationDisabledDialog from DialogManager after check
 * geolocation enable or disable (not permission) */
fun Fragment.showLocationDisabledDialog(context: Context) {
    DialogManager.locationSettingsDialog(context, object: DialogManager.Listener{
        override fun onClick(name: String?) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) // geolocation settings
        }
    })
}

/* vibration fun in all fragments */
fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibLength: Long = 45
    if (vibrator.hasVibrator()) { // vibrator availability checking
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // new vibrate method for API Level 26 or higher
            vibrator.vibrate(VibrationEffect.createOneShot(vibLength, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(vibLength) // old vibrate method for below API Level 26
        }
    }
}

/* extension 2 fun for hiding keyboard */
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}
fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}