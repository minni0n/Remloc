package com.remlocteam.remloc1.HomeFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentHelpReviewBinding
import com.remlocteam.remloc1.databinding.FragmentSettingsBinding


class HelpReviewFragment : Fragment() {

    private lateinit var binding : FragmentHelpReviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentHelpReviewBinding.inflate(layoutInflater)

        var theme = String()
        var email = "remlocteam@gmail.com"

        val languageList = ArrayList<String>()
        languageList.add(getString(R.string.theme))
        languageList.add(getString(R.string.help))
        languageList.add(getString(R.string.feedback))

        val adapter = activity?.let { ArrayAdapter(it, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, languageList) }
        binding.themeSpinner.adapter = adapter

        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                when(p2){
                    0-> {
                        theme = ""
                        email = "remlocteam@gmail.com"
                    }
                    1-> {
                        theme = "Help"
                        email = "remlocteam+support@gmail.com"

                    }
                    2-> {
                        theme = "Feedback"
                        email = "remlocteam+feedback@gmail.com"
                    }

                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.sendEmail.setOnClickListener {

            val themeSelected = binding.themeSpinner.selectedItem!="Select"
            val etMessegeBoxNotEmpty = binding.etMessageBox.text.isNotEmpty()

            if (themeSelected && etMessegeBoxNotEmpty) {

                val intent = Intent(Intent.ACTION_SEND)
                val message = binding.etMessageBox.text

                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                intent.putExtra(Intent.EXTRA_SUBJECT, theme)
                intent.putExtra(Intent.EXTRA_TEXT, message)

                intent.type = "message/rfc822"
                startActivity(Intent.createChooser(intent, "Select email"))
            }
            else{
                if (!themeSelected){
                    Toast.makeText(activity, "Please choose the theme!", Toast.LENGTH_SHORT).show()
                }
                if(!etMessegeBoxNotEmpty){
                    Toast.makeText(activity, "Please describe a problem or a feedback!", Toast.LENGTH_SHORT).show()
                }

            }
        }

        return binding.root
    }

}