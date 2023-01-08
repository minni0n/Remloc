package com.remlocteam.remloc1.CityGameFragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.HomeFragments.GameMiejskaFragment
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentCityGamePlaceBinding

class CityGamePlaceFragment : Fragment() {




    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var binding :FragmentCityGamePlaceBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var gamePlaceNumber: Int = 1
    private var childCount: Int = 1

            @SuppressLint("SetTextI18n")
            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
                binding = FragmentCityGamePlaceBinding.inflate(layoutInflater)
                gamePlaceNumber = (activity as HomeActivity?)!!.getPlaceNumber()

                // Firebase and dataset
                val selectedCity = (activity as HomeActivity?)!!.getCity()
                binding.city.text = selectedCity

                auth = FirebaseAuth.getInstance()
                database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("Games/$selectedCity")

                database.get().addOnSuccessListener { it ->
                     childCount = it.childrenCount.toInt()
                }
                setData()

                // Location tracking

                getLocationUpdates()

                // Buttons listener

                binding.nextPlace.setOnClickListener {

                    if (gamePlaceNumber<childCount){
                        refreshPlace()
                    } else{
                        nextPlace()
                    }
                }

                return binding.root
    }

    private fun checkPlaceSmth(){
        if (gamePlaceNumber<childCount){
            refreshPlace()
        } else{
            nextPlace()
        }
    }

    private fun refreshPlace(){
        gamePlaceNumber += 1
        (activity as HomeActivity?)!!.setPlaceNumber(gamePlaceNumber)
        setData()
    }

    private fun nextPlace(){
        (activity as HomeActivity?)!!.setPlaceNumber(1)
        (activity as HomeActivity?)!!.replaceFragment(GameMiejskaFragment(),getString(R.string.game_miejska))
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val latt = p0.lastLocation?.latitude
            val long = p0.lastLocation?.longitude

            val myLocation = Location("Location 1")
            myLocation.latitude = latt!!
            myLocation.longitude = long!!

            val placeLocation = Location("Location 2")
            placeLocation.latitude = latitude
            placeLocation.longitude = longitude

            val distance = distanceBetweenAB(myLocation, placeLocation)
            if (distance <= 50.0){
                longitude = 0.0
                latitude = 0.0
                checkPlaceSmth()
            }
            Log.d("distance", distance.toString())
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val locationRequest = LocationRequest().apply {
            interval = 5000 // 5 seconds
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun distanceBetweenAB(location1: Location, location2: Location): Double {
        val distance = location1.distanceTo(location2)

        return distance.toDouble()
    }

    private fun setData() {

        database.child(gamePlaceNumber.toString()).get().addOnSuccessListener { place ->

            binding.placeNumber.text = gamePlaceNumber.toString()
            binding.placeLegend.text = place.child("legend").value.toString()
            longitude = place.child("longitude").value as Double
            latitude = place.child("latitude").value as Double

        }.addOnFailureListener{

            Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show()
        }
    }
}