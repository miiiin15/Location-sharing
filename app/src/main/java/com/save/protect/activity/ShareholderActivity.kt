package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.MarkerIcons
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.BottomSheetChat
import com.save.protect.custom.ButtonListener
import com.save.protect.data.UserInfo
import com.save.protect.data.UserManagement
import com.save.protect.data.enums.UpdateState
import com.save.protect.database.FirebaseReceiver
import com.save.protect.database.FirebaseTransmitter
import com.save.protect.database.UserInfoManager
import com.save.protect.databinding.ActivityShareholderBinding
import com.save.protect.helper.Logcat
import com.save.protect.util.ImageUtils
import com.save.protect.util.KakaoUtils
import com.save.protect.util.PermissionUtils.checkShowLocationPermission
import com.save.protect.util.PermissionUtils.hasLocationPermission
import com.save.protect.util.PermissionUtils.requestLocationPermission
import com.save.protect.util.PermissionUtils.showLocationPermissionDialog


class ShareholderActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var binding: ActivityShareholderBinding

    private lateinit var userData: UserInfo
    private lateinit var userName: String
    private var uid: String? = null
    private var audienceLatLng: LatLng? = null
    private lateinit var firstLatLng: LatLng

    private lateinit var naverMap: NaverMap
    private var shareHolderMarker = Marker()
    private var audienceMarker = Marker()
    private var betweenLine = PolylineOverlay()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<BottomSheetChat>

    private var isAutoFocus = true
    private val markSize = 100

    private var updateState: UpdateState = UpdateState.DEFAULT


    // 사용자 입력값
    private var setting_markLimit = 3
    private var setting_updateInterval = 10
    private var setting_minimunInterval = 3


    // 위치 데이터를 저장하는 리스트 (제한된 길이 유지)
    private val locationDataList = mutableListOf<Location>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareholderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setInitialValues()
        createLocationCallback()
        checkLocationPermission()


        binding.buttonInvite.setOnClickListener {
            val name = UserManagement.getUserInfo()?.userName ?: "익명의 유저"
            KakaoUtils.shareText(this, "${name}님의 초대", "위치공유 초대가 왔습니다.", "${uid}")
        }

        binding.buttonShare.setOnClickListener {
            shareText(this, "${uid}")

        }

        binding.checkboxAutoFocus.setOnCheckedChangeListener { buttonView, isChecked ->
            isAutoFocus = isChecked
        }

    }


    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        startLocationUpdates(setting_updateInterval * 100L, setting_minimunInterval * 100L)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    fun shareText(context: Context, textToShare: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "공유할 앱 선택"))
    }


    private fun setInitialValues() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.shareholderChat)
        // Intent로 전달받은 설정 값을 읽어옵니다.
        val markLimit = intent.getIntExtra("MARK_LIMIT", 3)
        val updateInterval = intent.getIntExtra("UPDATE_INTERVAL", 10)
        val minimumInterval = intent.getIntExtra("MINIMUM_INTERVAL", 3)
        uid = UserManagement.uid
        userData = UserManagement.getUserInfo()!!
        userName = if (userData.userName.isNullOrBlank()) userData.userName else "익명의 유저"

        UserManagement.uid.let {
            FirebaseReceiver.observeMessage(
                it,
                listener = { messageData ->
                    if (getTimeDifference(messageData.date) === "방금 전" && messageData.id != uid) {
                        Toast.makeText(
                            applicationContext,
                            "${messageData.userName} : ${messageData.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (messageData.latitude != null && messageData.longitude != null) {
                            setBetweenPath(
                                naverMap, firstLatLng, LatLng(
                                    messageData.latitude,
                                    messageData.longitude
                                )
                            )
                            fetchAudienceInfo(messageData.id ?: "") { audience ->
                                audienceLatLng = LatLng(
                                    messageData.latitude,
                                    messageData.longitude
                                )
                                setMapMarker(
                                    audience!!,
                                    messageData.latitude,
                                    messageData.longitude,
                                    null,
                                    true
                                )
                            }
                        }


                    }
                },

                )
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.shareholderChat.setTitle("입력창 닫으려면 내리기")
                        binding.shareholderChat.moveFocusInput()
                        showKeyboard()
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.shareholderChat.setTitle("메시지 보내려면 올리기")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        closeKeyboard()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        binding.shareholderChat.setButtonListener(object : ButtonListener {
            override fun callback() {
                if (uid.isNullOrBlank()) {
                    return
                }
                if (binding.shareholderChat.getMessage().isNullOrBlank()) {
                    return
                }
                FirebaseTransmitter.sendMessage(
                    documentId = uid!!,
                    userId = uid!!,
                    message = binding.shareholderChat.getMessage(),
                    userName = userData.userName,
                    onSuccess = {},
                    onFailure = {}
                )
            }
        }
        )


        // 값이 null인 경우 처리
        if (markLimit != 0 || updateInterval != 0 || minimumInterval != 0) {
            setting_markLimit = markLimit
            setting_minimunInterval = minimumInterval
            setting_updateInterval = updateInterval
        }
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
    }

    // 위치 권한 확인
    private fun checkLocationPermission() {
        when {
            hasLocationPermission(this) -> {
            }

            checkShowLocationPermission(this) -> {
                showLocationPermissionDialog(this)
            }

            PackageManager.PERMISSION_DENIED == -1 -> {
                showLocationPermissionDialog(this)
            }

            else -> {
                requestLocationPermission(this)
            }
        }
    }

    private fun _checkList(locationData: Location): MutableList<Location> {
        // 위치 데이터를 리스트에 추가
        locationDataList.add(0, locationData)
        // 리스트가 제한된 길이를 초과하는 경우, 가장 오래된 데이터를 제거 (선입선출)
        if (locationDataList.size > setting_markLimit) {
            locationDataList.removeAt(setting_markLimit)
        }

        return locationDataList
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // 위치 변경 시 작업
                    uid?.let {
                        FirebaseTransmitter.sendLocationData(
                            it,
                            _checkList(location),
                            userName = userData.userName,
                            onSuccess = {
                                setMapMarker(
                                    userData,
                                    location.latitude,
                                    location.longitude,
                                    UpdateState.SUCCESS
                                )
                            },
                            onFailure = {
                                setMapMarker(
                                    userData,
                                    location.latitude,
                                    location.longitude,
                                    UpdateState.FAIL
                                )
                            })
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(updateInterval: Long = 10000, minimumInterval: Long = 3000) {
        Logcat.d("위치 추적 시작")

        if (hasLocationPermission(this)) {
            val locationRequest = LocationRequest()
            locationRequest.interval = updateInterval // 위치 업데이트 간격 (밀리초)
            locationRequest.fastestInterval = minimumInterval // 가장 빠른 업데이트 간격 (밀리초)
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    fun stopLocationUpdates() {
        Logcat.d("위치 추적 중지")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 지도에 마커 표시
    private fun setMapMarker(
        userData: UserInfo,
        userLatitude: Double,
        userLongitude: Double,
        updateState: UpdateState?,
        isAudience: Boolean = false
    ) {
        val marker = if (isAudience) audienceMarker else shareHolderMarker

        firstLatLng = LatLng(userLatitude, userLongitude)
        audienceLatLng?.let { setBetweenPath(naverMap, firstLatLng, it) }

        if (updateState != null) {
            this.updateState = updateState
        }

        binding.mapView.getMapAsync { nMap ->
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
            marker.captionText = userData.userName


            if (!isAudience) {
                binding.stateText.setTextColor(getStateColor(updateState!!))
                marker.captionColor = getStateColor(updateState)

                when (updateState) {
                    UpdateState.DEFAULT -> {
                        binding.stateText.text = "⏳ 대기"
                    }

                    UpdateState.SUCCESS -> {
                        binding.stateText.text = "✅ 정상"
                        binding.iconUpdate.visibility = View.VISIBLE

                        Handler(Looper.getMainLooper()).postDelayed({
                            marker.captionColor = Color.BLACK
                            binding.iconUpdate.visibility = View.INVISIBLE
                        }, 500)
                    }

                    UpdateState.FAIL -> {
                        binding.stateText.text = "❌ 실패"
                    }

                    else -> {
                        binding.stateText.text = "⚠️ 오류"
                    }
                }
            }

            if (userData.imageUrl.isNotEmpty()) {
                // 마커 이미지를 URL에서 로드하여 설정
                ImageUtils.loadBitmapFromUrl(this, userData.imageUrl) {
                    marker.icon = OverlayImage.fromBitmap(
                        // 이미지 리사이징
                        ImageUtils.resizeAndCropToCircle(it!!, markSize, markSize, 3, Color.BLACK)
                    )
                }
            } else {
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = Color.LTGRAY
            }
        }
    }

    private fun fetchAudienceInfo(id: String, callback: (UserInfo?) -> Unit) {
        id.let {
            UserInfoManager.getUserInfo(it) { audience ->
                callback(audience)
            }
        }
    }

    private fun setBetweenPath(
        naverMap: NaverMap,
        startLatLng: LatLng,
        endLatLng: LatLng
    ) {
        betweenLine.coords = listOf(startLatLng, endLatLng)

        betweenLine.color = getColor(R.color.primary)
        betweenLine.width = 5

        betweenLine.map = naverMap
    }

    private fun getStateColor(state: UpdateState): Int {
        when (state) {
            UpdateState.DEFAULT -> {
                return getColor(R.color.disable)
            }

            UpdateState.SUCCESS -> {
                return Color.GREEN
            }

            UpdateState.FAIL -> {
                return Color.RED
            }

            else -> {
                return Color.YELLOW
            }
        }
    }
}
