package com.remlocteam.remloc1.foregroundLocationCheck

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.IBinder
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    private var intervalTime:Long = 5000L

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
            ACTION_RESTART ->{
                stop()
                serviceScope.launch {
                    delay(1000) // wait for 1 seconds
                    start()
                }
                start()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("UnlocalizedSms", "UnspecifiedImmutableFlag")
    private fun start() {

        var actionsFromDB: ArrayList<ActionsData>? = null
        val actionsDone = ActionsData()

        actionsFromDB = readData()

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location is tracked, you can turn it off in settings!")
            .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(this.resources,
            R.mipmap.ic_launcher))
            .setOngoing(true)



        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var stopService = false

        locationClient
            .getLocationUpdates(intervalTime)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->

                val currentLatLng = LatLng(location.latitude, location.longitude)


                actionsFromDB.forEach { action ->
                    stopService = action.placeName == null
                }

                if (stopService){
                    val updatedNotification = notification.setContentText(
                        "Stopping service"
                    )
                    notificationManager.notify(1, updatedNotification.build())
                    stop()

                }else{
                    val updatedNotification = notification.setContentText(
                        "Location is tracked, you can turn it off in settings!"
                    )
                    notificationManager.notify(1, updatedNotification.build())
                }





//              actionsFromDB.forEach { dataLine ->
                for (i in actionsFromDB.indices) {
                    Log.d("actionsFromDB1", actionsFromDB.toString())
                    val dataLine = actionsFromDB[i]
                    Log.d("dataLine1", dataLine.toString())
                    when(dataLine.actionType){

                        ("Sms") ->{

                            if (distance(currentLatLng, LatLng(dataLine.latitude!!, dataLine.longitude!!))){
                                // Get a reference to the SmsManager
                                val smsManager = SmsManager.getDefault()
                                // Send the SMS
                                smsManager.sendTextMessage(dataLine.phoneNumber, null, dataLine.smsText, null, null)

                                actionsFromDB[i] = actionsDone

                            }
                        }
                        ("Mute") ->{

                            if (distance(currentLatLng, LatLng(dataLine.latitude!!, dataLine.longitude!!))){

                                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

                                actionsFromDB[i] = actionsDone
                            }
                        }

                        ("Notification") ->{

                            if (distance(currentLatLng, LatLng(dataLine.latitude!!, dataLine.longitude!!))){
                                val newNotification = NotificationCompat.Builder(this, "location")
                                    .setContentTitle(dataLine.placeName)
                                    .setContentText(dataLine.smsText)
                                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                    .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
                                    .setLargeIcon(
                                        BitmapFactory.decodeResource(this.resources,
                                            R.mipmap.ic_launcher))
                                    .setOngoing(false)

                                notificationManager.notify(2, newNotification.build())

                                actionsFromDB[i] = actionsDone
                            }
                        }
                    }

                }
            }
            .launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESTART = "ACTION_RESTART"
    }


    private fun readData(): ArrayList<ActionsData>{

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val dataNow: ArrayList<ActionsData> = ArrayList()

        if (uid != null) {

            database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)
            database.child("Actions").get().addOnSuccessListener {
                if(it.exists()){

                    it.children.forEach { actionTypes ->
                        actionTypes.children.forEach { action ->

                            val contactName = action.child("contactName").value.toString()
                            val phoneNumber = action.child("phoneNumber").value.toString()
                            val smsText = action.child("smsText").value.toString()
                            val placeName = action.child("placeName").value.toString()
                            val actionType = action.child("actionType").value.toString()
                            val latitude = action.child("latitude").value as Double
                            val longitude = action.child("longitude").value as Double
                            var turnOn = action.child("turnOn").value

                            if (turnOn==null){
                                turnOn = true
                            }
                            else{
                                turnOn = turnOn as Boolean
                            }


                            if (turnOn){
                                val actionToDo = ActionsData(
                                    contactName,
                                    phoneNumber,
                                    smsText,
                                    placeName,
                                    actionType,
                                    latitude,
                                    longitude,
                                    turnOn
                                )
                                dataNow.add(actionToDo)
                            }






                        }
                    }
                }


            }.addOnFailureListener{

                Toast.makeText(this, getString(R.string.failed),Toast.LENGTH_SHORT).show()

            }
        }

        Log.d("dataNow",dataNow.toString())
        return dataNow

    }


    private fun distance(currentLatLng: LatLng, placeLatLng: LatLng): Boolean{
        val theta = currentLatLng.longitude - placeLatLng.longitude
        var dist = Math.sin(deg2rad(currentLatLng.latitude)) * Math.sin(deg2rad(placeLatLng.latitude)) + Math.cos(deg2rad(currentLatLng.latitude)) * Math.cos(deg2rad(placeLatLng.latitude)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        dist *= 1.609344
        dist *= 1000 // distance in meters
        val triggerRange = getSliderValue()

        if (dist <= triggerRange) {
            return true
        }
        return false
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    private fun getSliderValue(): Int {
        val sp: SharedPreferences = getSharedPreferences("Distance", MODE_PRIVATE)
        return sp.getInt("TriggerDistanceValue", 10)
    }

}