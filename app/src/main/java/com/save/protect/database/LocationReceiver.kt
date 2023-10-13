package com.save.protect.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.save.protect.data.LocationData

object LocationReceiver {

    private val db = FirebaseFirestore.getInstance()

    // 사용자가 입력한 문서 ID를 전달받는 함수
    fun getLocationData(documentId: String, listener: (LocationData) -> Unit) {
        db.collection("locations_test")
            .document(documentId) // 사용자가 입력한 문서 ID를 여기에 지정합니다.
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("위치 수신기", "Listen failed.", e)
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
                Log.w("위치 변화 탐지기", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("위치 변화 탐지기", "Current data: ${snapshot.data}")
                val locationData = snapshot.toObject(LocationData::class.java)
                locationData?.let { listener(it) }
            } else {
                Log.d("위치 변화 탐지기", "Current data: null")
            }
        }
    }

}
