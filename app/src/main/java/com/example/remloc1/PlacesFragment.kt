package com.example.remloc1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.remloc1.databinding.FragmentPlacesBinding

class PlacesFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val bind = FragmentPlacesBinding.inflate(layoutInflater)

        bind.openMapBtn.setOnClickListener{
            val intent = Intent(this@PlacesFragment.requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }

        return bind.root
    }

}