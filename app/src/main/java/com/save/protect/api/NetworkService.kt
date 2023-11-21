package com.save.protect.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.save.protect.data.Constants.SERVER_URL
import com.save.protect.data.Constants
import com.save.protect.data.DataProvider
import com.save.protect.helper.Logcat
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object NetworkService {


    fun getService(): ApiService = retrofit.create(ApiService::class.java)

    private val httpLoggingInterceptor = HttpLoggingInterceptor()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(SERVER_URL) // 도메인 주소
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    private fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .run {
            addInterceptor(AddInterceptor())
            addInterceptor(ReceiveInterceptor())
            addInterceptor(httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC))
            connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            build()
        }

    class ReceiveInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var pref =
                Constants.context?.getSharedPreferences("dataPreferences", Context.MODE_PRIVATE)
            val original = chain.proceed(chain.request())

            try {
                Logcat.d("related request: " + original.request.url.toString())


                // https://m.blog.naver.com/PostView.nhn?blogId=bluecrossing&logNo=221463228194&proxyReferer=https:%2F%2Fwww.google.com%2F
                // original.body.string 은 한번만 호출해야 한다고 한다...
                Logcat.d(
                    "raw response: \n" +
                            GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(
//                                Gson().fromJson(original.body?.string(), JsonObject::class.java)
                                    Gson().fromJson(
                                        original.peekBody(Long.MAX_VALUE).string(),
                                        JsonObject::class.java
                                    )
                                )
                )
                // double quote escaped jsonString: { \"name\": \"Baeldung\", \"java\": true }
                // jsonString: { "name": "Baeldung", "java": true }


            } catch (e: Exception) {

            }


            if (original.headers("Set-Cookie").isNotEmpty()) {
                val cookies = java.util.HashSet<String>()

                for (header in original.headers("Set-Cookie")) {
                    cookies.add(header)
                }
                var editor = pref?.edit()
                editor?.putStringSet("cookie", cookies)
                editor?.apply()
            }

            val jwtAccessToken = original.headers("access-token")
            val jwtRefreshToken = original.headers("refresh-token")
            Logcat.d("jwtAccessToken : $jwtAccessToken")
            Logcat.d("jwtRefreshToken : $jwtRefreshToken")
            if (jwtAccessToken.isNotEmpty()) {
                for (token in jwtAccessToken) {
                    pref?.edit()?.putString("access-token", token)?.apply()
                }
            }

            if (jwtRefreshToken.isNotEmpty()) {
                for (token in jwtRefreshToken) {
                    pref?.edit()?.putString("refresh-token", token)?.apply()
                }
            }
            return original
        }
    }

    class AddInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {

            var pref =
                Constants.context?.getSharedPreferences("dataPreferences", Context.MODE_PRIVATE)
            val accessToken = pref?.getString("access-token", "")

            Logcat.d("accessToken:::" + accessToken.toString())

            val builder = request().newBuilder()
            val preferences = pref?.getStringSet("cookie", HashSet())

            builder.removeHeader("Cookie")
            for (cookie in preferences!!) {
                builder.addHeader("Cookie", cookie)
            }

            if (DataProvider.isLogin) {
                builder.addHeader("Authorization-jwt", "Bearer $accessToken")
            }
            builder.addHeader("User-Agent", "aos")


            proceed(builder.build())
        }
    }

}
