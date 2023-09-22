package com.save.protect

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.KakaoSdk.keyHash


class GlobalApplication : Application() {
    //    private lateinit var auth: FirebaseAuth
    override fun onCreate() {
        super.onCreate()
        instance = this

        // 앱 초기화 작업 수행

        // Initialize Firebase Auth
//        auth = Firebase.auth

        // Kakao SDK 초기화
        KakaoSdk.init(this, resources.getString(R.string.kakao_native_key))
//        KakaoSdk.init(this, "27eee38f87e06c631291470021c6af19")

        val kakaoHashKey = keyHash
        Log.d("kakaoHashKey", kakaoHashKey)
    }

    companion object {
        // 다른 메서드나 변수를 추가하여 앱 전체에서 사용할 수 있는 데이터나 기능을 구현할 수 있습니다.
        var instance: GlobalApplication? = null
            private set
    }
}
