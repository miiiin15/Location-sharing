package com.save.protect.data.auth

data class SignIn(
    val email : String,
    val accessToken : String,
    val refreshToken : String,
)
