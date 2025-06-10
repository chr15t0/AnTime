package com.example.antime.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antime.MainActivity
import com.example.antime.R
import com.example.antime.databinding.ActivityRegisterBinding
import com.example.antime.ui.login.LoginActivity
import com.example.antime.ui.login.LoginActivity.Companion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    lateinit var binding : ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonRegister.setOnClickListener{
            val username = binding.edtUsername.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtConfirmPassword.text.toString()
            if (username.isEmpty()||email.isEmpty()||password.isEmpty()||confirmPassword.isEmpty()){
                Toast.makeText(this,"Text field shouldn't be empty!",Toast.LENGTH_LONG).show()
            }else if (password.length < 8) { // <-- ADDED THIS CHECK
                Toast.makeText(this,"Password should be at least 8 characters long!",Toast.LENGTH_LONG).show()
            }else{
                if (password!=confirmPassword){
                    Toast.makeText(this,"Make sure you input the same password!",Toast.LENGTH_LONG).show()
                }
                else{
                    registerNewUser(username,email,password)
                }

            }
        }

        binding.textLogin.setOnClickListener(){
            var intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun registerNewUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    var user: FirebaseUser? = auth.currentUser
                    val dataUser = hashMapOf(
                        "email" to user?.email,
                        "username" to username,
                        "id" to user?.uid
                    )
                    insertNewUserToFirestore(dataUser)
                }
                else{
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this,"Failed to register new user",Toast.LENGTH_LONG).show()
                }
            }
    }


private fun insertNewUserToFirestore(dataUser: HashMap<String, String?>) {
    val db = Firebase.firestore
    db.collection("users")
        .document(dataUser["id"].toString())
        .set(dataUser)
        .addOnSuccessListener {
            Log.d(TAG, "Successfully added data to Firestore document")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding data to Firestore document", e)
            Toast.makeText(this, "Registration failed", Toast.LENGTH_LONG).show()
        }
}

    companion object {
        private const val TAG = "RegisterActivity"
    }
}