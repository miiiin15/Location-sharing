package com.save.protect.database

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Task

object AuthManager {

    private val auth = Firebase.auth

    // id 중복확인
    fun checkEmailDuplication(email: String): Task<*> {
        val auth = FirebaseAuth.getInstance()

        // Firebase Authentication을 사용하여 이메일 중복 확인
        return auth.fetchSignInMethodsForEmail(email)
    }

    // 회원가입
    fun signUpFirebase(email: String?, password: String?, onSuccess: () -> Unit) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            // 이메일 또는 비밀번호가 빈 문자열 또는 null인 경우
            Log.d("회원가입", "이메일 또는 비밀번호를 확인하세요.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 회원 가입 성공
                    Log.d("회원가입", "회원 가입 성공")
                    onSuccess()
                } else {
                    // 회원 가입 실패
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        Log.d("회원가입", "Firebase Authentication 오류: ${exception.errorCode}")
                    } else {
                        // 기타 오류 처리
                        Log.d("회원가입", "회원 가입 실패: ${exception?.message}")
                    }
                }
            }
    }

}
