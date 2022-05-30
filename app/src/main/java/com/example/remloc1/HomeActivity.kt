package com.example.remloc1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

//        textView = findViewById(R.id.user_name)
//        textView.text = "Random"

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //replaceFragment(PlacesFragment(), "Miejsca")

        val intent = Intent(this, PlacesActivity::class.java)

        navView.setNavigationItemSelectedListener {

            it.isChecked = true

            when(it.itemId){

                R.id.nav_places -> startActivity(intent)
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


    private fun logoutFromGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String){

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

    internal fun onOpenMap(){
        supportFragmentManager.beginTransaction()
            .replace(com.google.android.material.R.id.container, PlacesFragment())
    }
}