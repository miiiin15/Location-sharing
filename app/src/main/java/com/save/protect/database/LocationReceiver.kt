package com.save.protect.database

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import com.save.protect.data.LocationData
import com.save.protect.helper.Logcat

object LocationReceiver {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    // 사용자가 입력한 문서 ID를 전달받는 함수
    fun getLocationData(documentId: String, listener: (LocationData) -> Unit) {
        db.collection("locations_test")
            .document(documentId) // 사용자가 입력한 문서 ID를 여기에 지정합니다.
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Logcat.e("위치 수신기 failed : $e")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val locationData = snapshot.toObject(LocationData::class.java)
                    locationData?.let { listener(it) }
                }
            }
    }

    // 스냅샷 리스너
    fun observeLocationData(documentId: String, listener: (LocationData) -> Unit) {
        val docRef = db.collection("locations_test").document(documentId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Logcat.w("위치 변화 탐지기 failed : $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Logcat.d("위치 변화 탐지기 : ${snapshot.data}")
                val locationData = snapshot.toObject(LocationData::class.java)
                locationData?.let { listener(it) }
            } else {
                Logcat.d("위치 변화 탐지기 : null")
            }
        }
    }

}
