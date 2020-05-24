package com.example.android.bucket_quest.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.android.bucket_quest.R


class SplashActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_TIME_OUT = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // This method is used so that your splash activity
        // can cover the entire screen.
        setContentView(R.layout.activity_main)

        // This will bind your MainActivity.class file with activity_main.
        Handler().postDelayed({
            // Intent is used to switch from one activity to another.
            val i = Intent(
                this@SplashActivity,
                MainActivity::class.java
            )

            // Invoke the SecondActivity.
            startActivity(i)

            // The current activity will get finished.
            finish()
        }, SPLASH_SCREEN_TIME_OUT.toLong())
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()
    }
}
