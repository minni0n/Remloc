package com.example.remloc1.HomeFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remloc1.AddDataFragment.AddActionFragment
import com.example.remloc1.Data.ActionsData
import com.example.remloc1.Data.PlacesData
import com.example.remloc1.DataAdapter.ActionAdapter
import com.example.remloc1.DataAdapter.PlaceAdapter
import com.example.remloc1.EditDataFragments.EditActionFragment
import com.example.remloc1.HomeActivity
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentActionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ActionsFragment : Fragment() {

    private lateinit var binding : FragmentActionsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var actions: MutableList<String>
    private lateinit var keys: MutableList<String>
    private lateinit var actionTypeArray: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment



        binding = FragmentActionsBinding.inflate(layoutInflater)

        val listOfActions: ListView = binding.listOfActions
        var data: ArrayList<ActionsData> = ArrayList()

        actionTypeArray = mutableListOf("")
        actionTypeArray.clear()
        actions = mutableListOf("")
        actions.clear()
        keys = mutableListOf("")
        keys.clear()

        data = readData()

        listOfActions.adapter = activity?.let { ActionAdapter(it, data) }


        binding.listOfActions.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->

            (activity as HomeActivity?)!!.replaceFragment(EditActionFragment(keys[i], actionTypeArray[i]), getString(R.string.edit_action))
        }

        binding.addAction.setOnClickListener {

            (activity as HomeActivity?)!!.replaceFragment(AddActionFragment(), getString(R.string.add_action))

        }

        return binding.root
    }


    private fun readData(): ArrayList<ActionsData>{
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        val dataNow: ArrayList<ActionsData> = ArrayList()

        if (uid != null) {

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Actions").get().addOnSuccessListener {
                if(it.exists()){

                    it.children.forEach { actionTypes ->

                        actionTypes.children.forEach { action ->

                            val id = action.key
                            if (id != null) {
                                keys.add(id)
                            }
                            //                        Toast.makeText(activity, id, Toast.LENGTH_SHORT).show()

                            val contactName = action.child("contactName").value.toString()
                            val phoneNumber = action.child("phoneNumber").value.toString()
                            val smsText = action.child("smsText").value.toString()
                            val placeName = action.child("placeName").value.toString()
                            val actionType = action.child("actionType").value.toString()
                            val latitude = action.child("latitude").value as Double
                            val longitude = action.child("longitude").value as Double

                            actions.add(smsText + "\n" + placeName)
                            binding.listOfActions.invalidateViews()

                            actionTypeArray.add(actionType)

                            when(actionType){
                                "Sms" -> {
                                    val action1 = ActionsData(
                                        contactName,
                                        phoneNumber,
                                        smsText,
                                        placeName,
                                        getString(R.string.sms),
                                        latitude,
                                        longitude
                                    )

                                    dataNow.add(action1)
                                }
                                "Mute" -> {
                                    val action1 = ActionsData(
                                        contactName,
                                        phoneNumber,
                                        smsText,
                                        placeName,
                                        getString(R.string.mute_the_sound),
                                        latitude,
                                        longitude
                                    )

                                    dataNow.add(action1)
                                }
                                "Notification" -> {
                                    val action1 = ActionsData(
                                        contactName,
                                        phoneNumber,
                                        smsText,
                                        placeName,
                                        getString(R.string.notification),
                                        latitude,
                                        longitude
                                    )

                                    dataNow.add(action1)
                                }

                            }
                        }
                    }
                }


            }.addOnFailureListener{

                Toast.makeText(activity, "Failed",Toast.LENGTH_SHORT).show()

            }
        }

        return dataNow

    }

}
