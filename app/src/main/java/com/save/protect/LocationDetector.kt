package com.save.protect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class LocationDetector(private val context: Context) {
    private val TAG = "LocationDetector"
    private val locationClient: FusedLocationProviderClient =
        FusedLocationProviderClient(context)

    // 위치 변경을 감지하고 처리할 콜백 함수
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                // 위치 변경을 로그에 기록
                Log.d(TAG, "새로운 위")
            }
        }
    }

    // 위치 변경 감지 시작
    fun startLocationUpdates() {
        if (checkLocationPermission()) {
            val locationRequest = LocationRequest()
            locationRequest.interval = 3000 // 위치 업데이트 간격 (밀리초)
            locationRequest.fastestInterval = 1000 // 가장 빠른 업데이트 간격 (밀리초)
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            Log.e(TAG, "위치 권한이 없습니다.")
        }
    }

    // 위치 변경 감지 중지
    fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    // 위치 권한 확인
    private fun checkLocationPermission(): Boolean {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = PackageManager.PERMISSION_GRANTED
        return ActivityCompat.checkSelfPermission(context, permission) == granted
    }
}

