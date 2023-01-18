package com.remlocteam.remloc1.backgroundLocationTrack


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.location.Location
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToLong


@Suppress("DEPRECATION")
class LocationTrackingService : Service() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private var placesToDo: MutableList<Pair<ActionsData, Boolean>> = mutableListOf()
    private var placesLocations: MutableList<Location> = mutableListOf()
    private lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var notificationId = 1

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


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Step 2: Create a new instance of the NotificationChannel
        val channel = NotificationChannel("location", "Location", NotificationManager.IMPORTANCE_HIGH)

        // Step 3: Optionally configure additional properties of the notification channel
        channel.description = "Notifications related to location"

        // Step 4: Register the notification channel with the notification manager
        notificationManager.createNotificationChannel(channel)

        database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid).child("Actions")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest().apply {
            interval = 7000
            fastestInterval = 7000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Step 5: In your onStartCommand method, when you are building the notification, you can set the channel ID for the notification
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle(getString(R.string.location_tracking__))
            .setContentText(getString(R.string.notifiaction_location))
            .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))


        // Step 6: Finally, you can call startForeground(1, notification.build()) to start the service in foreground
        startForeground(notificationId, notification.build())

        notificationId += 1

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

//        if (placesLocations.isNotEmpty()){
//            val nearest = nearestLocation(placesLocations, currentLocation)
//            val dist = distanceBetweenAB(nearest, currentLocation)
//
////            setNewLocationRequest(dist)
////            booleanDidSet = true
////            Log.d("LocationTrackingService", "$dist")
//        }

        Log.d("LocationTrackingService", "Current location: ${currentLocation.latitude}, ${currentLocation.longitude}")


        if (placesToDo.isNotEmpty()){
            // remove all elements from the list
            getActionsDone()
        }

        placesLocations.clear()
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

                        sendNotification(place.placeName, place.smsText)

                        placesToDo[index] = Pair(place, true)
                    }
                    ("Mute")->{

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        } else {
                            // code for devices running Android 10 and below
                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val minVolume = 0
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, minVolume, AudioManager.FLAG_SHOW_UI)
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, minVolume, AudioManager.FLAG_SHOW_UI)
//                            audioManager.setStreamMute(AudioManager.STREAM_RING, true)
//                            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
                        }




                        placesToDo[index] = Pair(place, true)
                    }
                }
            }
        }

    }





    private fun sendNotification(placeName: String?, smsText: String?) {
        val builder = NotificationCompat.Builder(this, "action")
            .setSmallIcon(R.drawable.ic_baseline_doorbell_24)
            .setContentTitle(placeName)
            .setContentText(smsText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

        createNotificationChannel()

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }

        notificationId += 1
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = "actions"
        val descriptionText = "Actions Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("action", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
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


//    @SuppressLint("MissingPermission")
//    private fun setNewLocationRequest(distance: Double){
//        var time = calculateTravelTime(distance)
//
//        if (time<7000){
//            time = 7000
//        }else if (time>20000){
//            time = 20000
//        }
//
////        time = 30000
//
//        Log.d("LocationTrackingService", time.toString())
//        val locationRequest = LocationRequest().apply {
//            interval = time
//            fastestInterval = time
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
//
//    }
//
//    private fun calculateTravelTime(distance: Double): Long {
//        val speed = 20.0
//        val timeInSeconds = (distance / speed )/2
//        return timeInSeconds.toLong() * 1000 // convert seconds to milliseconds
//    }

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


//    // The Haversine formula
//    private fun nearestLocation(locations: List<Location>, currentLocation: Location): Location {
//        var nearestLocation = locations[0]
//        var nearestDistance = distance(currentLocation, nearestLocation)
//        for (location in locations) {
//            val distance = distance(currentLocation, location)
//            if (distance < nearestDistance) {
//                nearestDistance = distance
//                nearestLocation = location
//            }    }
//        return nearestLocation
//    }
//
//    private fun distance(location1: Location, location2: Location): Double {
//        val lat1 = location1.latitude
//        val lon1 = location1.longitude
//        val lat2 = location2.latitude
//        val lon2 = location2.longitude
//        val earthRadius = 6371000.0 // meters
//        val dLat = Math.toRadians(lat2 - lat1)
//        val dLon = Math.toRadians(lon2 - lon1)
//        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//                Math.sin(dLon / 2) * Math.sin(dLon / 2)
//        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
//        return earthRadius * c
//    }

}