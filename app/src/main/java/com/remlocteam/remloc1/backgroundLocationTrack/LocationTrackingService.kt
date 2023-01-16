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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class LocationTrackingService : Service() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private var placesToDo: MutableList<Pair<ActionsData, Boolean>> = mutableListOf()
    private var placesLocations: MutableList<Location> = mutableListOf()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(DelicateCoroutinesApi::class)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.lastOrNull()?.let { location ->
                GlobalScope.launch(Dispatchers.Main) { send(location) }
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
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle(getString(R.string.location_tracking__))
            .setContentText(getString(R.string.notifiaction_location))
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
        fusedLocationClient.removeLocationUpdates(locationCallback)
        placesToDo.clear()
        stopForeground(true)
        stopSelf()
    }

    private fun send(currentLocation: Location) {


        refreshDatabase()
        addActionToDo(currentLocation)

        if (placesLocations.isNotEmpty()){
            val nearest = nearestLocation(placesLocations, currentLocation)
            val dist = distanceBetweenAB(nearest, currentLocation)

            setNewLocationRequest(dist)

            Log.d("LocationTrackingService", "$dist")
        }else{
//            setNewLocationRequest(5000)
        }

        Log.d("LocationTrackingService", "Current location: ${currentLocation.latitude}, ${currentLocation.longitude}")


        if (placesToDo.isNotEmpty()){
            // remove all elements from the list
            getActionsDone()
        }

        placesLocations.clear()
    }


    private fun getActionsDone(){
        placesToDo.forEachIndexed{ index, (place, state)  ->
//            Log.d("LocationTrackingService", place.toString())
//            Log.d("LocationTrackingService", state.toString())
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
                    val actionDataToCheck = ActionsData(contactName, phoneNumber, smsText, placeName, actionType, placelatt, placeLong, true or false)

                    val distance = distanceBetweenAB(currentLocation, placeLocation).roundToLong()

                    val location = Location("nearestLocation")
                    location.longitude = placeLong
                    location.latitude = placelatt

                    placesLocations.add(location)


                    if (distance <= getSliderValue()){

                        if (turnOn && !placesToDo.contains(Pair(actionDataToCheck, true))){
                            placesToDo.add(Pair(actionData, false))
                        }
                    }else{

                        if (placesToDo.contains(Pair(actionDataToCheck, true))){
                            placesToDo.remove(Pair(actionDataToCheck, true))
                        }
                    }
                }
            }
        }
//        Log.d("LocationTrackingService", "$closestPlace")
    }


    @SuppressLint("MissingPermission")
    private fun setNewLocationRequest(distance: Double){
        var time = calculateTravelTime(distance)

        if (time<7000){
            time = 7000
        }else if (time>20000){
            time = 20000
        }

//        time = 5000

        Log.d("LocationTrackingService", time.toString())
        val locationRequest = LocationRequest().apply {
            interval = time
            fastestInterval = time
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

    }

    private fun calculateTravelTime(distance: Double): Long {
        val speed = 20.0
        val timeInSeconds = (distance / speed )/2
        return timeInSeconds.toLong() * 1000 // convert seconds to milliseconds
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


    // The Haversine formula
    private fun nearestLocation(locations: List<Location>, currentLocation: Location): Location {
        var nearestLocation = locations[0]
        var nearestDistance = distance(currentLocation, nearestLocation)
        for (location in locations) {
            val distance = distance(currentLocation, location)
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestLocation = location
            }    }
        return nearestLocation
    }

    private fun distance(location1: Location, location2: Location): Double {
        val lat1 = location1.latitude
        val lon1 = location1.longitude
        val lat2 = location2.latitude
        val lon2 = location2.longitude
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

}
