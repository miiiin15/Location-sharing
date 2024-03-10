package com.save.protect.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.BottomSheetChat
import com.save.protect.data.LocationItem
import com.save.protect.data.UserInfo
import com.save.protect.data.UserManagement
import com.save.protect.data.location.repo.LocationRepo
import com.save.protect.helper.Logcat
import com.save.protect.util.ImageUtils
import com.save.protect.util.KakaoUtils
import com.save.protect.util.PermissionUtils.hasLocationPermission
import java.util.*

class ShareholderActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    private lateinit var buttonInvite: Button
    private lateinit var checkboxAutoFocus: CheckBox

    private lateinit var userData: UserInfo
    private var isAutoFocus = true
    private var marker = Marker()


    // 사용자 입력값
    private var settingMarkLimit = 3
    private var settingUpdateInterval = 10
    private var settingMinimumInterval = 3

    private val locationDataList = mutableListOf<LocationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shareholder)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setInitialValues()
        BottomSheetChat.initBottomSheet(
            findViewById(R.id.bottom_sheet_chat),
            onChange = { bttomSheet, newState ->
                Log.d("바텀시트 newState : ", " $newState")
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                }
            },
            onSlide = { bttomSheet, slideOffset ->
//                Log.d("바텀시트 slideOffset : ", " $slideOffset")
            }
        )

        createLocationCallback()

        buttonInvite.setOnClickListener {
            userData.recommenderCode.let {
                KakaoUtils.shareText(
                    this,
                    "위치공유 초대",
                    "위치공유 초대가 왔습니다.",
                    userData.recommenderCode
                )
            }
        }

        checkboxAutoFocus.setOnCheckedChangeListener { buttonView, isChecked ->
            isAutoFocus = isChecked
        }

    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        startLocationUpdates(settingUpdateInterval * 100L, settingMinimumInterval * 100L)
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


    private fun setInitialValues() {
        // Intent로 전달받은 설정 값을 읽어옵니다.
        val markLimit = intent.getIntExtra("MARK_LIMIT", 3)
        val updateInterval = intent.getIntExtra("UPDATE_INTERVAL", 10)
        val minimumInterval = intent.getIntExtra("MINIMUM_INTERVAL", 3)
        userData = UserManagement.getUserInfo() ?: UserInfo()

        // 값이 null인 경우 처리
        if (markLimit != 0 || updateInterval != 0 || minimumInterval != 0) {
            settingMarkLimit = markLimit
            settingMinimumInterval = minimumInterval
            settingUpdateInterval = updateInterval
        }
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view)
        buttonInvite = findViewById(R.id.button_invite)
        checkboxAutoFocus = findViewById(R.id.checkbox_autoFocus)
        mapView.onCreate(savedInstanceState)
    }


    private fun _checkList(latitude: Double, longitude: Double): String {

        locationDataList.add(0, LocationItem(latitude, longitude))

        if (locationDataList.size > settingMarkLimit) {
            locationDataList.removeAt(settingMarkLimit)
        }

        val jsonArray = locationDataList.map { (lat, lng) ->
            val jsonObject = JsonObject()
            jsonObject.addProperty("latitude", lat)
            jsonObject.addProperty("longitude", lng)
            jsonObject
        }

        Log.d("리스트??", Gson().toJson(jsonArray))

        return Gson().toJson(jsonArray)
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                locationResult.locations.forEach { location ->
                    // 위치 변경 시 작업
                    setMapMarker(location.latitude, location.longitude)
                    LocationRepo.saveLocationData(
                        _checkList(location.latitude, location.longitude),
                        success = {
                            if (it.status == "OK") {
                                Logcat.d("성공!")
                            } else {
                                showAlert(it.error.message.toString())
                            }
                        },
                        failure = {
                            loadingDialog.dismiss()
                            showAlert(it.message.toString())
                        },
                        networkFail = {
                            loadingDialog.dismiss()
                            showAlert("$it : " + getString(R.string.network_error_message))
                        }
                    )
                    // 위치 변경을 출력
                    Log.d("위치 추적", "위도: ${location.latitude}, 경도: ${location.longitude}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(updateInterval: Long = 10000, minimumInterval: Long = 3000) {
        if (hasLocationPermission(this)) {
            val locationRequest = LocationRequest()
            locationRequest.interval = updateInterval
            locationRequest.fastestInterval = minimumInterval
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 지도에 마커 표시
    private fun setMapMarker(userLatitude: Double, userLongitude: Double) {
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            val initialLatLng = LatLng(userLatitude, userLongitude)
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0).animate(
                CameraAnimation.Fly
            )
            if (isAutoFocus) {
                naverMap!!.moveCamera(cameraUpdate)
            }


            marker.position = initialLatLng
            marker.map = naverMap
            if (userData.name.isNotEmpty()) {
                marker.captionText = userData.name
            } else {
                marker.captionText = getString(R.string.none_member_name)
            }
            if (userData.imageUrl.isNotEmpty()) {
                // 마커 이미지를 URL에서 로드하여 설정
                Log.d("이미지 주소", " ${userData.imageUrl}")
                ImageUtils.loadBitmapFromUrl(this, userData.imageUrl) {
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
