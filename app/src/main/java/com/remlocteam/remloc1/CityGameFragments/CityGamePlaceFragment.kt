package com.remlocteam.remloc1.CityGameFragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.Data.ScoreData
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
    private var placeName: String = ""
    private var gamePlaceNumber: Int = 1
    private var childCount: Int = 1
    private var distanceDefault: Double = 50.0
    private var score: Int = 0
    private var timer: Long = 0
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var imageURL: String = ""
    private lateinit var selectedCity: String
    private lateinit var imageHint: ImageView
    private lateinit var currentLanguage: String

            @SuppressLint("SetTextI18n")
            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
                binding = FragmentCityGamePlaceBinding.inflate(layoutInflater)
                imageHint = binding.imageHint

                currentLanguage = (activity as HomeActivity?)!!.getCurrentLanguage().toString()

                gamePlaceNumber = (activity as HomeActivity?)!!.getPlaceNumber()

                // Firebase and dataset
                selectedCity = (activity as HomeActivity?)!!.getCity().toString()
                binding.city.text = selectedCity

                auth = FirebaseAuth.getInstance()
                database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("Games/$selectedCity/$currentLanguage")

                database.get().addOnSuccessListener { it ->
                     childCount = it.childrenCount.toInt()
                }
                setData()

                // Location tracking
                getLocationUpdates()

                // Buttons listener
                binding.exitGame.setOnClickListener {
                    onStop()
                    backToMenu()
                }


                binding.showHintBtn.setOnClickListener {
                    score -= 5
                    imageHint.visibility = View.VISIBLE
                    binding.showHintBtn.visibility = View.GONE
                    setImageHint()
                }

                return binding.root
    }

    @SuppressLint("StaticFieldLeak")
    @Suppress("DEPRECATION")
    private inner class DownloadImageFromInternet(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        init {
            Toast.makeText(requireContext(), getString(R.string.please_wait),     Toast.LENGTH_SHORT).show()
        }
        override fun doInBackground(vararg urls: String): Bitmap? {

            var imageURL = urls[0]
            if (imageURL==""){
                imageURL = "https://media.licdn.com/dms/image/C5612AQEPYce5KpNLyg/article-cover_image-shrink_720_1280/0/1551659700811?e=1678924800&v=beta&t=SwwpRDk2nez4mC4oBDGXdf8AtJhmu7ljFDj4i7dKtTs"
            }
            var image: Bitmap? = null
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
            }
            catch (e: Exception) {
                Log.e("Error Message", e.message.toString())
                e.printStackTrace()
            }
            return image
        }
        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }

    private fun checkPlaceSmth(){
        if (gamePlaceNumber<childCount) {
            refreshPlace()
        }else{
            backToMenu()
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun refreshPlace(){
        gamePlaceNumber += 1
        (activity as HomeActivity?)!!.setPlaceNumber(gamePlaceNumber)
        setData()
    }

    private fun backToMenu(){
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
            if (distance <= distanceDefault){
                longitude = 0.0
                latitude = 0.0
                stopTimer()
                val time = ((endTime - startTime)/1000)
                timer+=time
                Log.d("timer", timer.toString())
                if (gamePlaceNumber==childCount){
                    showExitDialog(requireActivity())
                }else{
                    showGotToThePlaceDialog(requireActivity())
                }

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

    @SuppressLint("SetTextI18n")
    private fun showGotToThePlaceDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.place_alert_dialog, null)
        builder.setView(view)
        val placeNameView = view.findViewById<TextView>(R.id.placeName)
        val buttonClaim = view.findViewById<TextView>(R.id.buttonClaim)
        placeNameView.text =  "${placeNameView.text} $placeName!"

        builder.setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        buttonClaim.setOnClickListener {
            checkPlaceSmth()
            alertDialog.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showExitDialog(context: Context) {

        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.place_alert_dialog, null)
        builder.setView(view)
        val placeNameView = view.findViewById<TextView>(R.id.placeName)
        val buttonClaim = view.findViewById<TextView>(R.id.buttonClaim)
        val descriptionView = view.findViewById<TextView>(R.id.description)


        score += calculateScore(timer, 100, 1800.0 )

        val congrats = getString(R.string.congrats_score)


        placeNameView.text =  "${placeNameView.text} $placeName!"
        descriptionView.text = "$congrats $score!"
        buttonClaim.text = getString(R.string.exit_button)

        builder.setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        buttonClaim.setOnClickListener {
            checkPlaceSmth()
            saveScoreToFB()
            alertDialog.dismiss()
        }



    }


    private fun saveScoreToFB(){
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid!= null) {

            database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("GameMiejskaScores/$uid")
            val databaseChild = database.child(selectedCity)
            Log.d("databaseChild", databaseChild.toString())
            databaseChild.get().addOnSuccessListener {
                val  prevScore = it.child("placeScore").value as Long?

                val data = ScoreData(selectedCity, score)

                if (prevScore != null) {
                    if (prevScore.toInt() < score){
                        databaseChild.setValue(data)
                    }
                }else{
                    databaseChild.setValue(data)
                }
            }
        }
    }

    private fun setImageHint(){
        database.child(gamePlaceNumber.toString()).get().addOnSuccessListener { place ->
            imageURL = place.child("hint").value.toString()
            DownloadImageFromInternet(imageHint).execute(imageURL)
        }.addOnFailureListener{

            Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show()
        }

    }

    private fun setData() {

        imageHint.visibility = View.GONE
        binding.showHintBtn.visibility = View.VISIBLE

        database.child(gamePlaceNumber.toString()).get().addOnSuccessListener { place ->

            binding.placeNumber.text = gamePlaceNumber.toString()
            binding.placeLegend.text = place.child("legend").value.toString()
            longitude = place.child("longitude").value as Double
            latitude = place.child("latitude").value as Double
            placeName = place.child("placeName").value.toString()
            imageURL = place.child("hint").value.toString()

        }.addOnFailureListener{

            Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show()
        }

        startTimer()
    }

    private fun startTimer(){
        startTime = System.currentTimeMillis()
    }

    private fun stopTimer(){
        endTime = System.currentTimeMillis()
    }

    private fun calculateScore(time: Long, maxScore: Int, timeConstant: Double): Int {
        return (maxScore - (time / timeConstant)).toInt()
    }
}