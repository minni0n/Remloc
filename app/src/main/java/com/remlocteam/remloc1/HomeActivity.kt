package com.remlocteam.remloc1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.remlocteam.remloc1.HomeFragments.ActionsFragment
import com.remlocteam.remloc1.HomeFragments.GameMiejskaFragment
import com.remlocteam.remloc1.HomeFragments.HelpReviewFragment
import com.remlocteam.remloc1.HomeFragments.SettingsFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.remlocteam.remloc1.AddDataFragment.AddActionFragment
import com.remlocteam.remloc1.foregroundLocationCheck.LocationService
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*


class HomeActivity : AppCompatActivity() {

//    interface IOnBackPressed {
//        fun onBackPressed(): Boolean
//    }
//
//
//    override fun onBackPressed() {
//        val fragment =
//            this.supportFragmentManager.findFragmentById(R.id.cityGameWeb)
//        (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let {
//            super.onBackPressed()
//        }
//    }



    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

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

        currentLanguage = intent.getStringExtra("currentLang").toString()

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        //setting Username and Email
        setUsernameEmail(navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)



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

        replaceFragment(ActionsFragment(), getString(R.string.actions))




        // call requestIdToken as follows
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("518564489431-tcna2ahq4pir464qgjhuhgmcrcf7h25a.apps.googleusercontent.com")
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


    // Set and Get slider range values
    fun getSliderValue(): Int {
        val sp: SharedPreferences = getSharedPreferences("Distance", MODE_PRIVATE)
        val value = sp.getInt("TriggerDistanceValue", 10)
        return value
    }

    fun setSliderValue(value: Int){
        val edit: SharedPreferences.Editor
        val sp: SharedPreferences = getSharedPreferences("Distance", MODE_PRIVATE)
        edit = sp.edit()
        edit.putInt("TriggerDistanceValue", value)
        edit.apply()
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

    fun setPlaceNumber(number: Int){
        val edit: SharedPreferences.Editor
        val sp: SharedPreferences = getSharedPreferences("PlaceNumber", MODE_PRIVATE)
        edit = sp.edit()
        edit.putInt("number", number)
        edit.apply()
    }

    fun getPlaceNumber(): Int{
        val prefs = getSharedPreferences("PlaceNumber", MODE_PRIVATE)
        val lang = prefs.getString("number", null)
        return if (lang != null) {
            lang.toInt()
        } else{
            1
        }
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
        contactsList.clear()
        contactsList.add(getString(R.string.choose_contact))


        while (contacts?.moveToNext()!!) {
            val name =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            contactsList.add("$name: $number")
        }

        contacts.close()

        return contactsList
    }

}
