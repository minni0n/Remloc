package com.remlocteam.remloc1.AddDataFragment


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.remlocteam.remloc1.Data.ActionMuteData
import com.remlocteam.remloc1.Data.ActionNotificationData
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.HomeFragments.ActionsFragment
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentAddActionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.remlocteam.remloc1.foregroundLocationCheck.LocationService


class AddActionFragment : Fragment() {

    private lateinit var binding : FragmentAddActionBinding
    private lateinit var database : DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var smsText: EditText
    private lateinit var placeName: Spinner
    private lateinit var contactInfo: Spinner
    private lateinit var buttonSave: Button
    private lateinit var spinnerPlaces: Spinner
    private lateinit var spinnerContacts: Spinner
    private var typeOfAction: Int = 0
    private lateinit var places: MutableList<String>
    private lateinit var longitudes: MutableList<Double>
    private lateinit var latitudes: MutableList<Double>
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
        longitudes = mutableListOf()
        latitudes = mutableListOf()
        contacts = mutableListOf("")
//        places.clear()


        spinnerPlaces = binding.placesSpinner
        spinnerContacts = binding.contactsSpinner

        readData()
        contacts = (activity as HomeActivity?)!!.readContacts()
//        contacts = mutableListOf((activity as HomeActivity?)!!.readContacts())

        val adapter1: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, places
            )
        }

        val adapter2: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, contacts
            )
        }


        val actionsList = ArrayList<String>()
        actionsList.add(getString(R.string.select_type_of_action))
        actionsList.add(getString(R.string.sms))
        actionsList.add(getString(R.string.mute_the_sound))
        actionsList.add(getString(R.string.notification))

        val adapter = activity?.let { ArrayAdapter(it, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, actionsList) }
        binding.typeOfActionSpinner.adapter = adapter

        binding.typeOfActionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                when(p2){

                    0->{
                        typeOfAction = p2
                        binding.smsText.setText("")
                        binding.contactsSpinner.setSelection(0)
                        binding.smsText.visibility = View.GONE
                        binding.contactsSpinner.visibility = View.GONE
                    }
                    1-> {
                        typeOfAction = p2
                        binding.contactsSpinner.isEnabled = true
                        binding.smsText.isEnabled = true
                        binding.smsText.visibility = View.VISIBLE
                        binding.contactsSpinner.visibility = View.VISIBLE
                    }
                    2-> {
                        typeOfAction = p2
                        binding.smsText.setText("")
                        binding.contactsSpinner.setSelection(0)
                        binding.smsText.visibility = View.GONE
                        binding.contactsSpinner.visibility = View.GONE
                    }
                    3-> {
                        typeOfAction = p2
                        binding.smsText.setText("")
                        binding.contactsSpinner.setSelection(0)
                        binding.smsText.visibility = View.VISIBLE
                        binding.contactsSpinner.visibility = View.GONE
                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }


        spinnerPlaces.adapter = adapter1
        spinnerContacts.adapter = adapter2


        buttonSave = binding.btnSaveAction
        auth = FirebaseAuth.getInstance()





        buttonSave.setOnClickListener{

//            Intent(context, LocationService::class.java).apply {
//                action = LocationService.ACTION_RESTART
//                context?.startService(this)
//            }

            saveData()

        }

        return binding.root
    }

    private fun saveData(){

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        when(typeOfAction){

            1-> {
                smsText = binding.smsText
                placeName = binding.placesSpinner
                contactInfo = binding.contactsSpinner

                val placeIndex = placeName.selectedItemId.toInt()-1

                val strPhoneNumberOld = binding.contactsSpinner.selectedItem.toString()
                val strSmsText = smsText.text.toString()
                val strPlaceName: String = binding.placesSpinner.selectedItem.toString()

                if (strSmsText!="" && strPlaceName != getString(R.string.choose_place) && strPhoneNumberOld!= getString(R.string.choose_contact)){

                    ///
                    val index = strPhoneNumberOld.indexOf(": ") + 2
                    val len = strPhoneNumberOld.length
                    val strPhoneNumber = strPhoneNumberOld.subSequence(index, len).toString()
                    val contactName = strPhoneNumberOld.subSequence(0, index-2).toString()
                    ///

                    if (uid!= null){

                        database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)
                        val key: String? = database.push().key
                        val action = ActionsData(contactName, strPhoneNumber, strSmsText, strPlaceName, "Sms", latitudes[placeIndex], longitudes[placeIndex], true)

                        database.child("Actions//Sms//$key").setValue(action).addOnCompleteListener{
                            if(it.isSuccessful){
                                Toast.makeText(activity, getString(R.string.success), Toast.LENGTH_SHORT).show()
                                (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                            }else{

                                Toast.makeText(activity, getString(R.string.failed_to_upd_data), Toast.LENGTH_SHORT).show()

                            }
                        }
                    }
                }else{
                    Toast.makeText(activity, getString(R.string.set_parameters),Toast.LENGTH_SHORT).show()
                }

            }
            2-> {

                placeName = binding.placesSpinner

                val placeIndex = placeName.selectedItemId.toInt()-1
                val strPlaceName: String = binding.placesSpinner.selectedItem.toString()

                if (strPlaceName != getString(R.string.choose_place) ) {
                    if (uid != null) {

                        database =
                            FirebaseDatabase.getInstance(getString(R.string.firebase_database_url))
                                .getReference(uid)
                        val key: String? = database.push().key
                        val action = ActionMuteData(
                            strPlaceName,
                            "Mute",
                            latitudes[placeIndex],
                            longitudes[placeIndex],
                            true
                        )

                        database.child("Actions//Mute//$key").setValue(action)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        activity,
                                        getString(R.string.success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    (activity as HomeActivity?)!!.replaceFragment(
                                        ActionsFragment(),
                                        getString(R.string.actions)
                                    )

                                } else {

                                    Toast.makeText(
                                        activity,
                                        getString(R.string.failed_to_upd_data),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            }
                    }
                }else{
                    Toast.makeText(activity, getString(R.string.set_parameters),Toast.LENGTH_SHORT).show()
                }
            }
            3-> {

                placeName = binding.placesSpinner
                smsText = binding.smsText

                val strSmsText = smsText.text.toString()
                val placeIndex = placeName.selectedItemId.toInt()-1
                val strPlaceName: String = binding.placesSpinner.selectedItem.toString()

                if (strPlaceName != getString(R.string.choose_place) ) {
                    if (uid != null) {

                        database =
                            FirebaseDatabase.getInstance(getString(R.string.firebase_database_url))
                                .getReference(uid)
                        val key: String? = database.push().key
                        val action = ActionNotificationData(
                            strPlaceName,
                            strSmsText,
                            "Notification",
                            latitudes[placeIndex],
                            longitudes[placeIndex],
                            true
                        )

                        database.child("Actions//Notification//$key").setValue(action)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        activity,
                                        getString(R.string.success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    (activity as HomeActivity?)!!.replaceFragment(
                                        ActionsFragment(),
                                        getString(R.string.actions)
                                    )

                                } else {

                                    Toast.makeText(
                                        activity,
                                        getString(R.string.failed_to_upd_data),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            }
                    }
                }else{
                    Toast.makeText(activity, getString(R.string.set_parameters),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun readData(){
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {

            database = FirebaseDatabase.getInstance(getString(R.string.firebase_database_url)).getReference(uid)
            database.child("Places").get().addOnSuccessListener {
                if(it.exists()){

                    it.children.forEach{ placeInfo ->

                        val placeName = placeInfo.child("placeName").value
                        val longitude = placeInfo.child("longitude").value
                        val latitude = placeInfo.child("latitude").value

                        latitudes.add(latitude as Double)
                        longitudes.add(longitude as Double)
                        places.add(placeName.toString())

                    }

                }

            }.addOnFailureListener{

                Toast.makeText(activity, getString(R.string.failed),Toast.LENGTH_SHORT).show()

            }
        }

    }


}