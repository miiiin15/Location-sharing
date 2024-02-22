package com.save.protect.data

import android.util.Log
import com.save.protect.helper.Logcat

class UserManagement {
    companion object {

        private var userInfo: UserInfo = UserInfo()
        var isGuest = true

        fun resetUserInfo() {
            userInfo = UserInfo()
            isGuest = true
        }

        // 사용자 정보 설정
        fun setUserInfo(userData: UserInfo) {
            userInfo = userData
            Logcat.d("userInfo : $userInfo")
            Logcat.d("isGuest : $isGuest")
        }

        // 사용자 정보 반환
        fun getUserInfo(): UserInfo? {
            return userInfo
        }

    }
}