package com.save.protect.database

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object AuthManager {

    private val auth = Firebase.auth

    // 회원가입
    fun signUpFireBase(context: Context,email: String?, password: String?, onSuccess: () -> Unit,onFailure: (String) -> Unit) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 회원 가입 성공
                    Toast.makeText(context, "회원가입 성공 로그인을 실행해주세요.", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    // 회원 가입 실패
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        val errorMessage = "Firebase Authentication 오류: ${exception.errorCode}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onFailure(errorMessage)
                    } else {
                        val errorMessage = "로그인 실패: ${exception?.message}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onFailure(errorMessage)
                    }
                }
            }
    }

    fun loginFireBase(
        context: Context,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    // 로그인 실패
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        val errorMessage = "Firebase Authentication 오류: ${exception.errorCode}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onFailure(errorMessage)
                    } else {
                        // 기타 오류 처리
                        val errorMessage = "로그인 실패: ${exception?.message}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onFailure(errorMessage)
                    }
                }
            }
    }

}
