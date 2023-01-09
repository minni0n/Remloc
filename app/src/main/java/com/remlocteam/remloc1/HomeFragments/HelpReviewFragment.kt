package com.remlocteam.remloc1.HomeFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.remlocteam.remloc1.HomeActivity
import com.remlocteam.remloc1.R
import com.remlocteam.remloc1.databinding.FragmentHelpReviewBinding
import com.remlocteam.remloc1.databinding.FragmentSettingsBinding


class HelpReviewFragment : Fragment() {

    private lateinit var binding : FragmentHelpReviewBinding
    private lateinit var language: String
    private lateinit var userManual: String

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentHelpReviewBinding.inflate(layoutInflater)

        language = (activity as HomeActivity?)!!.getCurrentLanguage().toString()

        val textView = binding.userManual
        when (language) {
            "en" -> {
                userManual = "<a href='https://drive.google.com/file/d/17L_LUsRck7BpkfAq4RnEtdSoB2Cwl6Ms/view?usp=sharing'>User manual</a>"
            }
            "uk" -> {
                userManual = "<a href='https://drive.google.com/file/d/1zUvI-U7nO0l1cl4L-uA2kFr7W6jzylwC/view?usp=sharing'>Інтрукція користувача</a>"
            }
            "pl" -> {
                userManual = "<a href='https://drive.google.com/file/d/1kZss6HGruY_FEYl-ylqlFULRM4EdZxrK/view?usp=sharing'>Instrukcja użytkownika</a>"
            }
            "ru" -> {
                userManual = "<a href='https://drive.google.com/file/d/12PhsGb_36DHAaEf7hLslagCFqNLg5uvk/view?usp=sharing'>Интсрукция пользователя</a>"
            }
        }

        textView.text = HtmlCompat.fromHtml(userManual, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = LinkMovementMethod.getInstance()

        var theme = String()
        var email = "remlocteam@gmail.com"

        val languageList = ArrayList<String>()
        languageList.add(getString(R.string.theme))
        languageList.add(getString(R.string.help))
        languageList.add(getString(R.string.feedback))
        languageList.add(getString(R.string.game_suggest))

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
                    3-> {
                        theme = "Suggest a game"
                        email = "remlocteam+game@gmail.com"
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