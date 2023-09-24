package com.save.protect.activity

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.firebase.firestore.ListenerRegistration
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.save.protect.database.LocationReceiver
import com.save.protect.util.PermissionUtils
import com.save.protect.R
import org.json.JSONArray


class AudienceActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var listenerRegistration: ListenerRegistration? = null

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    var marker = Marker()
    val path = PathOverlay()
    private lateinit var docId: String


    // 위치 데이터를 저장하는 리스트 (제한된 길이 유지)
    private val locationDataList = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audience)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // 진입경로에 따른 값 초기화
        initDocId()
        // 위치 권한 확인
        checkLocationPermission()
        // Firestore 위치 데이터 수신
        docId?.let { fetchLocationData(it) }
    }

    private fun initDocId() {
        // 딥링크로 들어온 경우 Intent에서 데이터 추출
        val data = intent?.data
        val deepLinkValue = data?.getQueryParameter("doc_id")

        if (deepLinkValue != null) {
            docId = deepLinkValue
            deepLinkValue.let { Log.d("딥링크 값", it.toString()) }
        } else {
            docId = intent.getStringExtra("doc_id").toString()

        }
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view_audience)
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        docId.let { LocationReceiver.observeLocationData(it) }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        docId?.let { fetchLocationData(it) }
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
        listenerRegistration?.remove()
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
            PermissionUtils.hasLocationPermission(this) -> {
//                getLastKnownLocation()
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

    // 단발성 위치 수신기
    private fun fetchLocationData(id: String = "") {
        LocationReceiver.getLocationData(id) { locationData ->
            // Handle received location data
            val locationList = locationData.locationList

            if (locationList != null && locationList.isNotEmpty()) {
                val coords = mutableListOf<LatLng>()

                val jsonArray = JSONArray(locationList)

                // 위치 데이터를 기반으로 좌표 리스트를 생성합니다.
                for (i in 0 until jsonArray.length()) {
                    val location = jsonArray.getJSONObject(i)
                    val latLng =
                        LatLng(location.getDouble("latitude"), location.getDouble("longitude"))
                    coords.add(latLng)
                }
                // 생성한 좌표 리스트를 사용하여 경로선 그리기 함수 호출
                drawPath(coords)
            } else {
                Log.d("위치 수신기", "No location data available.")
            }
        }
    }

    // 경로선 그리기 함수
    fun drawPath(coords: MutableList<LatLng>) {
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            if (coords.isNotEmpty()) {
                // 경로선 객체 생성
                val pathOverlay = PathOverlay()

                // 경로선 좌표 설정
                pathOverlay.coords = coords

                // 경로선 색상 및 두께 설정 (옵션)
                pathOverlay.color = Color.BLUE // 색상 설정
                pathOverlay.width = 5 // 두께 설정 (픽셀)

                // 네이버 지도에 경로선 추가
                pathOverlay.map = naverMap

                // 현재 줌 레벨 가져오기
                val currentZoom = naverMap.cameraPosition.zoom

                // coords 배열이 존재할 경우 0번째 값을 로그로 출력
                val firstLatLng = coords[0]
                val initialLatLng = LatLng(firstLatLng.latitude, firstLatLng.longitude)
                val cameraUpdate =
                    CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0).animate(CameraAnimation.Fly)
                // 카메라 이동
                naverMap.moveCamera(cameraUpdate)

            } else {
                // coords 배열이 비어있을 경우 Toast를 사용하여 알림 표시
                val toast = Toast.makeText(this, "좌표 데이터 없음", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }


    // 지도에 마커 표시
    public fun setMapMarker(userLatitude: Double, userLongitude: Double) {
        Log.d("위치 추적", "마킹값 위도: ${userLatitude}, 마킹값 경도: ${userLongitude}")
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            val initialLatLng = LatLng(userLatitude, userLongitude)
            val cameraUpdate = CameraUpdate.scrollTo(initialLatLng)
            naverMap.moveCamera(cameraUpdate)


            marker.position = initialLatLng
            marker.map = naverMap
            marker.captionText = "유저*23#"
        }
    }
}
