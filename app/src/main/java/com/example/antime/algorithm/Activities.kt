package com.example.antime.algorithm

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Activities(
    var prodi:String,
    var pic:String,
    var programmer: String
):Parcelable

@Parcelize
data class Assignment(
    var activities: Activities,
    var room: String,
    var day: String,
    var startHour: Int
):Parcelable

@Parcelize
data class Schedule(
    val day: String,
    val prodi: String,
    val pic: String,
    val programmer: String,
    val room: String,
    val startHour: String,
    val endHour: String
):Parcelable

@Parcelize
data class DailySchedule(
    val day: String,
    val activities : List<Schedule>
):Parcelable

val days = listOf("Monday","Tuesday","Wednesday","Thursday","Friday")
val rooms = listOf("Ruangan 1 Ditgrasi ","Ruangan 2 Ditgrasi")
val timeSlots = listOf(8,10,13,15)