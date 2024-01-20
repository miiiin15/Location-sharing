package com.save.protect.data

data class LocationData(
    val locationList: MutableList<MutableMap<String, Any?>>? = null,
    val date: String? = "",
    val userName: String? = ""
)
