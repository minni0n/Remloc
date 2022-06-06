package com.example.remloc1

import android.R
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
import com.example.remloc1.databinding.FragmentPlacesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PlacesFragment : Fragment() {

    private lateinit var binding : FragmentPlacesBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var places: MutableList<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentPlacesBinding.inflate(layoutInflater)

        val listView: ListView = binding.listOfPlaces
        places = mutableListOf("Poznan")
        places.clear()
        listView.invalidateViews()
        readData()

        val arrayAdapter: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter(
                it, R.layout.simple_list_item_1, places
            )
        }

        listView.adapter = arrayAdapter

        binding.listOfPlaces.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            Toast.makeText(activity,  places[i], Toast.LENGTH_SHORT).show()
        }

        binding.addPlaceBtn.setOnClickListener{
            val intent = Intent(activity, MapsActivity::class.java)
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
                        Toast.makeText(activity, id, Toast.LENGTH_SHORT).show()

                        val placeName = placeInfo.child("placeName").value
                        val longitude = placeInfo.child("longitude").value
                        val latitude = placeInfo.child("latitude").value

                        places.add(placeName.toString() + "\n"+ longitude.toString()+" | "+latitude.toString())
                        binding.listOfPlaces.invalidateViews()

                    }

                }

            }.addOnFailureListener{

                Toast.makeText(activity, "Failed",Toast.LENGTH_SHORT).show()

            }
        }

    }
}