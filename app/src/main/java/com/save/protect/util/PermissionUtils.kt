// PermissionUtils.kt

package com.save.protect.util

import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.save.protect.activity.PermissionResultListener

object PermissionUtils {

    // 위치 권한이 있는지 확인
    fun hasLocationPermission(activity: FragmentActivity): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 설명이 필요한지 확인
    fun checkShowLocationPermission(activity: FragmentActivity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // 위치 권한 설명 다이얼로그 표시
    fun showLocationPermissionDialog(activity: FragmentActivity, onSuccess: (() -> Unit)? = null) {
        AlertDialog.Builder(activity)
            .setTitle("위치 권한 필요")
            .setMessage("이 작업을 수행하기 위해 위치 권한이 필요합니다.\n 비 동의시 진행 하실 수 없습니다.")
            .setPositiveButton("동의") { _, _ ->
                requestLocationPermission(activity, onSuccess)
            }
            .setNegativeButton("종료") { dialog, _ ->
                dialog.dismiss()
                activity.finish()
            }
            .create()
            .show()
    }

    // 위치 권한 요청
    fun requestLocationPermission(activity: FragmentActivity, onSuccess: (() -> Unit)? = null) {

        ActivityCompat.requestPermissions(activity, locationPermissions, 1000)

        // 권한 요청 결과를 처리하는 메서드를 Activity에 추가해야 합니다.
        if (activity is PermissionResultListener) {
            activity.setOnPermissionResultListener { requestCode, grantResults ->
                if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onSuccess?.invoke() // 권한이 허용된 경우 onSuccess 콜백 호출
                }
            }
        }
    }


    private val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
}
