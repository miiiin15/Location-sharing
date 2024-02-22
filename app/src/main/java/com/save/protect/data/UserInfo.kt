package com.save.protect.data

data class UserInfo(
    var email: String = "",
    var name: String = "",
    var uuid: String = "",
    var imageUrl: String = "",
    var pushToken: String = ""
)