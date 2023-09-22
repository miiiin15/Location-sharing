package com.save.protect

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

object LocationReceiver {

    private val db = FirebaseFirestore.getInstance()

    // 사용자가 입력한 문서 ID를 전달받는 함수
    fun observeLocationData(documentId: String, listener: (LocationData) -> Unit) {
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

}
