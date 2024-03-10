package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.IsValidListener
import com.save.protect.data.DataProvider
import com.save.protect.data.UserManagement
import com.save.protect.data.auth.repo.SignInRepo
import com.save.protect.databinding.ActivitySigninBinding

class SigninActivity : BaseActivity() {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var sharedPref: SharedPreferences


    private var isSave = false


    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signin)

        init()

        // 외부 뷰 터치 시 키보드 내리기와 포커스 해제
        binding.outsideView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.outsideView.windowToken, 0)
                binding.editTextEmail.clearFocus()
                binding.editTextPassword.clearFocus()
            }
            true
        }


        binding.editTextEmail.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                validButton()
                return text.isNotEmpty()
            }
        })

        binding.editTextPassword.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                validButton()
                return text.isNotEmpty()
            }
        })

        binding.checkBoxEmailSave.setOnCheckedChangeListener { buttonView, isChecked ->
            isSave = isChecked
        }

        binding.btnLogin.setOnClickListener {
            login()
        }


        binding.btnJoin.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

    }

    private fun init() {
        sharedPref = this.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val email = sharedPref.getString("savedEmail", "")
        Log.d("이메일 저장된 값 ", ": $email")
        if (email != null && email.isNotEmpty()) {
            binding.editTextEmail.setText(email)
            binding.checkBoxEmailSave.isChecked = true
        }
        validButton()
    }

    // 이메일, 비번 검사
    private fun validButton() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        binding.btnLogin.setEnable(email.isNotEmpty() && password.isNotEmpty())
    }


    private fun login() {
        loadingDialog.show(supportFragmentManager, "")
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        SignInRepo.signIn(
            email,
            password,
            success = {
                loadingDialog.dismiss()
                if (it.status == "OK") {
                    Toast.makeText(this, it.data, Toast.LENGTH_SHORT).show()
                    UserManagement.isGuest = false
                    DataProvider.isLogin = true
                    if (isSave) {
                        setStringSharedPref()
                    }
                    loadingDialog.dismiss()
                    next()
                } else {
                    showAlert(it.error.message.toString())
                }
            },
            failure = {
                loadingDialog.dismiss()
                showAlert(it.message.toString())
            },
            networkFail = {
                loadingDialog.dismiss()
                showAlert("$it : " + getString(R.string.network_error_message))
            }
        )
    }

    // 내부에 이메일값 저장
    private fun setStringSharedPref() {
        try {
            val editor = sharedPref.edit()
            val email = binding.editTextEmail.text.toString().trim()
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
