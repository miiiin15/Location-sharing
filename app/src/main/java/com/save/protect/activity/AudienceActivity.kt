package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import com.save.protect.custom.BottomSheetChat
import com.save.protect.data.LocationData
import com.save.protect.data.UserInfo
import com.save.protect.data.enums.UpdateState
import com.save.protect.database.LocationReceiver
import com.save.protect.database.UserInfoManager
import com.save.protect.util.ImageUtils
import com.save.protect.util.PermissionUtils
import org.json.JSONArray
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*


class AudienceActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var listenerRegistration: ListenerRegistration? = null

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var docId: String
    private var isAutoFocus = true
    private var updateState = UpdateState.DEFAULT

    private lateinit var stateText: TextView
    private lateinit var updateIcon: TextView
    private lateinit var timeText: TextView
    private lateinit var refreshButton: ImageButton
    private lateinit var checkboxAutoFocus: CheckBox

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
        BottomSheetChat.initBottomSheet(
            findViewById(R.id.bottom_sheet_chat),
            onChange = { bttomSheet, newState ->
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
            }
        )

        refreshButton.setOnClickListener() {
            docId?.let {
                fetchLocationData(it)
            }
        }

        checkboxAutoFocus.setOnCheckedChangeListener { buttonView, isChecked ->
            isAutoFocus = isChecked
        }
    }

    private fun initDocId() {
        // 딥링크로 들어온 경우 Intent에서 데이터 추출
        val data = intent?.data
        val deepLinkValue = data?.getQueryParameter("doc_id")

        if (deepLinkValue != null) {
            docId = deepLinkValue
        } else {
            docId = intent.getStringExtra("doc_id").toString()
        }
        docId?.let {
            fetchUserInfo(it)
        }
    }


    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view_audience)
        stateText = findViewById(R.id.stateText)
        timeText = findViewById(R.id.timeText)
        updateIcon = findViewById(R.id.icon_update)
        refreshButton = findViewById(R.id.button_refresh)
        checkboxAutoFocus = findViewById(R.id.checkbox_autoFocus_audience)
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        docId.let {
            LocationReceiver.observeLocationData(it,
                listener = { locationData ->
                    timeText.text = "⏱️ " + getTimeDifference(locationData.date)
                    updateState = UpdateState.SUCCESS
                    draw(locationData)
                },
                onFailure = {
                    timeText.text = "⏱️ 업데이트 없음"
                    updateState = UpdateState.FAIL
                }
            )
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
        LocationReceiver.getLocationData(id,
            listener = { locationData ->
                timeText.text = "⏱️ " + getTimeDifference(locationData.date)
                updateState = UpdateState.SUCCESS
                draw(locationData)
            },
            onFailure = {
                timeText.text = "⏱️ 업데이트 없음"
                updateState = UpdateState.FAIL
            }
        )
    }

    private fun fetchUserInfo(id: String = "") {
        UserInfoManager.getUserInfo(id) {
            shareholderInfo = it
        }
    }

    fun draw(locationData: LocationData) {
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
        }
    }


    // 경로선 그리기 함수
    @SuppressLint("ResourceAsColor")
    fun drawPath(cordList: MutableList<LatLng>) {

        mapView.getMapAsync { nMap ->
            naverMap = nMap

            if (cordList.isNotEmpty()) {
                val firstLatLng = cordList[0]

                if (cordList.size > 2) {
                    // 경로선 좌표 설정
                    pathOverlay.coords = cordList

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
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            val initialLatLng = LatLng(userLatitude, userLongitude)
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0).animate(
                CameraAnimation.Fly
            )
            if (isAutoFocus) {
                naverMap.moveCamera(cameraUpdate)
            }

            marker.position = initialLatLng
            marker.map = naverMap

            stateText.setTextColor(getStateColor(updateState))
            marker.captionColor = getStateColor(updateState)
            marker.captionText =
                if (shareholderInfo.userName.isNotEmpty()) shareholderInfo.userName else "익명의 유저"

            when (updateState) {
                UpdateState.DEFAULT -> {
                    stateText.text = "⏳ 대기"
                }

                UpdateState.SUCCESS -> {
                    stateText.text = "✅ 정상"
                    updateIcon.visibility = View.VISIBLE

                    Handler(Looper.getMainLooper()).postDelayed({
                        marker.captionColor = Color.BLACK
                        updateIcon.visibility = View.INVISIBLE
                    }, 500)
                }

                UpdateState.FAIL -> {
                    stateText.text = "❌ 실패"
                }
                else -> {
                    stateText.text = "⚠️ 오류"
                }
            }

            if (shareholderInfo.imageUrl.isNotEmpty()) {
                // 마커 이미지를 URL에서 로드하여 설정
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


    private fun getTimeDifference(inputTime: String?): String {
        if (inputTime == null) {
            return "기록 없음"
        }
        // 입력된 시간을 Date 객체로 변환
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val targetDate = dateFormat.parse(inputTime) ?: return "Invalid date format"

        // 현재 시간 가져오기
        val currentDate = Date()

        // 시간 차이 계산 (밀리초 단위)
        val diffInMillis = abs(currentDate.time - targetDate.time)

        // 1년, 1개월, 1주, 1일, 1시간, 1분에 해당하는 밀리초
        val oneYearInMillis = 365L * 24 * 60 * 60 * 1000
        val oneMonthInMillis = 30L * 24 * 60 * 60 * 1000
        val oneWeekInMillis = 7L * 24 * 60 * 60 * 1000
        val oneDayInMillis = 24 * 60 * 60 * 1000
        val oneHourInMillis = 60 * 60 * 1000
        val oneMinuteInMillis = 60 * 1000

        return when {
            // 1년 이상 차이
            diffInMillis >= oneYearInMillis -> {
                val years = diffInMillis / oneYearInMillis
                "${years}년 전"
            }
            // 1개월 이상 차이
            diffInMillis >= oneMonthInMillis -> {
                val months = diffInMillis / oneMonthInMillis
                "${months}개월 전"
            }
            // 1주 이상 차이
            diffInMillis >= oneWeekInMillis -> {
                val weeks = diffInMillis / oneWeekInMillis
                "${weeks}주 전"
            }
            // 하루 이상 차이
            diffInMillis >= oneDayInMillis -> {
                val days = diffInMillis / oneDayInMillis
                "${days}일 전"
            }
            // 24시간 이내 차이
            else -> {
                val hours = diffInMillis / oneHourInMillis
                val minutes = (diffInMillis % oneHourInMillis) / oneMinuteInMillis

                if (hours > 0) {
                    "${hours}시간 ${minutes}분 전"
                }
                else if (hours < 1 && minutes > 0) {
                    "${minutes}분 전"
                } else {
                    "방금 전"
                }
            }
        }
    }

}
