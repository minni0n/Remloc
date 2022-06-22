package com.example.remloc1.HomeFragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.remloc1.HomeActivity
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


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
    ): View {

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

        if (binding.locationSwitch.isChecked){
            binding.locationSwitch.isEnabled = false
        }

        binding.contactsSwitch.isChecked = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (binding.contactsSwitch.isChecked){
            binding.contactsSwitch.isEnabled = false
        }

        binding.smsSwitch.isChecked = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (binding.smsSwitch.isChecked){
            binding.smsSwitch.isEnabled = false
        }

        binding.deleteAllFromDatabase.setOnClickListener {
            showChooseNameDialog()
        }

        binding.locationSwitch.setOnCheckedChangeListener { _, b ->

            if (b){
                checkLocationPermission()
                binding.locationSwitch.isEnabled = false
            }

        }

        binding.smsSwitch.setOnCheckedChangeListener { _, b ->

            if (b){
                checkSmsPermission()
                binding.smsSwitch.isEnabled = false
            }

        }

        binding.contactsSwitch.setOnCheckedChangeListener { _, b ->

            if (b){
                checkContactsPermission()
                binding.contactsSwitch.isEnabled = false
            }

        }



        return binding.root
    }


    private fun showChooseNameDialog(){

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.deleting))
        builder.setMessage(getString(R.string.you_sure_del_data))

        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(
                uid
            )
            database.removeValue().addOnSuccessListener {
                Toast.makeText(activity, getString(R.string.succ_deleted), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(activity, getString(R.string.failed_to_delete), Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(getString(R.string.no)) { _, _ -> }

        builder.show()

    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

    private fun checkSmsPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

    private fun checkContactsPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }
}