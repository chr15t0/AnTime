package com.example.antime.ui.detailDailyActivities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.antime.databinding.CardActivityBinding
import com.example.antime.algorithm.Schedule

class ScheduleAdapter:ListAdapter<Schedule, ScheduleAdapter.MyViewHolder>(DIFF_CALLBACK) {
    class MyViewHolder (val binding: CardActivityBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(schedule: Schedule){
            binding.textProdi.text = schedule.prodi
            val hour =  schedule.startHour + " - " + schedule.endHour
            binding.textHour.text = hour
            binding.textRoom.text = schedule.room
            binding.textPic.text = schedule.pic
            binding.textProgrammer.text = schedule.programmer
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CardActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule)

    }

    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Schedule>(){
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }

        }
    }
}