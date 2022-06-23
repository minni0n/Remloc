package com.example.remloc1.EditDataFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.remloc1.HomeActivity
import com.example.remloc1.HomeFragments.ActionsFragment
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentEditActionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditActionFragment(private val key: String , private val actionType: String) : Fragment() {

    private lateinit var binding : FragmentEditActionBinding
    private lateinit var database : DatabaseReference
    private lateinit var deleteBtn: Button
    private lateinit var saveChangesBtn: Button
    private lateinit var editAction: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var smsTextEdit: EditText
    private lateinit var actionTypeTV: TextView
    private lateinit var placeName: TextView
    private lateinit var contactName: TextView
    private lateinit var smsText: TextView
    private lateinit var phoneNumber: TextView
    private lateinit var placesSpinner: Spinner
    private lateinit var contactsSpinner: Spinner
    private lateinit var places: MutableList<String>
    private lateinit var contacts: MutableList<String>
    private lateinit var unnecessaryLayout: LinearLayout
    private lateinit var contactNamePhone: LinearLayout

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditActionBinding.inflate(layoutInflater)



        // initialization of data
        smsTextEdit = binding.smsTextEdit
        placesSpinner = binding.placesSpinner
        contactsSpinner = binding.contactsSpinner

        unnecessaryLayout = binding.unnecessaryLayout
        contactNamePhone = binding.contactNamePhone

        actionTypeTV = binding.actionTypeTV
        placeName = binding.placeName
        smsText = binding.smsText
        phoneNumber = binding.phoneNumber
        deleteBtn = binding.btnDeleteAction
        saveChangesBtn = binding.btnSavePlaceChanges
        contactName = binding.contactName
        editAction = binding.btnEditData

        // unable of edit data
        smsTextEdit.visibility = View.GONE
        placesSpinner.visibility = View.GONE
        contactsSpinner.visibility = View.GONE
        saveChangesBtn.visibility = View.GONE
        deleteBtn.visibility = View.GONE


        //Spinners auth
        places = mutableListOf(getString(R.string.choose_place))
        contacts = mutableListOf("")
        contacts = (activity as HomeActivity?)!!.readContacts()

        readPlaceData()

        //Adapters creation and loading
        val adapterPlaces: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_item, places
            )
        }

        val adapterContacts: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter<String>(
                it,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, contacts
            )
        }

        placesSpinner.adapter = adapterPlaces
        contactsSpinner.adapter = adapterContacts


        editAction.setOnClickListener {

            editAction.visibility = View.GONE
            smsTextEdit.visibility = View.VISIBLE
            placesSpinner.visibility = View.GONE
            contactsSpinner.visibility = View.VISIBLE
            saveChangesBtn.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE

        }

        //

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        database = uid?.let {
            FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(
                it
            )
        }!!

        when(actionType){
            "Sms" -> {
                database.child("Actions//Sms//$key").get().addOnSuccessListener {
                    if(it.exists()){

                        val placeNameRes = it.child("placeName").value.toString()
                        val phoneNumberRes = it.child("phoneNumber").value.toString()
                        val contactNameRes = it.child("contactName").value.toString()
                        val smsTextRes = it.child("smsText").value.toString()

                        actionTypeTV.text = getString(R.string.sms)
                        placeName.text = placeNameRes
                        phoneNumber.text = phoneNumberRes
                        smsText.text = smsTextRes
                        contactName.text = contactNameRes

                    }
                }

                deleteBtn.setOnClickListener{

                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//Sms//$key").removeValue()

                    (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                }
            }
            "Mute" -> {
                unnecessaryLayout.visibility = View.GONE
                editAction.visibility = View.GONE
                deleteBtn.visibility = View.VISIBLE
                database.child("Actions//Mute//$key").get().addOnSuccessListener {
                    if(it.exists()){

                        val placeNameRes = it.child("placeName").value.toString()
                        actionTypeTV.text = getString(R.string.mute_the_sound)
                        placeName.text = placeNameRes

                    }
                }

                deleteBtn.setOnClickListener{

                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//Mute//$key").removeValue()

                    (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                }
            }
            "Notification" -> {
                contactNamePhone.visibility = View.GONE
                editAction.visibility = View.GONE
                deleteBtn.visibility = View.VISIBLE
                database.child("Actions//Notification//$key").get().addOnSuccessListener {
                    if(it.exists()){

                        val placeNameRes = it.child("placeName").value.toString()
                        val smsTextRes = it.child("smsText").value.toString()
                        actionTypeTV.text = getString(R.string.notification)
                        placeName.text = placeNameRes
                        smsText.text = smsTextRes

                    }
                }

                deleteBtn.setOnClickListener{

                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//Notification//$key").removeValue()

                    (activity as HomeActivity?)!!.replaceFragment(ActionsFragment(), getString(R.string.actions))

                }
            }
        }



        saveChangesBtn.setOnClickListener{

            val strPhoneNumberOld = binding.contactsSpinner.selectedItem.toString()
            val strSmsText = smsText.text.toString()
            val strPlaceName: String = binding.placesSpinner.selectedItem.toString()

            if (strSmsText!="" || strPlaceName != getString(R.string.choose_place) || strPhoneNumberOld!= getString(R.string.choose_contact)){

                if (placesSpinner.selectedItem.toString()!=getString(R.string.choose_place)){
                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//Sms//$key//placeName").setValue(placesSpinner.selectedItem.toString())
                }

                if (contactsSpinner.selectedItem.toString()!=getString(R.string.choose_contact)){

                    ///
                    val index = strPhoneNumberOld.indexOf(": ") + 2
                    val len = strPhoneNumberOld.length
                    val strPhoneNumber = strPhoneNumberOld.subSequence(index, len).toString()
                    val contactName = strPhoneNumberOld.subSequence(0, index-2).toString()
                    ///

                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//Sms//$key//phoneNumber").setValue(strPhoneNumber)
                    database.child("Actions//Sms//$key//contactName").setValue(contactName)
                }

                if (smsTextEdit.text.toString()!=""){
                    database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
                    database.child("Actions//Sms//$key//smsText").setValue(smsTextEdit.text.toString())
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


                        val placeName = placeInfo.child("placeName").value

                        places.add(placeName.toString())

                    }

                }

            }.addOnFailureListener{

                Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show()

            }
        }
    }

}