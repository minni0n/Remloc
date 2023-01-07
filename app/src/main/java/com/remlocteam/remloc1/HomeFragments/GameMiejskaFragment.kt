package com.remlocteam.remloc1.HomeFragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.remlocteam.remloc1.CityGameFragments.CityGamePlaceFragment
import com.remlocteam.remloc1.EditDataFragments.EditActionFragment
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentGameMiejskaBinding
import kotlinx.android.synthetic.main.fragment_game_miejska.*


class GameMiejskaFragment : Fragment() {

    private lateinit var binding : FragmentGameMiejskaBinding
    private var gamePlaceNumber: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGameMiejskaBinding.inflate(layoutInflater)

        gamePlaceNumber = (activity as HomeActivity?)!!.getPlaceNumber()

//        if (gamePlaceNumber!=1){
//            binding.startGameBtn.text = "Continue"
//        }else{
//            binding.startGameBtn.text = "Start"
//        }

        if (gamePlaceNumber!=1){
            (activity as HomeActivity?)!!.replaceFragment(CityGamePlaceFragment(),getString(R.string.game_miejska))
        }

        binding.startGameBtn.setOnClickListener {
            (activity as HomeActivity?)!!.replaceFragment(CityGamePlaceFragment(),getString(R.string.game_miejska))
        }

        return binding.root
    }

}