package com.example.antime.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antime.R
import com.example.antime.databinding.ActivityDetailDailyBinding
import java.util.zip.Inflater

class DetailDailyActivity : AppCompatActivity() {
    private lateinit var  binding : ActivityDetailDailyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityDetailDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testText.text = intent.getStringExtra("Extra_text")
    }

    companion object{
        const val EXTRA_TEXT = "Extra_text"
    }
}