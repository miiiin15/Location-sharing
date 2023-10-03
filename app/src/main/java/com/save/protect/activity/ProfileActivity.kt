package com.save.protect.activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var editTextNickname: EditText
    private lateinit var buttonRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // XML 레이아웃에서 위젯을 찾아 변수에 할당
        imageViewProfile = findViewById(R.id.imageViewProfile)
        editTextNickname = findViewById(R.id.editTextNickname)
        buttonRegister = findViewById(R.id.buttonRegister)

        // 'Register' 버튼 클릭 시 동작 구현
        buttonRegister.setOnClickListener {
            // 등록 또는 편집 로직을 여기에 구현
            // 사용자 닉네임 및 프로필 이미지 등을 처리
        }
    }
}