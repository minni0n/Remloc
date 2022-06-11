package com.example.remloc1.AddDataFragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.remloc1.Data.ActionsData
import com.example.remloc1.HomeActivity
import com.example.remloc1.HomeFragments.ActionsFragment
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentAddActionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class AddActionFragment : Fragment() {

    private lateinit var binding : FragmentAddActionBinding
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: EditText
    private lateinit var smsText: EditText
    private lateinit var placeName: Spinner
    private lateinit var button: Button
    private lateinit var spinnerPlaces: Spinner
    private lateinit var places: MutableList<String>
    private lateinit var contacts: MutableList<String>

//    private val permissionRequest = 101

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout {


        // Inflate the layout for this fragment
        binding = FragmentAddActionBinding.inflate(layoutInflater)

        places = mutableListOf(getString(R.string.choose_place))
        contacts = mutableListOf(getString(R.string.choose_place))
//        places.clear()


        spinnerPlaces = binding.placesSpinner

        readData()
//        contacts = mutableListOf((activity as HomeActivity?)!!.readContacts())

        val adapter1: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item, places
            )
        }




        spinnerPlaces.adapter = adapter1

        button = binding.btnSaveAction
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

//        Toast.makeText(activity, places.toString(), Toast.LENGTH_SHORT).show()

        button.setOnClickListener{

            phoneNumber = binding.phoneNumber
            smsText = binding.smsText
            placeName = binding.placesSpinner

            val strPhoneNumber = phoneNumber.text.toString()
            val strSmsText = smsText.text.toString()
            //val strPlaceName = placeName.toString()
            val strPlaceName: String = binding.placesSpinner.selectedItem.toString()


            if (uid!= null){

                database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                val key: String? = database.push().key
                val action = ActionsData(strPhoneNumber, strSmsText, strPlaceName)

                database.child("Actions//$key").setValue(action).addOnCompleteListener{
                    if(it.isSuccessful){
                        Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                        (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                    }else{

                        Toast.makeText(activity, "Failed to update data", Toast.LENGTH_SHORT).show()

                    }
                }

            }

           // sendMessage()
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

                Toast.makeText(activity, "Failed",Toast.LENGTH_SHORT).show()

            }
        }

    }



}