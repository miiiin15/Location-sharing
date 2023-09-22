package com.save.protect

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
import com.kakao.sdk.common.KakaoSdk

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var stateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        stateText = findViewById(R.id.stateText)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Kakao SDK 초기화
//        KakaoSdk.init(this, "${R.string.kakao_native_key}")
        checkLocationPermission()

    }


    // 위치 권한 확인
    private fun checkLocationPermission() {
        when {
            PermissionUtils.hasLocationPermission(this) -> {
                login()
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

    private fun login() {
        stateText.text = "로그인 중..."
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val handler = Handler()

                    handler.postDelayed({
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("로그인", "signInAnonymously:success")
                        next()
                    }, 2000)
                } else {
                    stateText.text = "로그인 실패.!"
                    // If sign in fails, display a message to the user.
                    Log.w("로그인", "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "로그인 실패",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun next() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}