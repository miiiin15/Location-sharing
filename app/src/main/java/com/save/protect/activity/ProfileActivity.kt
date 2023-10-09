package com.save.protect.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.storage.StorageManager
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        editTextNickname = findViewById(R.id.editTextNickname)
        buttonRegister = findViewById(R.id.buttonRegister)

        auth = Firebase.auth
        uid = auth.currentUser?.uid


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
//            editTextNickname.text.let {
//                UserInfoManager.setUserInfo(it.toString(), imageUrl)
//            }

            if (imageUrl != null) {
                FirebaseStorageManager.uploadImageToFirebaseStorage(uid, imageUrl!!) {
                    Log.d("프로필", "${imageUrl}")
                }
            } else {
                Toast.makeText(this, "이미지를 갤러리에서 업로드 해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleActivityResult(requestCode, resultCode, data) {
            if (ImageUtils.isImageSizeValid(this, it)) {
                // 이미지 크기가 적절한 경우
                imageViewProfile.setImageURI(it)
                imageUrl = it
            } else {
                // 이미지 크기가 너무 크거나 오류 발생한 경우
                Toast.makeText(
                    this,
                    "이미지가 크거나 올바르지 않습니다. (1024 * 1024) 이하의 이미지 파일을 지정해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
}