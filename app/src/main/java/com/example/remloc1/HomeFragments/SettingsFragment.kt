package com.example.remloc1.HomeFragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.view.SupportActionModeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.getColorStateList
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import com.example.remloc1.HomeActivity
import com.example.remloc1.MainActivity
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentPlacesBinding
import com.example.remloc1.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.StringBuilder
import java.util.*


class SettingsFragment : Fragment() {

    private lateinit var binding : FragmentSettingsBinding
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!

        val languageList = ArrayList<String>()
        languageList.add(getString(R.string.select))
        languageList.add("English")
        languageList.add("Polski")
        languageList.add("Українська")
        languageList.add("Русский")

        val adapter = activity?.let { ArrayAdapter(it, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, languageList) }
        binding.languagesSpinner.adapter = adapter

        binding.languagesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                when(p2){

                    0->{

                    }
                    1-> (activity as HomeActivity?)!!.setLocale("en")
                    2-> (activity as HomeActivity?)!!.setLocale("pl")
                    3-> (activity as HomeActivity?)!!.setLocale("uk")
                    4-> (activity as HomeActivity?)!!.setLocale("ru")
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.locationSwitch.isChecked = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        binding.contactsSwitch.isChecked = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        binding.smsSwitch.isChecked = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED



        binding.deleteAllFromDatabase.setOnClickListener {
            showChooseNameDialog()
        }

        binding.locationSwitch.setOnCheckedChangeListener { compoundButton, b ->

            if (b){
                checkLocationPermition()
            }
        }
//
//        binding.contactsSwitch.setOnCheckedChangeListener { compoundButton, b ->
//
//
//
//        }
//
//
//        binding.smsSwitch.setOnCheckedChangeListener { compoundButton, b ->
//
//
//
//        }



        return binding.root
    }


    private fun showChooseNameDialog(){

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Deleting")
        builder.setMessage("You sure you want to delete all of the data?")

        builder.setPositiveButton("Yes") { dialog, which ->
            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid!!)
            database.removeValue().addOnSuccessListener {
                Toast.makeText(activity, "Successfully deleted!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed to delete!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("No") { dialog, which ->

        }

        builder.show()

    }

    private fun checkLocationPermition() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

}