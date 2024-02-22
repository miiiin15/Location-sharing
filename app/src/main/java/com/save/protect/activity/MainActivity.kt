package com.save.protect.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.CustomInput
import com.save.protect.custom.IsValidListener
import com.save.protect.data.DocIdManagement
import com.save.protect.data.UserManagement
import com.save.protect.data.auth.repo.UserInfoRepo
import com.save.protect.databinding.ActivityMainBinding
import com.save.protect.util.ClipboardUtils

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        checkReceivedId()
        init()

        binding.buttonUserInfo.setOnClickListener {
            if (UserManagement.isGuest) {
                Toast.makeText(this, "비회원은 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        binding.buttonOpen.setOnClickListener {
            //긴급방 만들때 쓰기
            //val intent = Intent(this, ShareholderActivity::class.java)
            val intent = Intent(this, CustomShareholderActivity::class.java)

            startActivity(intent)
        }

        binding.buttonEnter.setOnClickListener {
            // 버튼을 누르면 다이얼로그를 띄웁니다.
            showInputDialog()
        }
    }


    private fun init() {
        fetchUserInfo()
    }

    private fun checkReceivedId() {
        val id = DocIdManagement.getReceivedId()
        if (id != null && id.isNotEmpty()) {
            Toast.makeText(this, "초대받은 세션으로 이동합니다.", Toast.LENGTH_SHORT).show()
            enterAudience(id)
        }
        return
    }

    private fun fetchUserInfo() {
        UserInfoRepo.getUserInfo(
            success = {
                loadingDialog.dismiss()
                if (it.status == "OK") {

                    UserManagement.setUserInfo(it.data)
                    loadingDialog.dismiss()
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

    private fun enterAudience(input: String) {
        val intent = Intent(this, AudienceActivity::class.java)
        intent.putExtra("doc_id", input)
        startActivity(intent)
    }

    @SuppressLint("MissingInflatedId")
    private fun showInputDialog() {
        val clipboardUtils = ClipboardUtils(this)

        val copiedText = clipboardUtils.getClipboardText()
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_audience, null)

        val editTextInvite = dialogView.findViewById<CustomInput>(R.id.editText_invite)
        val buttonPaste = dialogView.findViewById<Button>(R.id.button_paste)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_confirm)
        val buttonCancel = dialogView.findViewById<Button>(R.id.button_cancel)

        builder.setView(dialogView)
        val dialog = builder.create()

        editTextInvite.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                return text.isNotEmpty()
            }
        })

        buttonPaste.setOnClickListener {
            copiedText.let { editTextInvite.setText(it) }
        }

        buttonConfirm.setOnClickListener {
            val userInput = editTextInvite.text.toString()
            if (userInput.length in 1..40) {
                // 최대 40자 이내의 문자열이 입력된 경우에만 확인 버튼 동작 추가
                enterAudience(userInput)
                dialog.dismiss() // 다이얼로그 닫기
            } else {
                // 40자를 초과한 경우
            }
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기
        }

        dialog.show()
    }
}
