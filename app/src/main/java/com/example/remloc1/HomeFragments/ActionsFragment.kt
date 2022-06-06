package com.example.remloc1.HomeFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.remloc1.AddDataFragment.AddActionFragment
import com.example.remloc1.EditDataFragments.EditActionFragment
import com.example.remloc1.HomeActivity
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment



        binding = FragmentActionsBinding.inflate(layoutInflater)

        val listView: ListView = binding.listOfActions
        actions = mutableListOf("")
        actions.clear()
        keys = mutableListOf("")
        keys.clear()
        listView.invalidateViews()

        readData()

        val arrayAdapter: ArrayAdapter<String>? = activity?.let {
            ArrayAdapter(
                it, android.R.layout.simple_list_item_1, actions
            )
        }

        listView.adapter = arrayAdapter

        binding.listOfActions.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
//            Toast.makeText(activity, keys[i], Toast.LENGTH_SHORT).show()
            (activity as HomeActivity?)!!.replaceFragment(EditActionFragment(keys[i]), "Edit Action")
        }

        binding.addAction.setOnClickListener {

            (activity as HomeActivity?)!!.replaceFragment(AddActionFragment(), "Add Action")

        }

        return binding.root
    }


    private fun readData(){
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {

            database = FirebaseDatabase.getInstance("https://remloc1-86738-default-rtdb.europe-west1.firebasedatabase.app").getReference(uid)
            database.child("Actions").get().addOnSuccessListener {
                if(it.exists()){

                    it.children.forEach{ action ->

                        val id = action.key
                        if (id != null) {
                            keys.add(id)
                        }
//                        Toast.makeText(activity, id, Toast.LENGTH_SHORT).show()

                        val phoneNumber = action.child("phoneNumber").value
                        val smsText = action.child("smsText").value
                        val placeName = action.child("placeName").value

                        actions.add(smsText.toString()+"\n"+placeName.toString())
                        binding.listOfActions.invalidateViews()

                    }

                }


            }.addOnFailureListener{

                Toast.makeText(activity, "Failed",Toast.LENGTH_SHORT).show()

            }
        }

    }

}
