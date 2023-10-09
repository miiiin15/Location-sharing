package com.save.protect.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.save.protect.data.UserInfo


object UserInfoManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun setUserInfo(userName: String, imageUrl: String) {
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
                    // 필요한 처리를 수행하세요.
                }
                .addOnFailureListener { e ->
                    // 데이터 등록 실패
                    // 오류 처리를 수행하세요.
                }
        }
    }
}