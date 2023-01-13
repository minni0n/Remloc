package com.remlocteam.remloc1.backgroundLocationTrack


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LocationTrackingService : Service() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

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
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun send(location: Location) {

        Log.d("LocationTrackingService", "Current location: ${location.latitude}, ${location.longitude}")
    }

    override fun onBind(intent: Intent?): IBinder? = null

}
