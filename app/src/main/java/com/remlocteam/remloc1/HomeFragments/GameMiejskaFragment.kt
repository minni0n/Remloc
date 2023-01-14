package com.remlocteam.remloc1.HomeFragments

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentGameMiejskaBinding
import com.remlocteam.remloc1.CityGameFragments.CityGamePlaceFragment
import com.remlocteam.remloc1.Data.PlacesData
import com.remlocteam.remloc1.Data.ScoreData
import com.remlocteam.remloc1.DataAdapter.PlaceAdapter
import com.remlocteam.remloc1.DataAdapter.ScoreAdapter
import com.remlocteam.remloc1.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameMiejskaFragment : Fragment() {

    private lateinit var binding : FragmentGameMiejskaBinding
    private var gamePlaceNumber: Int = 1
    private lateinit var database: DatabaseReference
    private lateinit var databaseScore: DatabaseReference
    private lateinit var auth: FirebaseAuth



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGameMiejskaBinding.inflate(layoutInflater)


        Utils().checkAllPermissions(requireContext())


        setData()

        binding.startGameBtn.setOnClickListener {

            val selectedItem = binding.cityGamesSpinner.selectedItem.toString()

            if (selectedItem != getString(R.string.select_city)){
                (activity as HomeActivity?)!!.setCity(selectedItem)
                (activity as HomeActivity?)!!.replaceFragment(CityGamePlaceFragment(),getString(R.string.game_miejska))
            }else{
                Toast.makeText(activity, "Wybierz miejce do grania!", Toast.LENGTH_LONG).show()
            }


        }

        return binding.root
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun setData(){
        gamePlaceNumber = (activity as HomeActivity?)!!.getPlaceNumber()

        if (gamePlaceNumber!=1){
            (activity as HomeActivity?)!!.replaceFragment(CityGamePlaceFragment(),getString(R.string.game_miejska))
        }
        // SpinnerSetUp

        val listOfScores: ListView = binding.listOfScores
        val cityList = ArrayList<String>()
        cityList.add(getString(R.string.select_city))

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("Games")

        binding.scoresLayout.visibility = View.GONE
        var data: ArrayList<ScoreData> = ArrayList()
        data = setScores()

        listOfScores.adapter = activity?.let { ScoreAdapter(it, data) }



        database.get().addOnSuccessListener { cities ->

            cities.children.forEach{ placeInfo ->

                val id = placeInfo.key
                if (id != null) {
                    cityList.add(id)
                }
            }

        }

        val adapterCity = activity?.let { ArrayAdapter(it, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, cityList) }
        binding.cityGamesSpinner.adapter = adapterCity

    }

    private fun setScores(): ArrayList<ScoreData>{
        val data: ArrayList<ScoreData> = ArrayList()
        val uid = auth.currentUser?.uid
        if (uid != null) {
            databaseScore = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("GameMiejskaScores")
            databaseScore.child(uid).get().addOnSuccessListener {
                if(it.exists()){
                    binding.scoresLayout.visibility = View.VISIBLE
                    it.children.forEach{ placeScoreInfo ->
                        val placeScore = placeScoreInfo.child("placeScore").value as Long?
                        val placeName = placeScoreInfo.child("placeName").value.toString()

                        val scoreInfo = ScoreData(placeName, placeScore?.toInt())

                        data.add(scoreInfo)
                    }
                }
            }.addOnFailureListener{

                Toast.makeText(activity, getString(R.string.failed),Toast.LENGTH_SHORT).show()
            }
        }

        return data
    }
}