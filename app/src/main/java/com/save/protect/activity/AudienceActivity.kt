package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.ListenerRegistration
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.overlay.PolylineOverlay
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
import com.save.protect.databinding.ActivityAudienceBinding
import com.save.protect.util.ImageUtils
import com.save.protect.util.PermissionUtils
import org.json.JSONArray

class AudienceActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityAudienceBinding

    private var listenerRegistration: ListenerRegistration? = null
    private lateinit var docId: String
    private var isAutoFocus = true
    private var updateTime: String? = null
    private var updated = false

    private var updateState = UpdateState.DEFAULT
    private lateinit var audienceData: UserInfo
    private lateinit var audienceName: String
    private var audienceLatLng: LatLng? = null
    private lateinit var shareholderInfo: UserInfo
    private lateinit var firstLatLng: LatLng

    private lateinit var naverMap: NaverMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<BottomSheetChat>

    private var pathOverlay = PathOverlay()
    private var betweenLine = PolylineOverlay()
    private var shareholderMarker = Marker()
    private var audienceMarker = Marker()


    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            handler.postDelayed(this, 60000)
            binding.timeTextView.text = "⏱️ " + getTimeDifference(updateTime)
            updateTime = updateTime

            if (updated) {
                updateState = UpdateState.DEFAULT
                updateUIBasedOnState()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudienceBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.audienceMapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        binding.audienceMapView.onStart()
        docId.let {
            FirebaseReceiver.observeLocationData(it,
                listener = { locationData ->
                    updateState = UpdateState.SUCCESS
                    updateTime = locationData.date
                    binding.timeTextView.text = "⏱️ " + getTimeDifference(locationData.date)
                    draw(locationData)
                    updateUIBasedOnState()
                },
                onFailure = {
                    updateState = UpdateState.FAIL
                    binding.timeTextView.text = "⏱️ 업데이트 없음"
                    updateUIBasedOnState()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.audienceMapView.onResume()
        docId.let { fetchLocationData(it) }
        startTask()
    }

    override fun onPause() {
        super.onPause()
        binding.audienceMapView.onPause()
        stopTask()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.audienceMapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.audienceMapView.onStop()
        listenerRegistration?.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.audienceMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.audienceMapView.onLowMemory()
    }

    private fun startTask() {
        handler.post(runnable)
    }

    private fun stopTask() {
        handler.removeCallbacks(runnable)
    }

    private fun setInitialValues() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.audienceChat)
        audienceData = UserManagement.getUserInfo()!!
        audienceName =
            if (audienceData.userName.isNullOrBlank()) audienceData.userName else "익명의 유저"

        docId.let {
            FirebaseReceiver.observeMessage(
                it,
                listener = { messageData ->
                    if (getTimeDifference(messageData.date) === "방금 전" && messageData.id != UserManagement.uid) {
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
                        binding.audienceChat.setTitle("입력창 닫으려면 내리기")
                        binding.audienceChat.moveFocusInput()
                        showKeyboard()
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.audienceChat.setTitle("메시지 보내려면 올리기")
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        closeKeyboard()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        binding.audienceChat.setButtonListener(object : ButtonListener {
            override fun callback() {
                val message: String = binding.audienceChat.getMessage()
                if (UserManagement.uid.isNullOrBlank()) {
                    return
                }
                if (binding.audienceChat.getMessage().isNullOrBlank()) {
                    return
                }
                getCurrentLocation {
                    FirebaseTransmitter.sendMessage(
                        documentId = docId,
                        userId = UserManagement.uid!!,
                        latitude = it?.latitude,
                        longitude = it?.longitude,
                        message = message,
                        userName = audienceData.userName,
                        onSuccess = {},
                        onFailure = {}
                    )
                }


            }
        }
        )

        binding.refreshButton.setOnClickListener {
            docId.let { fetchLocationData(it) }
        }

        binding.locationButton.setOnClickListener {
            getCurrentLocation {
                setMapMarker(
                    it?.latitude,
                    it?.longitude,
                    audienceData,
                    true
                )
            }
        }

        binding.autoFocusAudienceCheckBox.setOnCheckedChangeListener { _, isChecked ->
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
                updateTime = locationData.date
                binding.timeTextView.text = "⏱️ " + getTimeDifference(locationData.date)
                draw(locationData)
                updateUIBasedOnState()
            },
            onFailure = {
                updateState = UpdateState.FAIL
                binding.timeTextView.text = "⏱️ 업데이트 없음"
                updateUIBasedOnState()
            }
        )
    }

    private fun fetchUserInfo(id: String = "") {
        UserInfoManager.getUserInfo(id) { shareholderInfo = it }
    }

    private fun updateUIBasedOnState() {
        val (text, textColor, iconVisibility) = when (updateState) {
            UpdateState.DEFAULT -> Triple("⏳ 대기", getStateColor(updateState), View.INVISIBLE)
            UpdateState.SUCCESS -> Triple("✅ 정상", getStateColor(updateState), View.VISIBLE)
            UpdateState.FAIL -> Triple("❌ 실패", getStateColor(updateState), View.INVISIBLE)
            else -> Triple("⚠️ 오류", Color.YELLOW, View.INVISIBLE)
        }

        // 상태 텍스트와 색상 설정
        binding.stateTextView.text = text
        binding.stateTextView.setTextColor(textColor)


        // 아이콘 업데이트 상태 설정
        binding.updateIconTextView.visibility = iconVisibility

        // 추가 상태별 작업
        if (updateState == UpdateState.SUCCESS) {
            updated = true
            shareholderMarker.captionColor = textColor
            Handler(Looper.getMainLooper()).postDelayed({
                shareholderMarker.captionColor = Color.BLACK
                binding.updateIconTextView.visibility = View.INVISIBLE
            }, 500)
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
                firstLatLng = cordList[0]
                if (cordList.size > 2) {
                    setPathOverlay(cordList)
                } else {
                    pathOverlay.map = null
                }

                audienceLatLng?.let { setBetweenPath(naverMap, it, firstLatLng) }
                setMapMarker(
                    firstLatLng.latitude,
                    firstLatLng.longitude,
                    shareholderInfo
                )
            }
        }
    }

    private fun setPathOverlay(cordList: MutableList<LatLng>) {
        pathOverlay.apply {
            coords = cordList
            color = getColor(R.color.primary)
            width = 10
            outlineWidth = 3
            outlineColor = getColor(R.color.black)
            map = naverMap
            progress = (1 - (1.0 / cordList.size)) * -1;
            passedColor = getColor(R.color.default_color)
            passedOutlineColor = getColor(R.color.disable)
        }
    }

    private fun setMapMarker(
        userLatitude: Double?,
        userLongitude: Double?,
        userInfo: UserInfo,
        isAudience: Boolean = false
    ) {
        val marker = if (isAudience) audienceMarker else shareholderMarker

        withMap { nMap ->
            val initialLatLng = LatLng(userLatitude!!, userLongitude!!)
            if (isAutoFocus) {
                val cameraUpdate = CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0)
                    .animate(CameraAnimation.Fly)
                nMap.moveCamera(cameraUpdate)
            }
            val borderColor = if (isAudience) getColor(R.color.secondary) else Color.BLACK

            marker.apply {
                position = initialLatLng
                map = nMap
                captionColor =
                    if (isAudience) getColor(R.color.secondary) else getStateColor(updateState)
                captionText = userInfo.userName.ifEmpty { "익명의 유저" }

                if (userInfo.imageUrl.isNotEmpty()) {
                    // 마커 이미지를 URL에서 로드하여 설정
                    ImageUtils.loadBitmapFromUrl(applicationContext, userInfo.imageUrl) {
                        marker.icon = OverlayImage.fromBitmap(
                            // 이미지 리사이징
                            ImageUtils.resizeAndCropToCircle(it!!, 100, 100, 3, borderColor)
                        )
                    }
                } else {
                    marker.icon = MarkerIcons.BLACK
                    marker.iconTintColor = Color.LTGRAY
                }
            }

            if (!isAudience) updateUIBasedOnState()
        }
    }


    private fun withMap(action: (NaverMap) -> Unit) {
        binding.audienceMapView.getMapAsync { nMap ->
            naverMap = nMap
            action(nMap)
        }
    }

    private fun setBetweenPath(
        naverMap: NaverMap,
        startLatLng: LatLng,
        endLatLng: LatLng
    ) {
        betweenLine.coords = listOf(startLatLng, endLatLng)

        betweenLine.color = getColor(R.color.error)
        betweenLine.width = 5

        betweenLine.map = naverMap
    }


    fun getCurrentLocation(callback: (Location?) -> Unit) {
        checkLocationPermission()

        fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                audienceLatLng = LatLng(task.result.latitude, task.result.longitude)
                setBetweenPath(naverMap, audienceLatLng!!, firstLatLng)
                callback(task.result) // 위치 반환
            } else {
                callback(null) // 위치를 가져올 수 없는 경우
            }
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