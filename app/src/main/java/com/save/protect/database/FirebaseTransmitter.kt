package com.save.protect.database

import android.annotation.SuppressLint
import android.location.Location
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.save.protect.data.LocationData
import com.save.protect.data.MessageObject
import com.save.protect.helper.Logcat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseTransmitter {

    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore

    // 사용자가 제공한 문서 ID를 전달받는 함수
    fun sendLocationData(
        documentId: String,
        list: MutableList<Location>,
        userName: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        val dataList = mutableListOf<MutableMap<String, Any?>>()

        for (location in list) {
            // 각 위치 데이터를 Map 형식으로 변환
            val data = mutableMapOf<String, Any?>(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "altitude" to if (location.hasAltitude()) location.altitude else "" // 고도 정보가 없을 때 빈 문자열 처리
            )

            // 리스트에 추가
            dataList.add(data)
        }

        val currentDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val locationData = LocationData(
            locationList = dataList,
            date = currentDate,
            userName = userName
        )

        // Firestore에 특정 문서에 데이터를 전송 또는 업데이트합니다.
        db.collection("locations_test")
            .document(documentId)
            .set(locationData)
            .addOnSuccessListener {
                Logcat.d("위치 송신기 : 문서 업데이트 완료")
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                Logcat.e("위치 송신기 : 문서 업데이트 중 오류 발생 $e")
                onFailure.invoke()
            }
    }

    fun sendMessage(
        documentId: String,
        userId:String,
        message: String,
        userName: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {

        val currentDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val messageData = MessageObject(
            id = userId,
            message = message,
            date = currentDate,
            userName = userName
        )

        // Firestore에 특정 문서에 데이터를 전송 또는 업데이트합니다.
        db.collection("message_list")
            .document(documentId)
            .set(messageData)
            .addOnSuccessListener {
                Logcat.d("메시지 송신기 : 문서 업데이트 완료")
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                Logcat.e("메시지 송신기 : 문서 업데이트 중 오류 발생 $e")
                onFailure.invoke()
            }
    }


}
