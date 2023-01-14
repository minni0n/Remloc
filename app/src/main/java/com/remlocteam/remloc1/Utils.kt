package com.remlocteam.remloc1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Utils {
    fun checkAllPermissions(context: Context) {
        var locationGranted = false
        var contactsGranted = false
        var smsGranted = false

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            locationGranted = true
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            contactsGranted = true
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
        {
            smsGranted = true
        }

        if (!locationGranted || !contactsGranted || !smsGranted){
            val intent = Intent(context, PermissionActivity::class.java)
            context.startActivity(intent)
        }
    }
}