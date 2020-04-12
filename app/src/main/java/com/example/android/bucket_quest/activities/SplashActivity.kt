package com.example.android.bucket_quest.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android.bucket_quest.R
import java.lang.NullPointerException

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try{
            this.supportActionBar?.hide()

        }
        catch (e: NullPointerException){

        }
        setContentView(R.layout.activity_splash_screen)
    }
}
