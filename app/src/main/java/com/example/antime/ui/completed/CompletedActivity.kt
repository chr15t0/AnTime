package com.example.antime.ui.completed

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antime.R
import com.example.antime.algorithm.ToDoListData
import com.example.antime.databinding.ActivityCompletedBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class CompletedActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCompletedBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private val db : FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding  = ActivityCompletedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        user = auth.currentUser!!

        getCompletedList(user,db)
    }

    private fun getCompletedList(user: FirebaseUser, db: FirebaseFirestore) {
        db.collection("users").document(user.uid).collection("task").get()
            .addOnSuccessListener {documents->
//                get task where ""is done" is false
                if (documents!=null){
                    val CompletedList= mutableListOf<ToDoListData>()
                    for (document in documents.documents){
                        if (document.getString("is done") == "true"){
                            CompletedList.add(
                                ToDoListData(
                                    id = document.getString("id").toString(),
                                    ToDo = document.getString("to-do list").toString(),
                                    isDone = document.getString("is done").toString()
                                )
                            )
                        }
                    }
                    setAdapterRecyclerView(CompletedList)
                }
            }
    }

    private fun setAdapterRecyclerView(completedList: MutableList<ToDoListData>) {
        val adapter = CompletedListAdapter()
        adapter.submitList(completedList)
        binding.rvCompletedLlist.adapter = adapter
    }
}