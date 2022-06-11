package com.example.remloc1.HomeFragments

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.view.SupportActionModeWrapper
import androidx.core.app.ActivityCompat.recreate
import com.example.remloc1.HomeActivity
import com.example.remloc1.MainActivity
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentPlacesBinding
import com.example.remloc1.databinding.FragmentSettingsBinding
import java.lang.StringBuilder
import java.util.*


class SettingsFragment : Fragment() {

    private lateinit var binding : FragmentSettingsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(layoutInflater)



        val languageList = ArrayList<String>()
        languageList.add("Select")
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

        return binding.root
    }

}