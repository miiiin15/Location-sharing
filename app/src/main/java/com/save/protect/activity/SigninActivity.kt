package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.R
import com.save.protect.custom.CustomInput
import com.save.protect.database.AuthManager

class SigninActivity : AppCompatActivity() {

    private lateinit var outsideView: View
    private lateinit var editTextEmail: CustomInput
    private lateinit var editTextPassword: CustomInput

    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button


    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        outsideView = findViewById(R.id.outsideView)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnJoin)

        // 외부 뷰 터치 시 키보드 내리기와 포커스 해제
        outsideView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(outsideView.windowToken, 0)
                editTextEmail.clearFocus()
                editTextPassword.clearFocus()
            }
            true
        }

        btnLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthManager.loginFireBase(this, email, password) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "아이디 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }


        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
