package com.example.antime.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.antime.MainActivity
import com.example.antime.R
import com.example.antime.databinding.ActivityLoginBinding
import com.example.antime.ui.register.RegisterActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var  auth : FirebaseAuth
    var isError: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.edtEmail.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isError =
                    TextUtils.isEmpty(s.toString()) || !Patterns.EMAIL_ADDRESS.matcher(s.toString())
                        .matches()
                if (isError) {
                    binding.edtEmail.error = getString(R.string.invalid_email)
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.edtPassword.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isError = s.toString().length<8
                if (isError){
                    binding.edtPassword.error = getString(R.string.invalid_password_length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.buttonLogin.setOnClickListener{
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    if (task.isSuccessful){
                        Log.d(TAG,"SignInWithEmail: Success")
                        val user = auth.currentUser
                        updateUI(user)
                    }else{
                        Log.w(TAG,"signInWithEmail: Failed",task.exception)
                        Toast.makeText(this,"Email & password didn't match",Toast.LENGTH_LONG).show()
                    }

                }
        }

        binding.buttonLoginGoogle.setOnClickListener{
            signInGoogle()
        }

        binding.textRegister.setOnClickListener(){
            val intent= Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInGoogle() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.your_web_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result :GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context =  this@LoginActivity
                )
                handleSignInResult(result)
            }catch (e:GetCredentialException){
                Log.d("Error",e.errorMessage.toString())
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential){
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){task->
                if (task.isSuccessful){
                    Log.d(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    val dataUser = hashMapOf(
                        "username" to user?.displayName,
                        "email" to user?.email,
                        "id" to user?.uid
                    )
                    insertNewUserGoogle(dataUser)
                    updateUI(user)
                }
                else{
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun insertNewUserGoogle(dataUser: HashMap<String, String?>) {
        //storing user data to firestore database
        val db = Firebase.firestore
//        db.collection("users").document(dataUser["id"].toString()).get()
//            .addOnSuccessListener { documentSnapshot->
//                if (documentSnapshot.exists()){
//                    Toast.makeText(this,"udah ada bang usernya kongrets",Toast.LENGTH_LONG).show()
//                }else{
//                    db.collection("users")
//                        .document(dataUser["id"].toString())
//                        .set(dataUser)
//                        .addOnSuccessListener {  documentReference ->
//                            Log.d(TAG, "Successfully added data to Firestore document")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.w(TAG, "Error adding data to Firestore document", e)
//                        }
//                }
//            }

        db.collection("users")
            .document(dataUser["id"].toString())
            .set(dataUser)
            .addOnSuccessListener {  documentReference ->
                Log.d(TAG, "Successfully added data to Firestore document")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding data to Firestore document", e)
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        //check if user is logged in and updates the user interface
        if (currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        //Check if a user is logged in when starting the application and update the UI accordingly
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}