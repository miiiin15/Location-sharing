package com.save.protect.activity

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.CustomButton
import com.save.protect.custom.CustomInput
import com.save.protect.custom.IsValidListener
import com.save.protect.database.AuthManager

class SignupActivity : BaseActivity() {

    private lateinit var editTextEmail: CustomInput
    private lateinit var editTextPassword: CustomInput
    private lateinit var btnSignUp: CustomButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        init()

        editTextEmail.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                validButton()
                return text.isNotEmpty()
            }
        })
        editTextPassword.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                validButton()
                return text.isNotEmpty()
            }
        })

        btnSignUp.setOnClickListener {
            loadingDialog.show(supportFragmentManager, "")

            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                loadingDialog.dismiss()
                Toast.makeText(this, "이메일 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            } else {
                AuthManager.signUpFireBase(this, email, password,
                    onSuccess = {
                        loadingDialog.dismiss()
                        finish()
                    },
                    onFailure = {
                        loadingDialog.dismiss()
                    }
                )
            }
        }
    }

    private fun init() {
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        btnSignUp = findViewById(R.id.btnSignUp)

        validButton()
    }

    private fun validButton() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        btnSignUp.setEnable(email.isNotEmpty() && password.isNotEmpty())
    }

}
