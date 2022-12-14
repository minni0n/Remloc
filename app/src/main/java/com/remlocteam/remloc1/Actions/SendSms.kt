package com.remlocteam.remloc1.Actions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.text.TextUtils
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SendSms(var number:String, var message: String, var activity: Activity) {

    lateinit var button: Button
    private val permissionRequest = 101

    fun sendMessage() {
        val permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            myMessage()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS),
                permissionRequest)
        }
    }

    private fun myMessage() {
        var myNumber: String = number.trim()

        val re = Regex("[^0-9]")
        myNumber = re.replace(myNumber, "")

        val myMsg: String = message.trim()
//        Toast.makeText(activity, myNumber, Toast.LENGTH_SHORT).show()
        if (myNumber == "" || myMsg == "") {
//            Toast.makeText(activity, "Field cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            if (TextUtils.isDigitsOnly(myNumber)) {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(myNumber, null, myMsg, null, null)
//                Toast.makeText(activity, "Message Sent", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(activity, "Please enter the correct number", Toast.LENGTH_SHORT).show()
            }
        }
    }
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults:
//    IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == permissionRequest) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                myMessage();
//            } else {
//                Toast.makeText(this, "You don't have required permission to send a message",
//                    Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

}