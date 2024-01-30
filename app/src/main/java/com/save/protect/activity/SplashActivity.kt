package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.save.protect.R
import com.save.protect.data.DocIdManagement
import com.save.protect.data.UserManagement
import com.save.protect.databinding.ActivitySplashBinding
import com.save.protect.util.PermissionUtils

interface PermissionResultListener {
    fun setOnPermissionResultListener(listener: (requestCode: Int, grantResults: IntArray) -> Unit)
}

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), PermissionResultListener {
    private lateinit var binding: ActivitySplashBinding
    private var permissionResultListener: ((requestCode: Int, grantResults: IntArray) -> Unit)? = null

    override fun setOnPermissionResultListener(listener: (requestCode: Int, grantResults: IntArray) -> Unit) {
        permissionResultListener = listener
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResultListener?.invoke(requestCode, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        // 유저 정보 초기화
        UserManagement.resetUserInfo()
        initDocId()
        checkLocationPermission()
    }

    // 위치 권한 확인
    private fun checkLocationPermission() {
        binding.stateText.text = "위치 권한 동의가 필요합니다."
        when {
            PermissionUtils.hasLocationPermission(this) -> {
                next()
            }
            PermissionUtils.checkShowLocationPermission(this) -> {
                println(1)
                PermissionUtils.showLocationPermissionDialog(this){
                    next()
                }
            }
            PackageManager.PERMISSION_DENIED == -1 -> {
                println(2)
                PermissionUtils.showLocationPermissionDialog(this){
                    next()
                }
            }
            else -> {
                println(3)
                PermissionUtils.requestLocationPermission(this){}
            }
        }
    }

    private fun initDocId() {
        // 딥링크로 들어온 경우 Intent에서 데이터 추출
        val data = intent?.data
        val deepLinkValue = data?.getQueryParameter("doc_id")

        if (deepLinkValue != null) {
            binding.stateText.text = "초대 받은 사용자 입니다."
            DocIdManagement.setReceivedId(deepLinkValue)
        }
    }


    private fun next() {
        binding.stateText.text = "위치 권한 확인 완료"
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }
}