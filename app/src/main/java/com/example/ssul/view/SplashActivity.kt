package com.example.ssul.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ssul.MainActivity
import com.example.ssul.R
import com.example.ssul.StartActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val gifImage = findViewById<ImageView>(R.id.gif_image)
        Glide.with(this).load(R.raw.splash_gif).into(gifImage)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        Handler().postDelayed({
            val isDataSaved = sharedPreferences.contains("selectedCollege") && sharedPreferences.contains("selectedDepartment")

            val nextActivity = if (isDataSaved) {
                MainActivity::class.java // 값이 저장되어 있으면 MainActivity로 이동
            } else {
                StartActivity::class.java // 값이 없으면 StartActivity로 이동
            }

            // Intent 생성 및 Activity 이동
            val intent = Intent(this, nextActivity)
            startActivity(intent)
            finish()
        }, 2400)
    }
}