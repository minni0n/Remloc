package com.remlocteam.remloc1.CityGameFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.Data.GameMiejskaScenario
import com.remlocteam.remloc1.Data.PlacesData
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.HomeFragments.GameMiejskaFragment
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentActionsBinding
import com.remlocteam.remloc1.databinding.FragmentCityGamePlaceBinding

class CityGamePlaceFragment : Fragment() {

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

//                gamePlaceNumber = (activity as HomeActivity?)!!.getPlaceNumber()

                auth = FirebaseAuth.getInstance()
                database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("Games/Poznan")
                database.get().addOnSuccessListener { it ->
                     childCount = it.childrenCount.toInt()
                }

                setData()


                binding.nextPlace.setOnClickListener {

                    if (gamePlaceNumber<childCount){
                        gamePlaceNumber += 1
//                        (activity as HomeActivity?)!!.setPlaceNumber(gamePlaceNumber)
                        setData()
                    } else{
//                        (activity as HomeActivity?)!!.setPlaceNumber(0)
                        (activity as HomeActivity?)!!.replaceFragment(GameMiejskaFragment(),getString(R.string.game_miejska))
                    }
                }

                return binding.root
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