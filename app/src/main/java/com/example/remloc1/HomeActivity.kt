package com.example.remloc1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.remloc1.HomeFragments.ActionsFragment
import com.example.remloc1.HomeFragments.GameMiejskaFragment
import com.example.remloc1.HomeFragments.HelpReviewFragment
import com.example.remloc1.HomeFragments.SettingsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.system.exitProcess


class HomeActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var locale: Locale
    private var currentLanguage = "en"
    private var currentLang: String? = null
    private val contactsList: MutableList<String> = ArrayList()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sp: SharedPreferences = getSharedPreferences("Language", MODE_PRIVATE)
        val lang = sp.getString("My_Lang", "en")

        if (lang != null) {
            currentLang = lang
        }

        setLocale(currentLang!!)

        currentLanguage = intent.getStringExtra(currentLang).toString()
//        readContacts()

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        //setting Username and Email
        setUsernameEmail(navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        replaceFragment(ActionsFragment(), getString(R.string.actions))



        navView.setNavigationItemSelectedListener {

            it.isChecked = true

            val intent = Intent(this, MapActivity::class.java)


            when(it.itemId){
                R.id.nav_places ->startActivity(intent)
                R.id.nav_actions -> replaceFragment(ActionsFragment(), it.title.toString())
                R.id.nav_game_miejska -> replaceFragment(GameMiejskaFragment(), it.title.toString())
                R.id.nav_settings -> replaceFragment(SettingsFragment(), it.title.toString())
                R.id.nav_help_review -> replaceFragment(HelpReviewFragment(), it.title.toString())
                R.id.nav_logout -> logoutFromGoogle()

            }

            true


        }

        // call requestIdToken as follows
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1068238081596-n07tj5bf7lu02b3fb3tvuvofc1a9njuo.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    }


    private fun setUsernameEmail(navView: NavigationView){
        val headerView = navView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.user_name)
        val navEmail: TextView = headerView.findViewById(R.id.email)
        val navPhoto: CircleImageView = headerView.findViewById(R.id.photo)

        // Set user info by Firebase
        val user = Firebase.auth.currentUser

        user?.let {
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            navUsername.text = name
            navEmail.text = email

            Glide
                .with(this)
                .load(photoUrl) // the uri you got from Firebase // URI.parse(url)
                .into(navPhoto) //Your imageView variable


        }


    }


    private fun logoutFromGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, getString(R.string.log_out), Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }

     fun replaceFragment(fragment: Fragment, title: String){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun setLocale(localeName: String) {

        val currentLanguage = intent.getStringExtra(currentLang).toString()

        if (localeName != currentLanguage) {
            locale = Locale(localeName)
            val res = resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.locale = locale
            res.updateConfiguration(conf, dm)
            val refresh = Intent(
                this,
                HomeActivity::class.java
            )
            refresh.putExtra(currentLang, localeName)
            startActivity(refresh)

            val edit: SharedPreferences.Editor
            val sp: SharedPreferences = getSharedPreferences("Language", MODE_PRIVATE)
            edit = sp.edit()
            edit.putString("My_Lang", localeName)
            edit.apply()

        }
    }

    @Override
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
        exitProcess(0)
    }

    @SuppressLint("Range")
    fun readContacts(): MutableList<String> {
        val contacts = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        contactsList.add(getString(R.string.choose_contact))

//        var allContacts = ""
        while (contacts?.moveToNext()!!) {
            val name =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//            var objDTO = ContactDTO()
//            objDTO.name = name
//            objDTO.number = number
//            allContacts += "$name $number\n"

//            contactsList.add(objDTO)
            contactsList.add("$name: $number")

        }

        contacts.close()

        return contactsList
    }



}