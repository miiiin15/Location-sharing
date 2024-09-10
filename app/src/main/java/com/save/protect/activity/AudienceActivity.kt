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
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.BottomSheetChat
import com.save.protect.custom.ButtonListener
import com.save.protect.data.LocationData
import com.save.protect.data.UserInfo
import com.save.protect.data.UserManagement
import com.save.protect.data.enums.UpdateState
import com.save.protect.database.FirebaseReceiver
import com.save.protect.database.FirebaseTransmitter
import com.save.protect.database.UserInfoManager
import com.save.protect.util.ImageUtils
import com.save.protect.util.PermissionUtils
import org.json.JSONArray
import java.util.*

class AudienceActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var listenerRegistration: ListenerRegistration? = null

    private lateinit var docId: String
    private var isAutoFocus = true
    private var updateTime: String? = null
    private var updated = false
    private var updateState = UpdateState.DEFAULT

    private var uid: String? = null
    private lateinit var userData: UserInfo
    private lateinit var userName: String

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var bottomSheetChat: BottomSheetChat
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<BottomSheetChat>
    private lateinit var stateText: TextView
    private lateinit var updateIcon: TextView
    private lateinit var timeText: TextView
    private lateinit var refreshButton: ImageButton
    private lateinit var checkboxAutoFocus: CheckBox

    private var pathOverlay = PathOverlay()
    private var marker = Marker()
    private lateinit var shareholderInfo: UserInfo

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            handler.postDelayed(this, 60000)
            timeText.text = "⏱️ " + getTimeDifference(updateTime)
            updateTime = updateTime

            if (updated) {
                updateState = UpdateState.DEFAULT
                updateUIBasedOnState()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audience)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initDocId()
        checkLocationPermission()
        setInitialValues()


    }

    private fun initDocId() {
        val data = intent?.data
        val deepLinkValue = data?.getQueryParameter("doc_id")

        docId = deepLinkValue ?: intent.getStringExtra("doc_id").toString()
        docId.let { fetchUserInfo(it) }
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view_audience)
        bottomSheetChat = findViewById(R.id.audience_chat)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetChat)
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
            FirebaseReceiver.observeLocationData(it,
                listener = { locationData ->
                    updateState = UpdateState.SUCCESS
                    timeText.text = "⏱️ " + getTimeDifference(locationData.date)
                    draw(locationData)
                    updateUIBasedOnState()
                },
                onFailure = {
                    updateState = UpdateState.FAIL
                    timeText.text = "⏱️ 업데이트 없음"
                    updateUIBasedOnState()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        docId.let { fetchLocationData(it) }
        startTask()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        stopTask()
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

    private fun startTask() {
        handler.post(runnable)
    }

    private fun stopTask() {
        handler.removeCallbacks(runnable)
    }

    private fun setInitialValues() {
        uid = UserManagement.uid
        userData = UserManagement.getUserInfo()!!
        userName = if (userData.userName.isNullOrBlank()) userData.userName else "익명의 유저"

        docId.let {
            FirebaseReceiver.observeMessage(
                it,
                listener = { messageData ->
                    if (getTimeDifference(messageData.date) === "방금 전" && messageData.id != uid) {
                        Toast.makeText(
                            applicationContext,
                            "${messageData.userName} : ${messageData.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                )
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottomSheetChat.setTitle("입력창 닫으려면 내리기")
                        bottomSheetChat.moveFocusInput()
                        showKeyboard()
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheetChat.setTitle("메시지 보내려면 올리기")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        closeKeyboard()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        bottomSheetChat.setButtonListener(object : ButtonListener {
            override fun callback() {
                if (UserManagement.uid.isNullOrBlank()) {
                    return
                }
                if (bottomSheetChat.getMessage().isNullOrBlank()) {
                    return
                }
                FirebaseTransmitter.sendMessage(
                    documentId = docId,
                    userId = UserManagement.uid!!,
                    message = bottomSheetChat.getMessage(),
                    userName = userData.userName,
                    onSuccess = {},
                    onFailure = {}
                )
            }
        }
        )

        refreshButton.setOnClickListener {
            docId.let { fetchLocationData(it) }
        }
        checkboxAutoFocus.setOnCheckedChangeListener { _, isChecked ->
            isAutoFocus = isChecked
        }
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

    private fun fetchLocationData(id: String = "") {
        FirebaseReceiver.getLocationData(id,
            listener = { locationData ->
                updateState = UpdateState.SUCCESS
                timeText.text = "⏱️ " + getTimeDifference(locationData.date)
                draw(locationData)
                updateUIBasedOnState()
            },
            onFailure = {
                updateState = UpdateState.FAIL
                timeText.text = "⏱️ 업데이트 없음"
                updateUIBasedOnState()
            }
        )
    }

    private fun fetchUserInfo(id: String = "") {
        UserInfoManager.getUserInfo(id) { shareholderInfo = it }
    }

    private fun updateUIBasedOnState() {
        when (updateState) {
            UpdateState.DEFAULT -> {
                stateText.text = "⏳ 대기"
                stateText.setTextColor(getStateColor(updateState))
                updateIcon.visibility = View.INVISIBLE
            }
            UpdateState.SUCCESS -> {
                stateText.text = "✅ 정상"
                stateText.setTextColor(getStateColor(updateState))
                updateIcon.visibility = View.VISIBLE
                updated = true
                marker.captionColor = getStateColor(updateState)
                Handler(Looper.getMainLooper()).postDelayed({
                    marker.captionColor = Color.BLACK
                    updateIcon.visibility = View.INVISIBLE
                }, 500)
            }
            UpdateState.FAIL -> {
                stateText.text = "❌ 실패"
                stateText.setTextColor(getStateColor(updateState))
                updateIcon.visibility = View.INVISIBLE
            }
            else -> {
                stateText.text = "⚠️ 오류"
                stateText.setTextColor(Color.YELLOW)
                updateIcon.visibility = View.INVISIBLE
            }
        }
    }

    private fun draw(locationData: LocationData) {
        val coords = createLatLngList(locationData.locationList)
        if (coords.isNotEmpty()) {
            drawPath(coords)
        } else {
            Toast.makeText(this, "좌표 데이터 없음", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLatLngList(locationList: MutableList<MutableMap<String, Any?>>?): MutableList<LatLng> {
        val coords = mutableListOf<LatLng>()
        if (locationList != null && locationList.isNotEmpty()) {
            val jsonArray = JSONArray(locationList)
            for (i in 0 until jsonArray.length()) {
                val location = jsonArray.getJSONObject(i)
                val latLng = LatLng(location.getDouble("latitude"), location.getDouble("longitude"))
                coords.add(latLng)
            }
        }
        return coords
    }

    @SuppressLint("ResourceAsColor")
    fun drawPath(cordList: MutableList<LatLng>) {
        withMap { nMap ->
            if (cordList.isNotEmpty()) {
                val firstLatLng = cordList[0]
                if (cordList.size > 2) {
                    setPathOverlay(cordList)
                } else {
                    pathOverlay.map = null
                }
                setMapMarker(firstLatLng.latitude, firstLatLng.longitude)
            }
        }
    }

    private fun setPathOverlay(cordList: MutableList<LatLng>) {
        pathOverlay.apply {
            coords = cordList
            color = Color.WHITE
            width = 10
            outlineWidth = 5
            outlineColor = Color.BLACK
            map = naverMap
        }
    }

    private fun setMapMarker(userLatitude: Double, userLongitude: Double) {
        withMap { nMap ->
            val initialLatLng = LatLng(userLatitude, userLongitude)
            if (isAutoFocus) {
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0)
                    .animate(CameraAnimation.Fly)
                nMap.moveCamera(cameraUpdate)
            }

            marker.apply {
                position = initialLatLng
                map = nMap
                captionColor = getStateColor(updateState)
                captionText = shareholderInfo.userName.ifEmpty { "익명의 유저" }

                if (shareholderInfo.imageUrl.isNotEmpty()) {
                    // 마커 이미지를 URL에서 로드하여 설정
                    ImageUtils.loadBitmapFromUrl(applicationContext, shareholderInfo.imageUrl) {
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

            updateUIBasedOnState()
        }
    }


    private fun withMap(action: (NaverMap) -> Unit) {
        mapView.getMapAsync { nMap ->
            naverMap = nMap
            action(nMap)
        }
    }


    private fun getStateColor(state: UpdateState): Int {
        return when (state) {
            UpdateState.DEFAULT -> getColor(R.color.disable)
            UpdateState.SUCCESS -> Color.GREEN
            UpdateState.FAIL -> Color.RED
            else -> Color.YELLOW
        }
    }


}