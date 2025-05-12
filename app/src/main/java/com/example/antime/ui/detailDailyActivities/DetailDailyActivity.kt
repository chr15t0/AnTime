package com.example.antime.ui.detailDailyActivities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.antime.algorithm.Schedule
import com.example.antime.databinding.ActivityDetailDailyBinding

class DetailDailyActivity : AppCompatActivity() {
    private lateinit var  binding : ActivityDetailDailyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityDetailDailyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textDay.text = intent.getStringExtra(SCHEDULE)
        var scheduleList = intent.getParcelableArrayListExtra<Schedule>(SCHEDULE_LIST) ?: emptyList()
        val timeOrder = listOf("8:00","10:00","13:00","15:00")
        val roomOrder = listOf("Ruangan 1 Ditgrasi ","Ruangan 2 Ditgrasi")
        scheduleList = scheduleList.sortedWith(compareBy({ timeOrder.indexOf(it.startHour) }, {roomOrder.indexOf(it.room)}))
        setScheduleAdapter(scheduleList)
    }

    private fun setScheduleAdapter(scheduleList: List<Schedule>) {
        val adapter = ScheduleAdapter()
        adapter.submitList(scheduleList)
        binding.rvSchedule.adapter = adapter
    }

    companion object{
        const val SCHEDULE = "SCHEDULE"
        const val SCHEDULE_LIST = "SCHEDULE_LIST"

    }
}