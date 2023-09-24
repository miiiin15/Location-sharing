package com.save.protect.database

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.save.protect.data.LocationData
import java.text.SimpleDateFormat
import java.util.*

object LocationTransmitter {

    // Firestore 초기화
    private val db = Firebase.firestore

    // 사용자가 제공한 문서 ID를 전달받는 함수
    fun sendLocationData(documentId: String, list: MutableList<Any>) {
        val currentDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val locationData = LocationData(
            locationList = list,
            date = currentDate,
            userName = "유저 *23#"
        )

        // Firestore에 특정 문서에 데이터를 전송 또는 업데이트합니다.
        db.collection("locations_test")
            .document(documentId) // 전달받은 문서 ID를 사용합니다.
            .set(locationData) // set()을 사용하여 문서를 업데이트하거나 생성합니다.
            .addOnSuccessListener {
                Log.d("위치 송신기", "문서 업데이트 완료: $documentId")
            }
            .addOnFailureListener { e ->
                Log.e("위치 송신기", "문서 업데이트 중 오류 발생", e)
            }
    }


}
