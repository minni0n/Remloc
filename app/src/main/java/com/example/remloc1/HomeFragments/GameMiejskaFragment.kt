package com.example.remloc1.HomeFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentActionsBinding
import com.example.remloc1.databinding.FragmentEditActionBinding
import com.example.remloc1.databinding.FragmentGameMiejskaBinding
import com.example.remloc1.databinding.FragmentSettingsBinding
import kotlinx.android.synthetic.main.fragment_game_miejska.*


class GameMiejskaFragment : Fragment() {

    private lateinit var binding : FragmentGameMiejskaBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGameMiejskaBinding.inflate(layoutInflater)

        return binding.root
    }
}