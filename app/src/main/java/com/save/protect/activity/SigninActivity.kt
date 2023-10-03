package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.R
import com.save.protect.custom.CustomInput
import com.save.protect.database.AuthManager

class SigninActivity : AppCompatActivity() {

    private lateinit var editTextEmail: CustomInput
    private lateinit var editTextPassword: CustomInput

    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnJoin)

        btnLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthManager.loginFireBase(this, email, password) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,MainActivity::class.java)
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
