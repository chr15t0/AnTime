package com.example.antime.ui.todoList

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.antime.R
import com.example.antime.algorithm.ToDoListData
import com.example.antime.databinding.ActivityToDoListBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.UUID

class ToDoListActivity : AppCompatActivity() {
    private lateinit var binding : ActivityToDoListBinding
    private lateinit var dialog : Dialog
    private lateinit var auth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private val db : FirebaseFirestore = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityToDoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        user = auth.currentUser!!

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        getToDoListFromFireStore(user)
        binding.fabAdd.setOnClickListener{
            showCreateToDoListDialog()
        }
    }

    private fun showCreateToDoListDialog() {
        dialog.setContentView(R.layout.form_to_do_list)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val edtNewToDoList : TextInputEditText = dialog.findViewById(R.id.edt_newToDoList)

        val buttonCreate : Button = dialog.findViewById(R.id.button_createNewToDoList)
        buttonCreate.setOnClickListener{
            val newToDoList = edtNewToDoList.text.toString()
            addNewToDoListToFireStore(newToDoList,user)
            dialog.dismiss()
        }
    }

    private fun addNewToDoListToFireStore(newToDoList: String,user:FirebaseUser) {
        val dataToDoList = hashMapOf(
            "id" to UUID.randomUUID().toString(),
            "to-do list" to newToDoList,
            "is done" to false.toString()
        )
        db.collection("users").document(user.uid).collection("task").document(dataToDoList["id"].toString())
            .set(dataToDoList)
            .addOnSuccessListener {
                getToDoListFromFireStore(user)
                Toast.makeText(this,"Successfully added new to-do list",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{
                Toast.makeText(this,"Failed to add new to-do list",Toast.LENGTH_LONG).show()
            }
    }

    private fun getToDoListFromFireStore(user: FirebaseUser) {
        db.collection("users").document(user.uid).collection("task").get()
            .addOnSuccessListener {documents->
//                get task where ""is done" is false
                if (documents!=null){
                    val ToDoList= mutableListOf<ToDoListData>()
                    for (document in documents.documents){
                        if (document.getString("is done") == "false"){
                           ToDoList.add(
                               ToDoListData(
                                   id = document.getString("id").toString(),
                                   ToDo = document.getString("to-do list").toString(),
                                   isDone = document.getString("is done").toString()
                               )
                           )
                        }
                    }
                    setAdapterRecyclerView(ToDoList)
                }
            }
    }

    private fun setAdapterRecyclerView(toDoList: MutableList<ToDoListData>) {
        val adapter = ToDoListAdapter()
        adapter.submitList(toDoList)
        binding.rvToDoList.adapter = adapter
    }
}