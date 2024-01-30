package com.save.protect.database

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.save.protect.data.UserInfo
import com.save.protect.helper.Logcat


object UserInfoManager {
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun setUserInfo(userName: String, imageUrl: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            val userInfo = UserInfo(currentUser.email ?: "", userName, imageUrl)

            // 'userInfo' 컬렉션에 UID를 문서 ID로 사용하여 데이터 등록
            db.collection("userInfo")
                .document(uid)
                .set(userInfo)
                .addOnSuccessListener {
                    // 데이터 등록 성공
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    // TODO : 데이터 등록 실패 처리
                }
        }
    }

    // 사용자가 입력한 문서 ID를 전달받는 함수
    fun getUserInfo(documentId: String, listener: (UserInfo) -> Unit) {
        db.collection("userInfo")
            .document(documentId) // 사용자가 입력한 문서 ID를 여기에 지정합니다.
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Logcat.e("유저 정보 조회 failed : e$")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val userInfo = snapshot.toObject(UserInfo::class.java)
                    userInfo?.let { listener(it) }
                }
            }
    }
}