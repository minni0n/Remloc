package com.example.remloc1.Actions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MuteDevice(var activity: Activity): AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    fun checkMutePermission() {

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.MODIFY_AUDIO_SETTINGS),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }else{
            mutePhone()
        }
    }

    private fun mutePhone() {

        val audiomanager = activity.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        audiomanager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

}