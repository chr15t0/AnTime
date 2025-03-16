package com.example.antime.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antime.MainActivity
import com.example.antime.R
import com.example.antime.databinding.ActivityRegisterBinding
import com.example.antime.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    lateinit var binding : ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.textLogin.setOnClickListener(){
            var intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

    }
}