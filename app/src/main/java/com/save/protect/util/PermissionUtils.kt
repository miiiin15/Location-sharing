// PermissionUtils.kt

package com.save.protect.util

import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity

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
    fun showLocationPermissionDialog(activity: FragmentActivity) {
        AlertDialog.Builder(activity)
            .setTitle("위치 권한 필요")
            .setMessage("이 작업을 수행하기 위해 위치 권한이 필요합니다.\n 미동의시 뒤로이동합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestLocationPermission(activity)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                activity.finish()
            }
            .create()
            .show()
    }

    // 위치 권한 요청
    fun requestLocationPermission(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(activity, locationPermissions, 1000)
    }

    private val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
}
