package com.example.antime.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.antime.databinding.CardDaysBinding
import com.example.antime.algorithm.DailySchedule

class DaysAdapter: ListAdapter<DailySchedule, DaysAdapter.MyViewHolder> (DIFF_CALLBACK) {

    private lateinit var onItemClickCallback:OnItemClickCallback
    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback){
        this.onItemClickCallback = onItemClickCallback
    }

    class MyViewHolder (val binding : CardDaysBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(dailySchedule: DailySchedule){
            binding.textDay.text = dailySchedule.day
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysAdapter.MyViewHolder {
        val binding = CardDaysBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DaysAdapter.MyViewHolder, position: Int) {
        val dailySchedule = getItem(position)
        holder.bind(dailySchedule)
        holder.itemView.setOnClickListener{
            onItemClickCallback.onItemClicked(dailySchedule,ActivityOptionsCompat.makeBasic())
        }
    }

    interface OnItemClickCallback{
        fun onItemClicked(schedule: DailySchedule, options: ActivityOptionsCompat)
    }

    companion object{
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<DailySchedule>(){
            override fun areItemsTheSame(oldItem: DailySchedule, newItem: DailySchedule): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DailySchedule, newItem: DailySchedule): Boolean {
                return oldItem == newItem
            }

        }
    }
}