package com.example.remloc1

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat

class GPSUtils() {
    private var locationManager: LocationManager? = null
    private var latitude: String? = null
    private var longitude: String? = null

    fun initPermissions(activity: Activity?) {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    fun findDeviceLocation(activity: Activity) {
        locationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //Check gps is enable or not
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Write Function To enable gps
            gpsEnable(activity)
        } else {
            //GPS is already On then
            getLocation(activity)
        }
    }

    private fun getLocation(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        } else {
            val LocationGps: Location? =
                locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val LocationNetwork: Location? =
                locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val LocationPassive: Location? =
                locationManager!!.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (LocationGps != null) {
                val lat: Double = LocationGps.latitude
                val longi: Double = LocationGps.longitude
                latitude = lat.toString()
                longitude = longi.toString()
            } else if (LocationNetwork != null) {
                val lat: Double = LocationNetwork.latitude
                val longi: Double = LocationNetwork.longitude
                latitude = lat.toString()
                longitude = longi.toString()
            } else if (LocationPassive != null) {
                val lat: Double = LocationPassive.latitude
                val longi: Double = LocationPassive.longitude
                latitude = lat.toString()
                longitude = longi.toString()
            } else {
                Toast.makeText(activity, "Can't Get Your Location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gpsEnable(activity: Activity) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES",
            DialogInterface.OnClickListener { _, _ ->
                activity.startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }).setNegativeButton("NO",
            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    fun getLatitude(): String? {
        return latitude
    }

    fun setLatitude(latitude: String?) {
        this.latitude = latitude
    }

    fun getLongitude(): String? {
        return longitude
    }

    fun setLongitude(longitude: String?) {
        this.longitude = longitude
    }

    companion object {
        private const val REQUEST_LOCATION = 1
        val instance = GPSUtils()
    }
}