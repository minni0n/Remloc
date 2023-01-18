package com.remlocteam.remloc1

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.remlocteam.remloc1.HomeFragments.ActionsFragment
import com.remlocteam.remloc1.HomeFragments.GameMiejskaFragment
import com.remlocteam.remloc1.HomeFragments.HelpReviewFragment
import com.remlocteam.remloc1.HomeFragments.SettingsFragment
import com.remlocteam.remloc1.backgroundLocationTrack.LocationTrackingService
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var locale: Locale
    private var currentLanguage = "en"
    private var currentLang: String? = null
    private val contactsList: MutableList<String> = ArrayList()


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Utils().checkAllPermissions(this)


        val sp: SharedPreferences = getSharedPreferences("Language", MODE_PRIVATE)
        val lang = sp.getString("My_Lang", "pl")

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
                R.id.nav_logout -> {
//                    Intent(this, LocationService::class.java).apply {
//                        action = LocationService.ACTION_STOP
//                        this@HomeActivity.startService(this)
//                    }
                    stopLocationService()
                    logoutFromGoogle()
                }
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


    fun logoutFromGoogle() {
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
        return sp.getInt("TriggerDistanceValue", 10)
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

    fun getCurrentLanguage(): String? {
        val sp: SharedPreferences = getSharedPreferences("Language", MODE_PRIVATE)
        return sp.getString("My_Lang", "en")
    }

    fun setPlaceNumber(number: Int){
        val edit: SharedPreferences.Editor
        val sp: SharedPreferences = getSharedPreferences("PlaceNumber", MODE_PRIVATE)
        edit = sp.edit()
        edit.putInt("number", number)
        edit.apply()
    }

    fun getPlaceNumber(): Int {
        val prefs = getSharedPreferences("PlaceNumber", MODE_PRIVATE)
        return prefs.getInt("number", 1)
    }


    fun setStartTimer(timer: Long, placeName: String){
        val edit: SharedPreferences.Editor
        val sp: SharedPreferences = getSharedPreferences("StartTimer$placeName", MODE_PRIVATE)
        edit = sp.edit()
        edit.putLong("timer", timer)
        edit.apply()
    }

    fun getStartTimer(placeName: String): Long {
        val prefs = getSharedPreferences("StartTimer$placeName", MODE_PRIVATE)
        return prefs.getLong("timer", 0.toLong())
    }


    fun setCity(city: String){
        val edit: SharedPreferences.Editor
        val sp: SharedPreferences = getSharedPreferences("selectedCity", MODE_PRIVATE)
        edit = sp.edit()
        edit.putString("city", city)
        edit.apply()
    }

    fun getCity(): String? {
        val prefs = getSharedPreferences("selectedCity", MODE_PRIVATE)
        return prefs.getString("city", null)
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

    fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.remlocteam.remloc1.backgroundLocationTrack.LocationTrackingService" == service.service.className) {
                return true
            }
        }
        return false
    }

    fun stopLocationService(){
        val intent = Intent(this, LocationTrackingService::class.java)
        stopService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startLocationService(){
        val intentService = Intent(this, LocationTrackingService::class.java)
        startForegroundService(intentService)
    }
}
