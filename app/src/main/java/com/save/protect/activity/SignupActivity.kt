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

    private lateinit var emailInput: CustomInput
    private lateinit var passwordInput: CustomInput
    private lateinit var signUpButton: CustomButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        init()

        emailInput.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                validButton()
                return text.isNotEmpty()
            }
        })
        passwordInput.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                validButton()
                return text.isNotEmpty()
            }
        })

        signUpButton.setOnClickListener {
            loadingDialog.show(supportFragmentManager, "")

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

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
        emailInput = findViewById(R.id.signup_email_input)
        passwordInput = findViewById(R.id.signup_password_input)
        signUpButton = findViewById(R.id.signup_button)

        validButton()
    }

    private fun validButton() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        signUpButton.setEnable(email.isNotEmpty() && password.isNotEmpty())
    }

}
