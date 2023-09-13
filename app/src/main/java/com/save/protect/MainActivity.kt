package com.save.protect

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.save.protect.PermissionUtils.hasLocationPermission
import com.save.protect.PermissionUtils.requestLocationPermission
import com.save.protect.PermissionUtils.shouldShowLocationPermissionRationale
import com.save.protect.PermissionUtils.showLocationPermissionExplanationDialog

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()

        // 위치 권한 확인
        checkLocationPermission()

    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
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
            hasLocationPermission(this) -> {
//                getLastKnownLocation()
                        startLocationUpdates()
            }

            shouldShowLocationPermissionRationale(this) -> {
                showLocationPermissionExplanationDialog(this)
            }
            PackageManager.PERMISSION_DENIED == -1 -> {
                showLocationPermissionExplanationDialog(this)
            }
            else -> {
                requestLocationPermission(this)
            }
        }
    }




    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // 위치 변경을 출력
                    // 위치 변경 시 필요한 작업을 여기에 추가할 수 있습니다.
                    // 예를 들어, 다른 클래스에 위치 정보를 전달하거나 지도에 마커를 표시할 수 있습니다.
                    setMapMarker(location.latitude,location.longitude)
                    Log.d("위치 추적", "위도: ${location.latitude}, 경도: ${location.longitude}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(updateInterval: Long = 10000, minimunInterval: Long = 5000) {
        Log.d("위치 추적", "시작")
        if (hasLocationPermission(this)) {
            val locationRequest = LocationRequest()
            locationRequest.interval = updateInterval // 위치 업데이트 간격 (밀리초)
            locationRequest.fastestInterval = minimunInterval // 가장 빠른 업데이트 간격 (밀리초)
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    fun stopLocationUpdates() {
        Log.d("위치 추적", "중지")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    // 지도에 마커 표시
    public fun setMapMarker(userLatitude: Double, userLongitude: Double) {
        Log.d("위치 추적", "마킹값 위도: ${userLatitude}, 마킹값 경도: ${userLongitude}")
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
}
