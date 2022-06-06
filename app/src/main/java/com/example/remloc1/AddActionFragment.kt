package com.example.remloc1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remloc1.Data.ActionsData
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
    private lateinit var placeName: EditText
    private lateinit var button: Button

//    private val permissionRequest = 101

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout {
        // Inflate the layout for this fragment
        binding = FragmentAddActionBinding.inflate(layoutInflater)

        button = binding.btnSaveAction
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        button.setOnClickListener{

            phoneNumber = binding.phoneNumber
            smsText = binding.smsText
            placeName = binding.placeName

            val strPhoneNumber = phoneNumber.text.toString()
            val strSmsText = smsText.text.toString()
            val strPlaceName = placeName.text.toString()

            if (uid!= null){

                database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                val key: String? = database.push().key
                val action = ActionsData(strPhoneNumber, strSmsText, strPlaceName)

                database.child("Actions//$key").setValue(action).addOnCompleteListener{
                    if(it.isSuccessful){
                        Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                        (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), "Akcje")

                    }else{

                        Toast.makeText(activity, "Failed to update data", Toast.LENGTH_SHORT).show()

                    }
                }

            }

           // sendMessage()
        }

        return binding.root
    }





}