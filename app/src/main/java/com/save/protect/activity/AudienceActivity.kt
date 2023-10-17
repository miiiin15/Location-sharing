package com.save.protect.activity

import android.annotation.SuppressLint
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
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.MarkerIcons
import com.save.protect.R
import com.save.protect.data.LocationData
import com.save.protect.data.UserInfo
import com.save.protect.database.LocationReceiver
import com.save.protect.database.UserInfoManager
import com.save.protect.util.ImageUtils
import com.save.protect.util.PermissionUtils
import org.json.JSONArray


class AudienceActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var listenerRegistration: ListenerRegistration? = null

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var docId: String

    // 경로선 객체
    private var pathOverlay = PathOverlay()

    // 마커 객체
    private var marker = Marker()

    // 유저정보
    private lateinit var shareholderInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audience)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // 진입경로에 따른 값 초기화
        initDocId()
        // 위치 권한 확인
        checkLocationPermission()

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
        docId?.let {
            fetchUserInfo(it)
        }
    }


    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view_audience)
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        docId.let {
            LocationReceiver.observeLocationData(it) { locationData ->
                draw(locationData)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        docId?.let {
            fetchLocationData(it)
        }
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
            draw(locationData)
        }
    }

    private fun fetchUserInfo(id: String = "") {
        UserInfoManager.getUserInfo(id) {
            Log.d("유저정보", " $it")
            shareholderInfo = it

        }
    }

    fun draw(locationData: LocationData) {
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


    // 경로선 그리기 함수
    @SuppressLint("ResourceAsColor")
    fun drawPath(coords: MutableList<LatLng>) {

        mapView.getMapAsync { nMap ->
            naverMap = nMap

            Log.d("그리기용 정보 ", "$coords")
            if (coords.isNotEmpty()) {
                val firstLatLng = coords[0]
                val initialLatLng = LatLng(firstLatLng.latitude, firstLatLng.longitude)
                val cameraUpdate =
                    CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0).animate(CameraAnimation.Fly)

                if (coords.size > 2) {
                    // 경로선 좌표 설정
                    pathOverlay.coords = coords

                    // 경로선 색상 및 두께 설정 (옵션)
                    pathOverlay.color = Color.WHITE// 색상 설정
                    pathOverlay.width = 10 // 두께 설정 (픽셀)
                    pathOverlay.outlineWidth = 5
                    pathOverlay.outlineColor = Color.BLACK
                    // 지도에 경로선 추가
                    pathOverlay.map = naverMap
                } else {
                    pathOverlay.map = null
                }
                // 카메라 이동
                naverMap.moveCamera(cameraUpdate)
                // 마커 찍기
                setMapMarker(firstLatLng.latitude, firstLatLng.longitude)

            } else {
                // coords 배열이 비어있을 경우
                val toast = Toast.makeText(this, "좌표 데이터 없음", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }


    // 지도에 마커 표시
    private fun setMapMarker(userLatitude: Double, userLongitude: Double) {
        Log.d("위치 추적", "마킹값 위도: ${userLatitude}, 마킹값 경도: ${userLongitude}")
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            val initialLatLng = LatLng(userLatitude, userLongitude)
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0).animate(
                CameraAnimation.Fly
            )
            naverMap.moveCamera(cameraUpdate)

            marker.position = initialLatLng
            marker.map = naverMap
            if (shareholderInfo.userName.isNotEmpty()) {
                marker.captionText = shareholderInfo.userName
            } else {
                marker.captionText = "익명의 유저"
            }
            if (shareholderInfo.imageUrl.isNotEmpty()) {
                // 마커 이미지를 URL에서 로드하여 설정
                Log.d("이미지 주소", " ${shareholderInfo.imageUrl}")
                ImageUtils.loadBitmapFromUrl(this, shareholderInfo.imageUrl) {
                    marker.icon = OverlayImage.fromBitmap(
                        // 이미지 리사이징
                        ImageUtils.resizeAndCropToCircle(it!!, 100, 100, 3, Color.BLACK)
                    )
                }
            } else {
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = Color.LTGRAY
            }
        }
    }

}
