package com.save.protect.data

import android.location.Location

data class MessageObject(
    val id : String?="",
    val latitude : Double?=null,
    val longitude : Double?=null,
    val message: String? ="",
    val date: String? = "",
    val userName: String? = ""
)
