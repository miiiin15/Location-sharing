package com.save.protect

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.KakaoSdk.keyHash
import com.save.protect.data.DataProvider


class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        // 앱 초기화 작업 수행
        DataProvider.init(this)

        // 라이트 모드 강제
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Kakao SDK 초기화
        KakaoSdk.init(this, resources.getString(R.string.kakao_native_key))
        val kakaoHashKey = keyHash
        Log.d("kakaoHashKey", kakaoHashKey)
    }

    companion object {
        // 다른 메서드나 변수를 추가하여 앱 전체에서 사용할 수 있는 데이터나 기능을 구현할 수 있습니다.
        var instance: GlobalApplication? = null
            private set
    }
}
