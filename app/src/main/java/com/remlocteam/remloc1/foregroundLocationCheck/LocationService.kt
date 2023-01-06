package com.remlocteam.remloc1.foregroundLocationCheck

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.IBinder
import android.provider.Settings
import android.telephony.SmsManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    private var intervalTime:Long = 5000L

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var actions: MutableList<String>
    private lateinit var keys: MutableList<String>
    private lateinit var actionTypeArray: MutableList<String>

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
                start()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("UnlocalizedSms", "UnspecifiedImmutableFlag")
    private fun start() {

        var actionsFromDB: ArrayList<ActionsData>? = null
        val actionsDone = ActionsData()

        actionTypeArray = mutableListOf("")
        actionTypeArray.clear()
        actions = mutableListOf("")
        actions.clear()
        keys = mutableListOf("")
        keys.clear()
        var notificationId = 1

        actionsFromDB = readData()


        //fixxxxxxx
//        val intent = Intent(this, HomeActivity::class.java).apply {
//            putExtra("fragment", "settings")
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
//            .setContentIntent(pendingIntent)
            .setLargeIcon(
                BitmapFactory.decodeResource(this.resources,
            R.mipmap.ic_launcher))
            .setOngoing(true)




        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(intervalTime)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()

                val currentLatLng = LatLng(location.latitude, location.longitude)

                actionsFromDB.forEach { dataLine ->

                    val datalong = dataLine.longitude
                    val dataLatt = dataLine.latitude

                    if (datalong != null && dataLatt != null){
                        val timeToAction = ((distancePrecise(currentLatLng, LatLng(dataLatt, datalong))/30.0)/2).toLong()
                        println("timeToAction $timeToAction")
                        if(intervalTime > timeToAction){
                            intervalTime = timeToAction * 1000

                        }
                        println("intervalTime $intervalTime")
                    }
                }


                val updatedNotification = notification.setContentText(
                    "Lat: $lat Long: $long"
                )
                notificationManager.notify(1, updatedNotification.build())

                // Check for distance




//              actionsFromDB.forEach { dataLine ->
                for (i in actionsFromDB.indices) {
                    val dataLine = actionsFromDB[i]
                    when(dataLine.actionType){

                        this.getString(R.string.sms) ->{

                            if (distance(currentLatLng, LatLng(dataLine.latitude!!, dataLine.longitude!!))){
                                // Get a reference to the SmsManager
                                val smsManager = SmsManager.getDefault()
                                // Send the SMS
                                smsManager.sendTextMessage(dataLine.phoneNumber, null, dataLine.smsText, null, null)

                                actionsFromDB[i] = actionsDone

                            }
                        }
                        this.getString(R.string.mute_the_sound) ->{

                            if (distance(currentLatLng, LatLng(dataLine.latitude!!, dataLine.longitude!!))){
                                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

                                actionsFromDB[i] = actionsDone
                            }
                        }

                        this.getString(R.string.notification) ->{

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

                            val id = action.key
                            if (id != null) {
                                keys.add(id)
                            }

                            val contactName = action.child("contactName").value.toString()
                            val phoneNumber = action.child("phoneNumber").value.toString()
                            val smsText = action.child("smsText").value.toString()
                            val placeName = action.child("placeName").value.toString()
                            val actionType = action.child("actionType").value.toString()
                            val latitude = action.child("latitude").value as Double
                            val longitude = action.child("longitude").value as Double

                            actions.add(smsText + "\n" + placeName)

                            actionTypeArray.add(actionType)

                            when(actionType){
                                "Sms" -> {
                                    val action1 = ActionsData(
                                        contactName,
                                        phoneNumber,
                                        smsText,
                                        placeName,
                                        this.getString(R.string.sms),
                                        latitude,
                                        longitude
                                    )

                                    dataNow.add(action1)
                                }
                                "Mute" -> {
                                    val action1 = ActionsData(
                                        contactName,
                                        phoneNumber,
                                        smsText,
                                        placeName,
                                        this.getString(R.string.mute_the_sound),
                                        latitude,
                                        longitude
                                    )

                                    dataNow.add(action1)
                                }
                                "Notification" -> {
                                    val action1 = ActionsData(
                                        contactName,
                                        phoneNumber,
                                        smsText,
                                        placeName,
                                        this.getString(R.string.notification),
                                        latitude,
                                        longitude
                                    )

                                    dataNow.add(action1)
                                }

                            }
                        }
                    }
                }


            }.addOnFailureListener{

                Toast.makeText(this, getString(R.string.failed),Toast.LENGTH_SHORT).show()

            }
        }

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

    private fun distancePrecise(currentLatLng: LatLng, placeLatLng: LatLng): Double {
        val theta = currentLatLng.longitude - placeLatLng.longitude
        var dist = Math.sin(deg2rad(currentLatLng.latitude)) * Math.sin(deg2rad(placeLatLng.latitude)) + Math.cos(deg2rad(currentLatLng.latitude)) * Math.cos(deg2rad(placeLatLng.latitude)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        dist *= 1.609344
        dist *= 1000 // distance in meters
        return dist
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