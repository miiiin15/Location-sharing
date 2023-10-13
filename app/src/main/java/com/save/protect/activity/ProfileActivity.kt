package com.save.protect.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.save.protect.R
import com.save.protect.data.UserInfo
import com.save.protect.data.UserManagement
import com.save.protect.database.FirebaseStorageManager
import com.save.protect.database.UserInfoManager
import com.save.protect.util.ImageUtils

class ProfileActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var editTextNickname: EditText
    private lateinit var buttonRegister: Button
    private var imageUrl: Uri? = null
    private lateinit var auth: FirebaseAuth

    private var uid: String? = null

    private var userData: UserInfo? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        editTextNickname = findViewById(R.id.editTextNickname)
        buttonRegister = findViewById(R.id.buttonRegister)

        init()

        imageViewProfile.setOnClickListener {
            try {
                ImageUtils.selectImageFromGallery(this) {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        buttonRegister.setOnClickListener {

            if (imageUrl != null) {
                FirebaseStorageManager.uploadImageToFirebaseStorage(uid, imageUrl!!) {
                    val URL = it
                    Log.d("다운로드 URL", " : ${URL}")
                    editTextNickname.text.let {
                        UserInfoManager.setUserInfo(it.toString(), URL.toString())
                        // TODO : 클로저로 바꾸기
                        Toast.makeText(this, "등록 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "이미지를 갤러리에서 업로드 해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        uid = UserManagement.uid
        userData = UserManagement.getUserInfo()

        Log.e("저장된유저 ", "${UserManagement.getUserInfo()}")
        Log.e("저장된유저 ", UserManagement.uid)

        if (userData?.userName?.isNotEmpty()!!) {
            editTextNickname.setText(userData!!.userName)
        }
        if (userData?.imageUrl?.isNotEmpty()!!) {
            ImageUtils.loadBitmapFromUrl(this, userData?.imageUrl!!) {
                imageViewProfile.setImageBitmap(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleActivityResult(requestCode, resultCode, data) {
            imageViewProfile.setImageBitmap(ImageUtils.loadImageAndResize(this, it, 500))
            imageUrl = it
        }
    }
}