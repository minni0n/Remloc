package com.remlocteam.remloc1.AddDataFragment


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.Data.ActionMuteData
import com.remlocteam.remloc1.Data.ActionNotificationData
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.Data.PlacesData
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.HomeFragments.ActionsFragment
import com.remlocteam.remloc1.MapsActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.Utils
import com.remlocteam.remloc1.databinding.FragmentAddActionBinding


class AddActionFragment : Fragment() {

    private lateinit var binding : FragmentAddActionBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    private lateinit var longitudes: MutableList<Double>
    private lateinit var latitudes: MutableList<Double>

    private lateinit var placeSpinner: Spinner
    private lateinit var typeOfActionSpinner: Spinner
    private lateinit var contactsSpinner: Spinner
    private lateinit var smsTextEt: EditText
    private lateinit var saveActionBtn: Button

    @SuppressLint("ServiceCast")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ScrollView {
        binding = FragmentAddActionBinding.inflate(layoutInflater)

        Utils().checkAllPermissions(requireContext())

        // Init and firebase setup
        init()
        firebaseAuth()

        // Set spinners values
        setPlacesSpinner()
        setTypeOfActionSpinner()
        setContactsSpinner()

        // Set layout
        setNeededLayout()
        addPlace()

        // Save button
        saveButtonListener()

//        binding.smsTextEt.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                val paint = Paint()
//                paint.textSize = smsTextEt.textSize // use the same text size as the EditText
//                val textWidth = paint.measureText(s.toString()) // measure the width of the text
//
//                var counter = 1
//
//                if (textWidth > counter*smsTextEt.width) {
//                    // if the text width is greater than the EditText width, double the height
//                    counter += 1
//                    smsTextEt.layoutParams.height = smsTextEt.height * 2
//                }
////                else {
////                    // if the text width is smaller than the EditText width, halve the height
////                    smsTextEt.layoutParams.height = smsTextEt.height / 2
////                }
//                smsTextEt.requestLayout() // update the layout
//            }
//
//            // other methods are not used in this example
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })

        return binding.root
    }


    private fun firebaseAuth(){
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)
    }

    private fun init() {
        placeSpinner = binding.placesSpinner
        typeOfActionSpinner = binding.typeOfActionSpinner
        contactsSpinner = binding.contactsSpinner
        smsTextEt = binding.smsTextEt
        saveActionBtn = binding.btnSaveAction
    }

    private fun setPlacesSpinner(){

        longitudes = mutableListOf()
        latitudes = mutableListOf()
        val defaultItems = mutableListOf("Wybierz miejsce", " + Dodaj miejsce")
        val placeSpinnerAdapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, defaultItems)
        placeSpinner.adapter = placeSpinnerAdapter

        database.child("Places").get().addOnSuccessListener { places ->

            places.children.forEach {   placeInfo ->

                val placeName = placeInfo.child("placeName").value.toString()
                val longitude = placeInfo.child("longitude").value
                val latitude = placeInfo.child("latitude").value

                latitudes.add(latitude as Double)
                longitudes.add(longitude as Double)

                defaultItems.add(placeName)
            }
            placeSpinnerAdapter.notifyDataSetChanged()

        }.addOnFailureListener{
            Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setTypeOfActionSpinner(){
        val defaultItems = mutableListOf("Wybierz rodzaj akcji","Sms", "Powiadomienie", "Wycisz dźwięk")
        val typeOfActionSpinnerAdapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, defaultItems)
        typeOfActionSpinner.adapter = typeOfActionSpinnerAdapter
    }

    private fun setContactsSpinner(){

        val contacts = (activity as HomeActivity?)!!.readContacts()
        val contactsSpinnerAdapter = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, contacts)
        contactsSpinner.adapter = contactsSpinnerAdapter
    }

    private fun setNeededLayout() {
        typeOfActionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // do something with the selected item
                when(position){
                    (0)->{
                        binding.smsTextLayout.visibility = View.GONE
                        binding.contactsLayout.visibility = View.GONE
                    }
                    (1)->{
                        binding.smsTextLayout.visibility = View.VISIBLE
                        binding.contactsLayout.visibility = View.VISIBLE
                    }
                    (2)->{
                        binding.smsTextLayout.visibility = View.VISIBLE
                        binding.contactsLayout.visibility = View.GONE
                    }
                    (3)->{
                        binding.smsTextLayout.visibility = View.GONE
                        binding.contactsLayout.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // do something when nothing is selected
            }
        }
    }


    private fun addPlace(){
        placeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                when(position){

                    (1)->{
                        val intent = Intent(requireActivity(), MapsActivity::class.java)
                        startActivity(intent)
                    }

                }
                // do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // do something when nothing is selected
            }
        }
    }

    private fun saveButtonListener() {
        saveActionBtn.setOnClickListener {

            val selectedPlaceIndex = placeSpinner.selectedItemPosition

            if (selectedPlaceIndex != 0 && selectedPlaceIndex != 1){
                when(typeOfActionSpinner.selectedItemPosition){

                    (0) -> {
                        Toast.makeText(activity, "Prosze wybrać rodzaj akcji!", Toast.LENGTH_SHORT).show()
                    }

                    (1) -> {
                        val smsText = smsTextEt.text.toString()
                        val placeName = placeSpinner.selectedItem.toString()
                        val contactData = contactsSpinner.selectedItem.toString()
                        val contactName = contactData.substringBefore(": ")
                        val contactPhoneNumber = contactData.substringAfter(": ")

                        if (smsText!=""){
                            if (contactsSpinner.selectedItemPosition != 0){
                                val action = ActionsData(contactName, contactPhoneNumber, smsText, placeName, "Sms", latitudes[selectedPlaceIndex-2], longitudes[selectedPlaceIndex-2], true)
                                val key: String? = database.push().key
                                database.child("Actions//Sms//$key").setValue(action).addOnCompleteListener{
                                    if(it.isSuccessful){
                                        Toast.makeText(activity, getString(R.string.success), Toast.LENGTH_SHORT).show()
                                        (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                                    }else{
                                        Toast.makeText(activity, getString(R.string.failed_to_upd_data), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{
                                Toast.makeText(activity, "Prosze wybrać kontakt!", Toast.LENGTH_SHORT).show()
                            }

                        }else{
                            Toast.makeText(activity, "Prosze podać text wiadomości!", Toast.LENGTH_SHORT).show()
                        }

                    }

                    (2) -> {
                        val smsText = smsTextEt.text.toString()
                        val placeName = placeSpinner.selectedItem.toString()
                        if (smsText!=""){
                            val action = ActionsData(null,null,smsText,placeName,"Notification",latitudes[selectedPlaceIndex-2],longitudes[selectedPlaceIndex-2],true)
                            val key: String? = database.push().key
                            database.child("Actions//Notification//$key").setValue(action).addOnCompleteListener{
                                if(it.isSuccessful){
                                    Toast.makeText(activity, getString(R.string.success), Toast.LENGTH_SHORT).show()
                                    (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                                }else{
                                    Toast.makeText(activity, getString(R.string.failed_to_upd_data), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }else{
                            Toast.makeText(activity, "Prosze podać text wiadomości!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    (3) -> {
                        val placeName = placeSpinner.selectedItem.toString()
                        val action = ActionsData(null,null,null,placeName,"Mute",latitudes[selectedPlaceIndex-2],longitudes[selectedPlaceIndex-2],true)
                        val key: String? = database.push().key
                        database.child("Actions//Mute//$key").setValue(action).addOnCompleteListener{
                            if(it.isSuccessful){
                                Toast.makeText(activity, getString(R.string.success), Toast.LENGTH_SHORT).show()
                                (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                            }else{
                                Toast.makeText(activity, getString(R.string.failed_to_upd_data), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(activity, "Prosze wybrać miejsce!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}