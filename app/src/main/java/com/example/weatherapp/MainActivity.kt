package com.example.weatherapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.fragments.MainFragment


/**
 * Single activity. Contain MainFragment. Connect MainFragment
 * and DaysFragment or Hours Fragment for using SwipeInterface.
 * Play sounds and define ringer mode
 * */

class MainActivity : AppCompatActivity(), SwipeInterface {
    private lateinit var mainFragment: MainFragment
    var playSound = true // sound button (enable or disable status)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainFragment = MainFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, mainFragment)
            .commit()

        /* register BroadcastReceiver: when user change ringer
        * mode, receiver change playSound status and soundImage */
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val audioManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                playSound = !(audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
                        || audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)
                mainFragment.checkSound(this@MainActivity)
            }
        }
        val filter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        registerReceiver(receiver, filter)
    }

    /* implement swipe interface for update weather in card of MainFragment
    * after swipe from DaysFragment or HoursFragment via MainActivity (like connector) */
    override fun onSwipe() {
        mainFragment.getLocation()
    }

    /* hide search city FrameLay when it open and back pressed */
    override fun onBackPressed() {
        if(mainFragment.searchFrameLayGlobField.visibility == View.VISIBLE)
            mainFragment.clickSearchFrameLay(show = false, vibrate = true, clearText = true, standartAdapter = true, hideKey = true)
        else super.onBackPressed()
    }

    /* play sound fun. It using in fragments. Default 'sun' sound and char symbol is 'D'.
    * 'R' is rain. 'S' is snow. 'C' cloud.
    * Don't play if ring mode is silent or vibrate or turn off play sound button */
    fun playSound(condition: Char) {
        if(!playSound) return
        var resId = resources.getIdentifier(R.raw.sun.toString(),"raw", this.packageName)
        when(condition) {
                'R' -> resId = resources.getIdentifier(R.raw.rain.toString(),"raw", this.packageName)
                'S' -> resId = resources.getIdentifier(R.raw.snow.toString(),"raw", this.packageName)
                'C' -> resId = resources.getIdentifier(R.raw.cloud.toString(),"raw", this.packageName)
        }
        val mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer.start()
    }
}