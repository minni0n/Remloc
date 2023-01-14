package com.remlocteam.remloc1.HomeFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.remlocteam.remloc1.Data.PlacesData
import com.remlocteam.remloc1.DataAdapter.PlaceAdapter
import com.remlocteam.remloc1.EditDataFragments.EditPlaceFragment
import com.remlocteam.remloc1.MapActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentPlacesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.Utils

class PlacesFragment : Fragment() {

    private lateinit var binding : FragmentPlacesBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var placeNameArray: ArrayList<String>
    private lateinit var addressLineArray: ArrayList<String>
    private lateinit var longitudeArray: ArrayList<Double>
    private lateinit var latitudeArray: ArrayList<Double>
    private lateinit var keys: ArrayList<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentPlacesBinding.inflate(layoutInflater)

        Utils().checkAllPermissions(requireContext())

        val listOfPlaces: ListView = binding.listOfPlaces



        var data: ArrayList<PlacesData> = ArrayList()

        placeNameArray = ArrayList()
        addressLineArray = ArrayList()
        longitudeArray = ArrayList()
        latitudeArray = ArrayList()
        keys = ArrayList()


        data = readData()


        listOfPlaces.adapter = activity?.let { PlaceAdapter(it, data) }


        binding.listOfPlaces.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->

            (activity as MapActivity?)!!.replaceFragment(EditPlaceFragment(keys[i]), getString(R.string.edit_place))

        }

        return binding.root
    }

    private fun readData(): ArrayList<PlacesData> {
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val dataNow: ArrayList<PlacesData> = ArrayList()

        if (uid != null) {

            database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)
            database.child("Places").get().addOnSuccessListener {
                if(it.exists()){
                    it.children.forEach{ placeInfo ->

                        val id = placeInfo.key

                        if (id != null) {
                            keys.add(id)
                        }

                        val addressLine = placeInfo.child("addressLine").value.toString()
                        val placeName = placeInfo.child("placeName").value.toString()
                        val longitude = placeInfo.child("longitude").value
                        val latitude = placeInfo.child("latitude").value

                        placeNameArray.add(placeName)
                        addressLineArray.add(addressLine)
                        longitudeArray.add(longitude as Double)
                        latitudeArray.add(latitude as Double)
                        binding.listOfPlaces.invalidateViews()

                        val place = PlacesData(addressLine, placeName, longitude as Double?, latitude as Double?)

                        dataNow.add(place)

                    }
                }
            }.addOnFailureListener{

                Toast.makeText(activity, getString(R.string.failed),Toast.LENGTH_SHORT).show()
            }
        }

        return dataNow

    }
}