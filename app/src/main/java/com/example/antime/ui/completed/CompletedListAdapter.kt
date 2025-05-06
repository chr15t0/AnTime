package com.example.antime.ui.completed

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.antime.algorithm.ToDoListData
import com.example.antime.databinding.ListCompletedBinding
import com.example.antime.databinding.ListToDoListBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class CompletedListAdapter: ListAdapter<ToDoListData,CompletedListAdapter.MyViewHolder>(DIFF_CALLBACK){
    private lateinit var auth : FirebaseAuth

    class MyViewHolder (val binding: ListCompletedBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ToDoListData){
            binding.textCompletedList.text = data.ToDo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListCompletedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val completedList = getItem(position)
        holder.bind(completedList)
        holder.binding.checkboxCompleted.setOnClickListener{
            updateCompletedList(completedList.id)
        }

    }

    private fun updateCompletedList(id: String) {
        auth = Firebase.auth
        val user = auth.currentUser
        val db = Firebase.firestore
        db.collection("users").document(user?.uid.toString()).collection("task").document(id)
            .update("is done", "false")
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully updated")
                val currentList = currentList.toMutableList()
                val itemIndex = currentList.indexOfFirst { it.id == id }
                if (itemIndex != -1) {
                    currentList.removeAt(itemIndex)
                    submitList(currentList)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update", e)
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