package com.save.protect.data

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import kotlin.system.exitProcess

class DataProvider {
    companion object {
        // 로그인 여부, 앱 최초 실행 여부, 보안 코드 등 앱 데이터를 관리하는 클래스

        var isLogin = false
        var isFirst = true
        var securityCode = ""

        fun init(context: Context) {
            // 앱의 전역적인 context 설정
            Constants.context = context
        }

        fun getUsername(): String {
            // 토큰에서 사용자 이름을 추출하여 반환
            val token = getToken()
            try {
                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                val data = JSONObject(decode ?: "").getString("data")
                return JSONObject(data).getString("usertitle")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getFsbId(): String {
            // 토큰에서 FSB ID를 추출하여 반환
            val token = getToken()
            try {
                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                val data = JSONObject(decode ?: "").getString("data")
                return JSONObject(data).getString("fsbId")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getCustUId(): String {
            // 토큰에서 고객 고유 번호(sub)를 추출하여 반환
            val token = getToken()
            try {
                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                return JSONObject(decode ?: "").getString("sub")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getLogoutCheck(): String {
            // 토큰의 만료 시간(exp)을 반환
            val token = getToken()
            try {
                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                return JSONObject(decode ?: "").getString("exp")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        private fun getToken(): String? {
            // SharedPreferences에서 토큰을 가져옴
            val pref =
                Constants.context?.getSharedPreferences("dataPreferences", Context.MODE_PRIVATE)
            return pref?.getString("access-token", "")
        }

        private fun getJson(strEncoded: String): String {
            // Base64 디코딩 수행
            val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodedBytes, charset("UTF-8"))
        }

        fun unCaughtException(context: Activity, activity: Activity) {
            // 예외가 발생했을 때 앱을 재시작하는 핸들러 설정
            Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
                Log.e("Alert", "앱 예외 발생")

                try {
                    val intent = Intent(context, activity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)

                    context.finish()
                    exitProcess(2)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun alertDlg(msg: String, dialog: Dialog) {
            // TODO : 경고 다이얼로그 만들기
        }
    }
}