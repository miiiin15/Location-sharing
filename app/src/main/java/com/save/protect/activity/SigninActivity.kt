package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.CustomButton
import com.save.protect.custom.CustomInput
import com.save.protect.custom.IsValidListener
import com.save.protect.data.UserManagement
import com.save.protect.database.AuthManager

class SigninActivity : BaseActivity() {

    private lateinit var sharedPref: SharedPreferences

    private lateinit var outsideView: View
    private lateinit var editTextEmail: CustomInput
    private lateinit var editTextPassword: CustomInput

    private lateinit var btnLogin: CustomButton
    private lateinit var btnSignUp: Button
    private lateinit var btnGuestLogin: Button
    private lateinit var checkBoxSave: CheckBox

    private lateinit var auth: FirebaseAuth

    private var isSave = false


    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        init()

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

        checkBoxSave.setOnCheckedChangeListener { buttonView, isChecked ->
            isSave = isChecked
        }

        btnLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                loadingDialog.show(supportFragmentManager, "")
                AuthManager.loginFireBase(this, email, password) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    UserManagement.isGuest = false
                    if (isSave) {
                        setStringSharedPref()
                    }
                    loadingDialog.dismiss()
                    next()
                }
            } else {
                Toast.makeText(this, "아이디 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        btnGuestLogin.setOnClickListener {
            guestLogin()
        }
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

    }

    private fun init() {
        outsideView = findViewById(R.id.outsideView)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        checkBoxSave = findViewById(R.id.checkBox_emailSave)

        btnLogin = findViewById(R.id.btnLogin)
        btnGuestLogin = findViewById(R.id.btnLogInGuest)
        btnSignUp = findViewById(R.id.btnJoin)

        auth = Firebase.auth
        sharedPref = this.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)

        val email = sharedPref.getString("savedEmail", "")
        Log.d("이메일 저장된 값 ", ": $email")
        if (email != null && email.isNotEmpty()) {
            editTextEmail.setText(email)
            checkBoxSave.isChecked = true
        }
        validButton()
    }

    // 이메일, 비번 검사
    private fun validButton() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        btnLogin.setEnable(email.isNotEmpty() && password.isNotEmpty())
    }

    private fun guestLogin() {
        loadingDialog.show(supportFragmentManager, "")
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val handler = Handler()
                    handler.postDelayed({
                        next()
                        loadingDialog.dismiss()
                        Toast.makeText(this, "비회원 로그인 성공", Toast.LENGTH_SHORT).show()
                    }, 2000)
                } else {
                    Log.w("로그인", "signInAnonymously:failure", task.exception)
                    loadingDialog.dismiss()
                    Toast.makeText(
                        baseContext,
                        "로그인 실패",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    // 내부에 이메일값 저장
    private fun setStringSharedPref() {
        try {
            val editor = sharedPref.edit()
            val email = editTextEmail.text.toString().trim()
            editor.putString("savedEmail", email)
            editor.apply()
            Log.d("이메일 저장 ", "성공 : $email")
        } catch (e: Exception) {
            Log.e("이메일 저장 ", "실패 ", e)
        }
    }

    private fun next() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
