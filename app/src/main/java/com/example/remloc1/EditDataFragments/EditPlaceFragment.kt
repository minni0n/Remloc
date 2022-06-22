package com.example.remloc1.EditDataFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.remloc1.MapActivity
import com.example.remloc1.databinding.FragmentEditPlaceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class EditPlaceFragment(private val key: String) : Fragment() {

    private lateinit var binding : FragmentEditPlaceBinding
    private lateinit var database : DatabaseReference
    private lateinit var deleteBtn: Button
    private lateinit var saveChangesBtn: Button
    private lateinit var editPlace: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var placeName: EditText
    private lateinit var placeOriginalName: TextView
    private lateinit var placeLatitude: TextView
    private lateinit var placeLongitude: TextView
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

        // initialization
        placeName = binding.placeNameEdit
        deleteBtn = binding.btnDeletePlace
        saveChangesBtn = binding.btnSavePlaceChanges
        editPlace = binding.btnEditData

        placeOriginalName = binding.placeOriginalName
        placeLatitude = binding.placeLatitude
        placeLongitude = binding.placeLongitude
        addressLineTextView = binding.addressLineTextView

        // unable of edit data
        placeName.visibility = View.GONE
        deleteBtn.visibility = View.GONE
        saveChangesBtn.visibility = View.GONE

        editPlace.setOnClickListener {

            editPlace.visibility = View.GONE

            placeName.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
            saveChangesBtn.visibility = View.VISIBLE

        }


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
                placeLatitude.text = latitude
                placeLongitude.text = longitude
                addressLineTextView.text = addressLine

            }

        }



        deleteBtn.setOnClickListener{

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Places//$key").removeValue()

            val intent = Intent(requireActivity(), MapActivity::class.java)
            startActivity(intent)

        }

        saveChangesBtn.setOnClickListener{

            if (placeName.text.toString()!=""){

                database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                database.child("Places//$key//placeName").setValue(placeName.text.toString())

            }

            val intent = Intent(requireActivity(), MapActivity::class.java)
            startActivity(intent)

        }

        return binding.root
    }

}