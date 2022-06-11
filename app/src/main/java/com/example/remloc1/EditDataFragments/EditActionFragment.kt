package com.example.remloc1.EditDataFragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.remloc1.AddDataFragment.AddActionFragment
import com.example.remloc1.HomeActivity
import com.example.remloc1.HomeFragments.ActionsFragment
import com.example.remloc1.HomeFragments.PlacesFragment
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentEditActionBinding
import com.example.remloc1.databinding.FragmentEditPlaceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditActionFragment(val key: String) : Fragment() {

    private lateinit var binding : FragmentEditActionBinding
    private lateinit var database : DatabaseReference
    private lateinit var deleteBtn: Button
    private lateinit var saveChangesBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var smsTextEdit: EditText
    private lateinit var phoneNumberEdit: EditText
    private lateinit var placeName: TextView
    private lateinit var smsText: TextView
    private lateinit var phoneNumber: TextView
    private lateinit var placesSpinner: Spinner
    private lateinit var places: MutableList<String>

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditActionBinding.inflate(layoutInflater)




        smsTextEdit = binding.smsTextEdit
        phoneNumberEdit = binding.phoneNumberEdit
        placeName = binding.placeName
        smsText = binding.smsText
        phoneNumber = binding.phoneNumber
        deleteBtn = binding.btnDeleteAction
        saveChangesBtn = binding.btnSavePlaceChanges

        places = mutableListOf(getString(R.string.choose_place))
//        places.clear()
        placesSpinner = binding.placesSpinner
        readData()
        val adapter: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item, places
            )
        }
        placesSpinner.adapter = adapter


        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        database = uid?.let {
            FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(
                it
            )
        }!!
        database.child("Actions//$key").get().addOnSuccessListener {
            if(it.exists()){

                val placeNameRes = it.child("placeName").value.toString()
                val phoneNumberRes = it.child("phoneNumber").value.toString()
                val smsTextRes = it.child("smsText").value.toString()

                placeName.text = placeNameRes
                phoneNumber.text = "Phone Number: $phoneNumberRes"
                smsText.text = smsTextRes

            }

//                    Toast.makeText(activity, keys.toString(), Toast.LENGTH_SHORT).show()
        }

        deleteBtn.setOnClickListener{

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Actions//$key").removeValue()

            (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

        }

        saveChangesBtn.setOnClickListener{

            if (placesSpinner.selectedItem.toString()!=""){
                database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                database.child("Actions//$key//placeName").setValue(placesSpinner.selectedItem.toString())
            }

            if (phoneNumberEdit.text.toString()!=""){
                database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                database.child("Actions//$key//phoneNumber").setValue(phoneNumberEdit.text.toString())
            }

            if (smsTextEdit.text.toString()!=""){
                database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                database.child("Actions//$key//smsText").setValue(smsTextEdit.text.toString())
            }


            (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

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

                        val placeName = placeInfo.child("placeName").value
//                        val longitude = placeInfo.child("longitude").value
//                        val latitude = placeInfo.child("latitude").value

                        places.add(placeName.toString())

                    }

                }

            }.addOnFailureListener{

                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()

            }
        }
    }

}