package com.remlocteam.remloc1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_permission.*

class PermissionActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    data class Results(val locationGranted: Boolean, val contactsGranted: Boolean, val smsGranted: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        title = getString(R.string.permissions)

        if (checkAllPermissions().locationGranted && checkAllPermissions().contactsGranted && checkAllPermissions().smsGranted){
            // Start location service
//            Intent(applicationContext, LocationService::class.java).apply {
//                action = LocationService.ACTION_START
//                startService(this)
//            }
            // Start Activity
            startActivity(Intent(this, HomeActivity::class.java))
        }

        checkForSwitchPosition()

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                requestLocationPermission()
            }else{
                checkForSwitchPosition()
            }
        }

        contactsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                requestContactsPermission()
            }else{
                checkForSwitchPosition()
            }
        }

        smsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                requestSmsPermission()
            }else{
                checkForSwitchPosition()
            }
        }



        nextButton.setOnClickListener {

            if (locationSwitch.isChecked && contactsSwitch.isChecked && smsSwitch.isChecked){

                // Start location service
//                Intent(applicationContext, LocationService::class.java).apply {
//                action = LocationService.ACTION_START
//                startService(this)
//                }
                // Start Activity
                startActivity(Intent(this, HomeActivity::class.java))
            }
            else{
                Toast.makeText(this, "Please give all of the permissions!", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun checkForSwitchPosition(){
        val permissions = checkAllPermissions()

        if (permissions.locationGranted){
            locationSwitch.isChecked = true
            locationSwitch.isEnabled = false
        } else{
            locationSwitch.isChecked = false
        }

        if (permissions.contactsGranted){
            contactsSwitch.isChecked = true
            contactsSwitch.isEnabled = false
        } else{
            contactsSwitch.isChecked = false
        }

        if (permissions.smsGranted){
            smsSwitch.isChecked = true
            smsSwitch.isEnabled = false
        } else{
            smsSwitch.isChecked = false
        }

    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this@PermissionActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this@PermissionActivity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this@PermissionActivity,
            arrayOf(Manifest.permission.SEND_SMS),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted
                    checkForSwitchPosition()
                } else {
                    // permission denied
                    checkForSwitchPosition()
                }
                return
            }


            // Add similar code for contacts and SMS permissions
        }
    }



    private fun checkAllPermissions(): Results {
        var locationGranted = false
        var contactsGranted = false
        var smsGranted = false

        if (ActivityCompat.checkSelfPermission(
                this@PermissionActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            locationGranted = true
        }
        if (ActivityCompat.checkSelfPermission(
                this@PermissionActivity,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            contactsGranted = true
        }
        if (ActivityCompat.checkSelfPermission(
                this@PermissionActivity,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
        {
            smsGranted = true
        }

        return Results(locationGranted, contactsGranted, smsGranted)
    }
}