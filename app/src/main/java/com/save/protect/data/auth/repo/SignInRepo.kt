package com.save.protect.data.auth.repo

import android.util.Log
import com.save.protect.api.NetworkService
import com.save.protect.helper.Logcat
import kr.co.essb.app.data.api.response.ApiResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SignInRepo {

    fun signIn(
        email: String,
        password: String,
        networkFail: (String) -> Unit,
        success: (ApiResponse<String>) -> Unit,
        failure: (Throwable) -> Unit
    ) {

        Logcat.d("$email $password")

        // JSON 형식의 요청 데이터 생성
        val jsonBody = "{\"email\": \"$email\", \"password\": \"$password\"}"
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        // NetworkService에서 Retrofit 인터페이스를 통해 로그인 요청을 보냄
        NetworkService.getService().requestSignIn(requestBody)
            .enqueue(object : Callback<ApiResponse<String>> {
                override fun onResponse(
                    call: Call<ApiResponse<String>>,
                    response: Response<ApiResponse<String>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        success(data)
                    } else {
                        // 통신 실패 처리
                        networkFail(response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                    // 실패한 응답 처리
                    failure(t)
                }
            })
    }
}