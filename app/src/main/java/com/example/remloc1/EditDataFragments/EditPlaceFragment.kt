package com.example.remloc1.EditDataFragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.remloc1.HomeActivity
import com.example.remloc1.HomeFragments.PlacesFragment
import com.example.remloc1.databinding.FragmentEditPlaceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class EditPlaceFragment(val key: String) : Fragment() {

    private lateinit var binding : FragmentEditPlaceBinding
    private lateinit var database : DatabaseReference
    private lateinit var deleteBtn: Button
    private lateinit var saveChangesBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var placeName: EditText
    private lateinit var placeOriginalName: TextView
    private lateinit var placeCoordinates: TextView
    private lateinit var addressLineTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditPlaceBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        placeOriginalName = binding.placeOriginalName
        placeCoordinates = binding.placeCoordinates
        addressLineTextView = binding.addressLineTextView

        database = uid?.let {
            FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(
                it
            )
        }!!
        database.child("Places//$key").get().addOnSuccessListener {
            if(it.exists()){

                val addressLine = it.child("addressLine").value.toString()
                val placeName = it.child("placeName").value.toString()
                val longitude = it.child("longitude").value.toString()
                val latitude = it.child("latitude").value.toString()

                placeOriginalName.text = placeName
                placeCoordinates.text = "Coordinates: $longitude, $latitude"
                addressLineTextView.text = addressLine

            }

//                    Toast.makeText(activity, keys.toString(), Toast.LENGTH_SHORT).show()
        }

        deleteBtn = binding.btnDeletePlace
        saveChangesBtn = binding.btnSavePlaceChanges
        placeName = binding.placeNameEdit

        deleteBtn.setOnClickListener{

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Places//$key").removeValue()

            (activity as HomeActivity?)!!.replaceFragment(PlacesFragment(), "Places")

        }

        saveChangesBtn.setOnClickListener{

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Places//$key//placeName").setValue(placeName.text.toString())

            (activity as HomeActivity?)!!.replaceFragment(PlacesFragment(), "Places")

        }

        return binding.root
    }

}