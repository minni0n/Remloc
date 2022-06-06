package com.example.remloc1

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.remloc1.HomeFragments.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView


class HomeActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        //setting Username and Email
        setUsernameEmail(navView)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        replaceFragment(ActionsFragment(), "Akcje")

//        val intent = Intent(this, PlacesActivity::class.java)



        navView.setNavigationItemSelectedListener {

            it.isChecked = true

            when(it.itemId){

                R.id.nav_places -> replaceFragment(PlacesFragment(), it.title.toString())
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

            //From shared pref
//            val sp: SharedPreferences = getSharedPreferences("enter", MODE_PRIVATE)
//            val url = sp.getString("imgUrl", null)
            Glide
                .with(this)
                .load(photoUrl) // the uri you got from Firebase // URI.parse(url)
                .into(navPhoto) //Your imageView variable


        }


    }


    private fun logoutFromGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show()
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
}