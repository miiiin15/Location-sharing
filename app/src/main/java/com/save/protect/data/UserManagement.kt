package com.save.protect.data

class UserManagement {
    companion object {

        private var userInfo: UserInfo = UserInfo()
        var uid: String = ""

        fun resetUserInfo() {
            userInfo = UserInfo()
            uid = ""
        }

        // 사용자 정보 설정
        fun setUserInfo(userData: UserInfo) {
            userInfo =
                UserInfo(userData.email, userData.userName, userData.imageUrl, userData.pushToken)
        }

        // 사용자 정보 설정
        fun setUserInfo(email: String, userName: String, imageUrl: String, pushToken: String) {
            userInfo = UserInfo(email, userName, imageUrl, pushToken)
        }

        // 사용자 정보 반환
        fun getUserInfo(): UserInfo? {
            return userInfo
        }

    }
}