package com.example.antime.ui

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

val days = listOf("Monday","Tuesday","Wednesday","Thursday","Friday")
val rooms = listOf("Ruangan 1 Ditgrasi ","Ruangan 2 Ditgrasi")
val timeSlots = listOf(8,10,13,15)