package com.example.antime.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Activities(
    var prodi:String,
    var pic:String,
    var programmer: String
):Parcelable
