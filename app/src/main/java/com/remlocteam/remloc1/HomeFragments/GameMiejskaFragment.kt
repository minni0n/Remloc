package com.remlocteam.remloc1.HomeFragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.CityGameFragments.CityGamePlaceFragment
import com.remlocteam.remloc1.EditDataFragments.EditActionFragment
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentGameMiejskaBinding
import kotlinx.android.synthetic.main.fragment_game_miejska.*


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

        gamePlaceNumber = (activity as HomeActivity?)!!.getPlaceNumber()

        // SpinnerSetUp

        val cityList = ArrayList<String>()
        cityList.add(getString(R.string.select_city))

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("Games")


//        val uid = auth.currentUser?.uid
//        databaseScore = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference("GameMiejskaScores/$uid").get().addOnSuccessListener { cities ->
//            if (cities.exists()){
//                cities.children.forEach{ place ->
//                    Log.d("cities",place.toString())
//                }
//            }
//        }




        database.get().addOnSuccessListener { cities ->

            cities.children.forEach{ placeInfo ->

                val id = placeInfo.key
                if (id != null) {
                    cityList.add(id)
                }
            }

        }

        val adapter = activity?.let { ArrayAdapter(it, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, cityList) }
        binding.cityGamesSpinner.adapter = adapter


        if (gamePlaceNumber!=1){
            (activity as HomeActivity?)!!.replaceFragment(CityGamePlaceFragment(),getString(R.string.game_miejska))
        }

        binding.startGameBtn.setOnClickListener {

            val selectedItem = binding.cityGamesSpinner.selectedItem.toString()

            if (selectedItem != getString(R.string.select_city)){
                (activity as HomeActivity?)!!.setCity(selectedItem)
                (activity as HomeActivity?)!!.replaceFragment(CityGamePlaceFragment(),getString(R.string.game_miejska))
            }else{
                Toast.makeText(activity, "Select a city!", Toast.LENGTH_LONG).show()
            }


        }

        return binding.root
    }

}