package com.example.antime.ui.todoList

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.antime.algorithm.ToDoListData
import com.example.antime.databinding.ListToDoListBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ToDoListAdapter :ListAdapter<ToDoListData,ToDoListAdapter.MyViewHolder>(DIFF_CALLBACK){
    private lateinit var auth : FirebaseAuth

    class MyViewHolder (val binding: ListToDoListBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ToDoListData){
            binding.textToDoList.text = data.ToDo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListToDoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val toDoList = getItem(position)
        holder.bind(toDoList)
        holder.binding.checkboxCompleted.setOnClickListener{
            updateToDoList(toDoList.id)
        }

    }

    private fun updateToDoList(id: String) {
        auth = Firebase.auth
        val user = auth.currentUser
        val db = Firebase.firestore
        db.collection("users").document(user?.uid.toString()).collection("task").document(id)
            .update("is done", "true")
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully updated to-do list")
                val currentList = currentList.toMutableList()
                val itemIndex = currentList.indexOfFirst { it.id == id }
                if (itemIndex != -1) {
                    currentList.removeAt(itemIndex)
                    submitList(currentList)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update to-do list", e)
            }

    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<ToDoListData>(){
            override fun areItemsTheSame(oldItem: ToDoListData, newItem: ToDoListData): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ToDoListData, newItem: ToDoListData): Boolean {
                return oldItem == newItem
            }

        }
    }
}