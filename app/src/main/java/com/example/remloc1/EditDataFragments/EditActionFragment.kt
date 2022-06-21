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

class EditActionFragment(private val key: String) : Fragment() {

    private lateinit var binding : FragmentEditActionBinding
    private lateinit var database : DatabaseReference
    private lateinit var deleteBtn: Button
    private lateinit var saveChangesBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var smsTextEdit: EditText
    private lateinit var placeName: TextView
    private lateinit var smsText: TextView
    private lateinit var phoneNumber: TextView
    private lateinit var placesSpinner: Spinner
    private lateinit var contactsSpinner: Spinner
    private lateinit var places: MutableList<String>
    private lateinit var contacts: MutableList<String>

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditActionBinding.inflate(layoutInflater)




        smsTextEdit = binding.smsTextEdit
        placeName = binding.placeName
        smsText = binding.smsText
        phoneNumber = binding.phoneNumber
        deleteBtn = binding.btnDeleteAction
        saveChangesBtn = binding.btnSavePlaceChanges

        places = mutableListOf(getString(R.string.choose_place))
        contacts = mutableListOf("")
        contacts = (activity as HomeActivity?)!!.readContacts()

        val adapter2: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, contacts
            )
        }

//        places.clear()
        placesSpinner = binding.placesSpinner
        contactsSpinner = binding.contactsSpinner

        readPlaceData()
        val adapter: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item, places
            )
        }


        placesSpinner.adapter = adapter
        contactsSpinner.adapter = adapter2

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
                val contactName = it.child("contactName").value.toString()
                val smsTextRes = it.child("smsText").value.toString()

                placeName.text = "${getString(R.string.place)}: $placeNameRes"
                phoneNumber.text = "$contactName: $phoneNumberRes"
                smsText.text = "${getString(R.string.sms_text)}: $smsTextRes"

            }

//                    Toast.makeText(activity, keys.toString(), Toast.LENGTH_SHORT).show()
        }

        deleteBtn.setOnClickListener{

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Actions//$key").removeValue()

            (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

        }

        saveChangesBtn.setOnClickListener{

            val strPhoneNumberOld = binding.contactsSpinner.selectedItem.toString()
            val strSmsText = smsText.text.toString()
            val strPlaceName: String = binding.placesSpinner.selectedItem.toString()

            if (strSmsText!="" || strPlaceName != getString(R.string.choose_place) || strPhoneNumberOld!= getString(R.string.choose_contact)){

                if (placesSpinner.selectedItem.toString()!=getString(R.string.choose_place)){
                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//$key//placeName").setValue(placesSpinner.selectedItem.toString())
                }

                if (contactsSpinner.selectedItem.toString()!=getString(R.string.choose_contact)){

                    ///
                    val index = strPhoneNumberOld.indexOf(": ") + 2
                    val len = strPhoneNumberOld.length
                    val strPhoneNumber = strPhoneNumberOld.subSequence(index, len).toString()
                    val contactName = strPhoneNumberOld.subSequence(0, index-2).toString()
                    ///

                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//$key//phoneNumber").setValue(strPhoneNumber)
                    database.child("Actions//$key//contactName").setValue(contactName)
                }

                if (smsTextEdit.text.toString()!=""){
                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//$key//smsText").setValue(smsTextEdit.text.toString())
                }

                (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))
            }

        }

        return binding.root
    }

    private fun readPlaceData(){
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