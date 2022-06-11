package com.example.remloc1.HomeFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remloc1.Data.PlacesData
import com.example.remloc1.DataAdapter.PlaceAdapter
import com.example.remloc1.EditDataFragments.EditPlaceFragment
import com.example.remloc1.HomeActivity
import com.example.remloc1.MapActivity
import com.example.remloc1.MapsActivity
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentPlacesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PlacesFragment : Fragment() {

    private lateinit var binding : FragmentPlacesBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var placeNameArray: MutableList<String>
    private lateinit var addressLineArray: MutableList<String>
    private lateinit var longitudeArray: MutableList<Double>
    private lateinit var latitudeArray: MutableList<Double>
    private lateinit var keys: MutableList<String>
    private lateinit var data: ArrayList<PlacesData>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentPlacesBinding.inflate(layoutInflater)

        val listOfPlaces: ListView = binding.listOfPlaces

        data = arrayListOf(PlacesData("addressLineArray", "placeNameArray", 0.0, 0.0))
        placeNameArray = mutableListOf("")
        addressLineArray = mutableListOf("")
        longitudeArray = mutableListOf(0.0)
        latitudeArray = mutableListOf(0.0)
        keys = mutableListOf("")
        placeNameArray.clear()
        addressLineArray.clear()
        longitudeArray.clear()
        latitudeArray.clear()
        keys.clear()


        data.add(PlacesData("addressLineArray", "placeNameArray", 0.0, 0.0))
        readData()
//        Toast.makeText(activity, placeNameArray.indices.toString(), Toast.LENGTH_SHORT).show()

        for (i in placeNameArray.indices){

            val place = PlacesData(addressLineArray[i], placeNameArray[i], longitudeArray[i], latitudeArray[i])


            data.add(place)
        }

        Toast.makeText(activity, data.indices.toString(), Toast.LENGTH_LONG).show()

        val arrayAdapter: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter(
                it, android.R.layout.simple_list_item_1, placeNameArray
            )
        }

        listOfPlaces.adapter = arrayAdapter

        ///




//        listOfPlaces.adapter = activity?.let { PlaceAdapter(it, data!!) }

        binding.listOfPlaces.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
//            Toast.makeText(activity,  places[i], Toast.LENGTH_SHORT).show()
            (activity as HomeActivity?)!!.replaceFragment(EditPlaceFragment(keys[i]), getString(R.string.edit_place))
        }

        binding.addPlaceBtn.setOnClickListener{
            val intent = Intent(activity, MapActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }


    private fun readData(){
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
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

//                        val place = PlacesData(addressLine, placeName, longitude as Double?, latitude as Double?)
//
//                        data.add(place)

                    }
                }
            }.addOnFailureListener{

                Toast.makeText(activity, "Failed",Toast.LENGTH_SHORT).show()
            }
        }

    }
}