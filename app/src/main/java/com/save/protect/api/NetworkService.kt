package com.save.protect.api

import android.content.Context
import android.util.Log
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

    // API 서비스를 제공하는 함수
    fun getService(): ApiService = retrofit.create(ApiService::class.java)

    // HTTP 통신 로그를 출력하기 위한 인터셉터
    private val httpLoggingInterceptor = HttpLoggingInterceptor()

    // Retrofit 객체 초기화
    private val retrofit =
        Retrofit.Builder()
            .baseUrl(SERVER_URL) // 서버의 기본 URL 설정
            .client(provideOkHttpClient()) // OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱을 위한 GsonConverterFactory 추가
            .build()

    // OkHttpClient를 생성하고 설정하는 함수
    private fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .run {
            // 각종 인터셉터 및 타임아웃 설정
            addInterceptor(AddInterceptor()) // 요청에 Bearer Token 추가하는 인터셉터
            addInterceptor(ReceiveInterceptor()) // 응답을 처리하는 인터셉터
            addInterceptor(httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)) // HTTP 로그 인터셉터
            connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 연결 제한 시간 설정
            readTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 읽기 제한 시간 설정
            writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 쓰기 제한 시간 설정
            build() // OkHttpClient 객체 생성 및 반환
        }

    // 응답을 처리하는 Interceptor 클래스
    class ReceiveInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            // SharedPreferences에서 데이터를 가져옴
            var pref =
                Constants.context?.getSharedPreferences("dataPreferences", Context.MODE_PRIVATE)
            Logcat.d("ReceiveInterceptor pref:::" + pref.toString())

            // 요청을 실행하고 응답을 받음
            val original = chain.proceed(chain.request())

            try {
                // 응답 값을 로그에 출력
                Logcat.d(
                    "응답 값 : \n" +
                            GsonBuilder()
                                .setPrettyPrinting()
                                .create()
                                .toJson(
                                    Gson().fromJson(
                                        original.peekBody(Long.MAX_VALUE).string(),
                                        JsonObject::class.java
                                    )
                                )
                )
            } catch (e: Exception) {
                // 예외가 발생한 경우 로그에 출력
                Logcat.d("응답 에러 : " + e.message)
            }

            // Set-Cookie 헤더를 읽어와 SharedPreferences에 저장
            if (original.headers("Set-Cookie").isNotEmpty()) {
                val cookies = java.util.HashSet<String>()
                for (header in original.headers("Set-Cookie")) {
                    cookies.add(header)
                }
                var editor = pref?.edit()
                editor?.putStringSet("cookie", cookies)
                editor?.apply()
            }

            // 응답 헤더에서 토큰을 추출하여 SharedPreferences에 저장
            val jwtAccessToken = original.headers("access-token")
            val jwtRefreshToken = original.headers("refresh-token")
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

            return original // 수정된 응답 반환
        }
    }

    // 요청에 Bearer Token을 추가하는 Interceptor 클래스
    class AddInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
            // SharedPreferences에서 Access Token을 가져옴
            var pref =
                Constants.context?.getSharedPreferences("dataPreferences", Context.MODE_PRIVATE)
            val accessToken = pref?.getString("access-token", "")

            // 요청 빌더 생성 및 설정
            val builder = request().newBuilder()
            val preferences = pref?.getStringSet("cookie", HashSet())
            builder.removeHeader("Cookie")
            for (cookie in preferences!!) {
                builder.addHeader("Cookie", cookie)
            }

            // 로그인 상태인 경우 Bearer Token을 요청 헤더에 추가
            if (DataProvider.isLogin) {
                builder.addHeader("Authorization", "Bearer $accessToken")
            }
            builder.addHeader("User-Agent", "aos")

            proceed(builder.build()) // 수정된 요청 실행 및 반환
        }
    }
}
