package com.example.remloc1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.remloc1.databinding.FragmentActionsBinding


class ActionsFragment : Fragment() {

    lateinit var button: Button
    lateinit var editTextNumber: EditText
    lateinit var editTextMessage: EditText
    private val permissionRequest = 101

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment



        val bind = FragmentActionsBinding.inflate(layoutInflater)

        editTextNumber = bind.editTextNum
        editTextMessage = bind.editTextMsg
        button = bind.btnSendMsg

        button.setOnClickListener{
            sendMessage()
        }

        return bind.root
    }


    private fun sendMessage() {
        val permissionCheck = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.SEND_SMS) }
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            myMessage()
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(Manifest.permission.SEND_SMS),
                    permissionRequest)
            }
        }
    }
    private fun myMessage() {
        val myNumber: String = editTextNumber.text.toString().trim()
        val myMsg: String = editTextMessage.text.toString().trim()
        if (myNumber == "" || myMsg == "") {
            Toast.makeText(activity, "Field cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            if (TextUtils.isDigitsOnly(myNumber)) {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(myNumber, null, myMsg, null, null)
                Toast.makeText(activity, "Message Sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Please enter the correct number", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults:
    IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequest) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myMessage();
            } else {
                Toast.makeText(activity, "You don't have required permission to send a message",
                    Toast.LENGTH_SHORT).show();
            }
        }
    }


}