package com.save.protect.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.data.UserInfo
import com.save.protect.data.UserManagement
import com.save.protect.database.FirebaseStorageManager
import com.save.protect.database.UserInfoManager
import com.save.protect.databinding.ActivityProfileBinding
import com.save.protect.util.ImageUtils

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var userData: UserInfo = UserInfo()

    private var imageUrl: Uri? = null
    private lateinit var auth: FirebaseAuth

    private var uid: String? = null

    private val markSize = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)


        init()

        binding.profileImageView.setOnClickListener {
            try {
                ImageUtils.selectImageFromGallery(this) {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {

            if (imageUrl != null) {
                loadingDialog.show(supportFragmentManager, "")
                FirebaseStorageManager.uploadImageToFirebaseStorage(uid, imageUrl!!) {
                    val URL = it
                    binding.nicknameInput.text.let {
                        UserInfoManager.setUserInfo(it.toString(), URL.toString()) {
                            Toast.makeText(this, "등록 성공", Toast.LENGTH_SHORT).show()
                            loadingDialog.dismiss()
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "이미지를 갤러리에서 업로드 해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init() {
        loadingDialog.show(supportFragmentManager, "")
        uid = UserManagement.uid
        userData = UserManagement.getUserInfo()!!

        binding.user = userData
        if (userData?.imageUrl?.isNotEmpty()!!) {
            ImageUtils.loadBitmapFromUrl(this, userData?.imageUrl!!) {
                binding.profileImageView.setImageBitmap(it)
            }
            ImageUtils.loadBitmapFromUrl(this, userData.imageUrl) {
                binding.previewImageView.visibility = View.VISIBLE
                binding.previewImageView.setImageBitmap(
                    ImageUtils.resizeAndCropToCircle(it!!, markSize, markSize, 3, Color.BLACK)
                )
            }
        }
        loadingDialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.handleActivityResult(requestCode, resultCode, data) {
            binding.profileImageView.setImageBitmap(ImageUtils.loadImageAndResize(this, it, 1000))
            imageUrl = it
            binding.previewImageView.visibility = View.VISIBLE
            binding.previewImageView.setImageBitmap(
                ImageUtils.resizeAndCropToCircle(this, it, markSize, markSize, 3, Color.BLACK)
            )
        }

    }
}