package com.remlocteam.remloc1.PinDropGame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.remlocteam.remloc1.R

class PinDropGameActivity : AppCompatActivity() {

    private lateinit var gameView: PinDropGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = PinDropGameView(this)
        setContentView(gameView)

        title = getString(R.string.pin_drop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        gameView.resumeGame()
    }

    override fun onPause() {
        gameView.pauseGame()
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
