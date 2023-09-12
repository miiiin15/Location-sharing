package com.save.protect

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    private var userLatitude = 37.5670135
    private var userLongitude = 126.9783740

    private val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // 위치 변경을 출력
                    Log.d("위치 추적", "위도: ${location.latitude}, 경도: ${location.longitude}")
                }
            }
        }
        // 위치 권한 확인
        checkLocationPermission()

    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest()
            locationRequest.interval = 2000 // 위치 업데이트 간격 (밀리초)
            locationRequest.fastestInterval = 1000 // 가장 빠른 업데이트 간격 (밀리초)
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        startLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    // 위치 권한 확인
    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
//                getLastKnownLocation()
            }

            shouldShowLocationPermissionRationale() -> {
                showLocationPermissionExplanationDialog()
            }
            PackageManager.PERMISSION_DENIED == -1 -> {
                showLocationPermissionExplanationDialog()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    // 위치 권한이 있는지 확인
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 설명이 필요한지 확인
    private fun shouldShowLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // 위치 권한 설명 다이얼로그 표시
    private fun showLocationPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("위치 권한 필요")
            .setMessage("이 작업을 수행하기 위해 위치 권한이 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // 위치 권한 요청
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, 1000)
    }

    // 사용자의 마지막 위치 가져오기
    private fun getLastKnownLocation() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLatitude = location.latitude.toDouble()
                        userLongitude = location.longitude.toDouble()
                        setMapMarker()
                    } else {
                        Log.d("위치 정보", "위치정보 없음")
                    }
                }
        } else {
            Log.d("위치 정보", "권한 없음")
        }
    }

    // 지도에 마커 표시
    private fun setMapMarker() {
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            val initialLatLng = LatLng(userLatitude, userLongitude)
            val cameraUpdate = CameraUpdate.scrollTo(initialLatLng)
            naverMap.moveCamera(cameraUpdate)

            val marker = Marker()
            marker.position = initialLatLng
            marker.map = naverMap
            marker.captionText = "유저*23#"
        }
    }



    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
