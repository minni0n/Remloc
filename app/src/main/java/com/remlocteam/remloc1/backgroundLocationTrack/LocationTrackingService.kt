package com.remlocteam.remloc1.backgroundLocationTrack


import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.location.Location
import android.media.AudioManager
import android.os.IBinder
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.Data.PlacesData
import com.remlocteam.remloc1.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LocationTrackingService : Service() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private var placesToDo: MutableList<Pair<ActionsData, Boolean>> = mutableListOf()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(DelicateCoroutinesApi::class)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.lastOrNull()?.let { location ->
                GlobalScope.launch { send(location) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid).child("Actions")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Śledzenie lokalizacji...")
            .setContentText("Lokalizacja jest śledzona, możesz to wyłączyć w aplikacji!")
            .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(this.resources,
                    R.mipmap.ic_launcher))
            .build()

        startForeground(1, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        placesToDo.clear()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun send(currentLocation: Location) {

        Log.d("LocationTrackingService", "Current location: ${currentLocation.latitude}, ${currentLocation.longitude}")
        refreshDatabase()
        addActionToDo(currentLocation)
        if (placesToDo.isNotEmpty()){
            getActionsDone()
        }

    }


    private fun getActionsDone(){
        placesToDo.forEachIndexed{ index, (place, state)  ->
            Log.d("LocationTrackingService", place.toString())
            Log.d("LocationTrackingService", state.toString())
            if (!state){
                when(place.actionType){

                    ("Sms")->{
                        val smsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(place.phoneNumber, null, place.smsText, null, null)

                        placesToDo[index] = Pair(place, true)
                    }
                    ("Notification")->{
                        val newNotification = NotificationCompat.Builder(this@LocationTrackingService, "location")
                            .setContentTitle(place.placeName)
                            .setContentText(place.smsText)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                            .setOngoing(false)

                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(2, newNotification.build())

                        placesToDo[index] = Pair(place, true)
                    }
                    ("Mute")->{
                        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

                        placesToDo[index] = Pair(place, true)
                    }
                }
            }
        }

    }


    private fun addActionToDo(currentLocation: Location){
        database.get().addOnSuccessListener { actionTypes ->
            actionTypes.children.forEach { key ->
                key.children.forEach { action ->

                    val placeLong = action.child("longitude").value as Double
                    val placelatt = action.child("latitude").value as Double

                    val placeLocation = Location("Place Location")
                    placeLocation.latitude = placelatt
                    placeLocation.longitude = placeLong

                    val turnOn = action.child("turnOn").value as Boolean
                    val phoneNumber = action.child("phoneNumber").value.toString()
                    val smsText = action.child("smsText").value.toString()
                    val placeName = action.child("placeName").value.toString()
                    val actionType = action.child("actionType").value.toString()
                    val contactName = action.child("contactName").value.toString()

                    val actionData = ActionsData(contactName, phoneNumber, smsText, placeName, actionType, placelatt, placeLong, turnOn)

                    if (distanceBetweenAB(currentLocation, placeLocation) <= getSliderValue()){

                        if (turnOn && !placesToDo.contains(Pair(actionData, true))){
                            placesToDo.add(Pair(actionData, false))
                        }
                    }else{
                        if (placesToDo.contains(Pair(actionData, true))){
                            placesToDo.remove(Pair(actionData, true))
                        }
                    }
                }
            }
        }
    }




    private fun refreshDatabase() {

        database.child("Actions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(actionsInfo: DataSnapshot) {
                database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid).child("Actions")

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LocationTrackingService", "Error reading location data: ${error.message}")
            }
        })
    }

    private fun distanceBetweenAB(location1: Location, location2: Location): Double {
        val distance = location1.distanceTo(location2)

        return distance.toDouble()
    }


    private fun getSliderValue(): Int {
        val sp: SharedPreferences = getSharedPreferences("Distance", MODE_PRIVATE)
        return sp.getInt("TriggerDistanceValue", 10)
    }

    override fun onBind(intent: Intent?): IBinder? = null


//        private fun getData(currentLocation: Location){
//
//        actionsInfo.children.forEach { actionsTypes ->
//            actionsTypes.children.forEach{ action ->
//
//
//
//                val placeLong = action.child("longitude").value as Double
//                val placelatt = action.child("latitude").value as Double
//
//                val placeLocation = Location("Location 2")
//                placeLocation.latitude = placelatt
//                placeLocation.longitude = placeLong
//
//                if (distanceBetweenAB(currentLocation, placeLocation) < getSliderValue()){
//                    val turnOn = action.child("turnOn").value as Boolean
//                    if (turnOn){
//                        val phoneNumber = action.child("phoneNumber").value.toString()
//                        val smsText = action.child("smsText").value.toString()
//                        val placeName = action.child("placeName").value.toString()
//                        val actionType = action.child("actionType").value.toString()
//
//
//                        when(actionType){
//                            ("Sms")->{
//                                val smsManager = SmsManager.getDefault()
//                                // Send the SMS
//                                smsManager.sendTextMessage(phoneNumber, null, smsText, null, null)
//
//                                database.child("Actions/$actionType/${action.key}/turnOn").setValue(false)
//
//                                Log.d("LocationTrackingService", "turnOn: $action")
//
//                            }
//                            ("Notification")->{
//                                val newNotification = NotificationCompat.Builder(this@LocationTrackingService, "location")
//                                    .setContentTitle(placeName)
//                                    .setContentText(smsText)
//                                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                                    .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
//                                    .setLargeIcon(
//                                        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
//                                    .setOngoing(false)
//
//                                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                                notificationManager.notify(2, newNotification.build())
//
//                                database.child("Actions/$actionType/${action.key}/turnOn").setValue(false)
//                            }
//                            ("Mute")->{
//                                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
//
//                                database.child("Actions/$actionType/${action.key}/turnOn").setValue(false)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}
