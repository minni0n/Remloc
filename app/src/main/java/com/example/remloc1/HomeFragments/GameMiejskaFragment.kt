package com.example.remloc1.HomeFragments

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
import com.example.remloc1.HomeActivity
import com.example.remloc1.R
import com.example.remloc1.databinding.FragmentGameMiejskaBinding
import kotlinx.android.synthetic.main.fragment_game_miejska.*


class GameMiejskaFragment : Fragment() {

    private lateinit var binding : FragmentGameMiejskaBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGameMiejskaBinding.inflate(layoutInflater)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webViewSetup()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSetup(){
        val myWebView: WebView = requireView().findViewById(R.id.cityGameWeb)
        myWebView.webViewClient = WebViewClient()
        myWebView.apply {
            loadUrl("https://google.com")
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true
        }
    }

//    override fun onBackPressed(): Boolean {
//        return if (binding.cityGameWeb.canGoBack()) {
//            binding.cityGameWeb.goBack()
//            true
//        } else {
//            false
//        }
//    }

}