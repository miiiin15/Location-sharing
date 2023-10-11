package com.save.protect.activity

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.R
import com.save.protect.database.AuthManager

class SignupActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var btnCheckDuplicate: Button
    private lateinit var btnSignUp: Button
    private lateinit var tvDuplicateStatus: TextView
    private var isEmailDuplicate = false // 이메일 중복 체크 상태를 나타내는 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        btnCheckDuplicate = findViewById(R.id.btnCheckDuplicate)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvDuplicateStatus = findViewById(R.id.tvDuplicateStatus)

        btnCheckDuplicate.setOnClickListener {
            val email = editTextEmail.text.toString()
            // 이메일 중복 확인 로직
            // 중복되면 isEmailDuplicate 변수를 true로 설정하고, tvDuplicateStatus에 메시지를 표시

            try {

                AuthManager.checkEmailDuplication(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result = task.result // 중복 확인 결과

                        Log.d("중복", "${result}")

//                    if (result?.signInMethods?.isEmpty() == true) {
//                        // 이메일이 중복되지 않음
//                        // 사용 가능한 이메일 주소
//                    } else {
//                        // 이메일이 이미 등록되어 있음
//                        // 중복된 이메일 주소
//                    }
                    } else {
                        // 중복 확인 작업 실패
                        val exception = task.exception
                        // 예외 처리
                        Log.d("실패", "${exception?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d("fail", "${e}")
            }


        }

        btnSignUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                // 이메일 또는 비밀번호가 비어 있거나 이메일 중복 확인이 되지 않은 경우
                Toast.makeText(this, "이메일 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            } else {
                AuthManager.signUpFireBase(email, password) {
                    Toast.makeText(this, "회원가입 성공 로그인을 실행해주세요.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

}
