package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.IsValidListener
import com.save.protect.data.UserManagement
import com.save.protect.data.auth.repo.SignInRepo
import com.save.protect.database.AuthManager
import com.save.protect.databinding.ActivitySigninBinding

class SigninActivity : BaseActivity() {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var sharedPref: SharedPreferences


    private lateinit var auth: FirebaseAuth

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
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loadingDialog.show(supportFragmentManager, "")
                AuthManager.loginFireBase(this, email, password,
                    onSuccess = {
                        // 로그인 성공
                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                        UserManagement.isGuest = false
                        if (isSave) {
                            setStringSharedPref()
                        }
                        loadingDialog.dismiss()
                        next()
                    },
                    onFailure = { errorMessage ->
                        // 로그인 실패 시 수행할 동작
                        loadingDialog.dismiss()
                        Toast.makeText(this, "로그인 실패 $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "아이디 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogInGuest.setOnClickListener {
            guestLogin()
        }

        binding.btnLogInTest.setOnClickListener {
            testLogin()
        }

        binding.btnJoin.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

    }

    private fun init() {

        auth = Firebase.auth
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

    private fun testLogin() {
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
                    if (isSave) {
                        setStringSharedPref()
                    }
                    loadingDialog.dismiss()
                    next()
                } else {
                    alertDlg(it.error.message.toString())
                }
            },
            failure = {
                loadingDialog.dismiss()
                alertDlg(it.message.toString())
            },
            networkFail = {
                loadingDialog.dismiss()
                alertDlg("$it : " + getString(R.string.network_error_message))
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
