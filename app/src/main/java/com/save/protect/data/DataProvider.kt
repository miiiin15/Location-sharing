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

        var isLogin = false
        var isFirst = true
        var securityCode = ""

        fun init(context: Context) {
            Constants.context = context
        }

        fun getUsername(): String {
            val token = getToken()
            try {
//                val split = token?.let { JWTUtils.decoded(it) }
//                val data = JSONObject(split).getString("data")
//                val username = JSONObject(data).getString("usertitle")

                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                val data = JSONObject(decode ?: "").getString("data")
                val username = JSONObject(data).getString("usertitle")


//                val split = token?.split("\\.".toRegex())?.toTypedArray()
//                val data = JSONObject(split?.get(1)).getString("data")
//                val username = JSONObject(data).getString("usertitle")
                return username
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getFsbId(): String {
            val token = getToken()
            try {
//                val split = token?.let { JWTUtils.decoded(it) }
//                val data = JSONObject(split).getString("data")
//                val username = JSONObject(data).getString("usertitle")

                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                val data = JSONObject(decode ?: "").getString("data")
                val fsbId = JSONObject(data).getString("fsbId")

                println("토큰데이터:::$data")


//                val split = token?.split("\\.".toRegex())?.toTypedArray()
//                val data = JSONObject(split?.get(1)).getString("data")
//                val username = JSONObject(data).getString("usertitle")
                return fsbId
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getCustUId(): String {
            val token = getToken()
            try {
//                val split = token?.let { JWTUtils.decoded(it) }
//                val data = JSONObject(split).getString("data")
//                val username = JSONObject(data).getString("usertitle")

                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
                val custUid = JSONObject(decode ?: "").getString("sub")

                println("고객고유번호:::$custUid")


//                val split = token?.split("\\.".toRegex())?.toTypedArray()
//                val data = JSONObject(split?.get(1)).getString("data")
//                val username = JSONObject(data).getString("usertitle")
                return custUid
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getLogoutCheck(): String {
            val token = getToken()
            try {

                val split = token?.split("\\.".toRegex())?.toTypedArray()
                val decode = split?.get(1)?.let { getJson(it) }
//                Logcat.d("getLogoutCheck exp : ${JSONObject(decode?: "").getString("exp")}")

                var expTime = JSONObject(decode ?: "").getString("exp")

                // 테스트용 (토큰만료시각에서 15분 빼기)
//                expTime = (Date(expTime.toLong() * 1000 - 13 * 60 * 1000).time / 1000).toString()

                return expTime

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }


        private fun getToken(): String? {
            var pref =
                Constants.context?.getSharedPreferences("dataPreferences", Context.MODE_PRIVATE)
            return pref?.getString("access-token", "")
        }

        private fun getJson(strEncoded: String): String {
            val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodedBytes, charset("UTF-8"))
        }

        fun unCaughtException(context: Activity, activity: Activity) {
            Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
                Log.e("Alert", "Lets See if it Works !!!")

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

        }
    }
}