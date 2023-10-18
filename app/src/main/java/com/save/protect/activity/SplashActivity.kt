package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.save.protect.util.PermissionUtils
import com.save.protect.R
import com.save.protect.data.UserManagement

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var stateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        stateText = findViewById(R.id.stateText)

        // 유저 정보 초기화
        UserManagement.resetUserInfo()
        checkLocationPermission()

    }


    // 위치 권한 확인
    private fun checkLocationPermission() {
        when {
            PermissionUtils.hasLocationPermission(this) -> {
                next()
            }
            PermissionUtils.checkShowLocationPermission(this) -> {
                PermissionUtils.showLocationPermissionDialog(this)
            }
            PackageManager.PERMISSION_DENIED == -1 -> {
                PermissionUtils.showLocationPermissionDialog(this)
            }
            else -> {
                PermissionUtils.requestLocationPermission(this)
            }
        }
    }


    private fun next() {
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }
}