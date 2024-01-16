package com.save.protect.data.test.repo

import android.util.Log
import com.save.protect.api.NetworkService
import com.save.protect.helper.Logcat
import kr.co.essb.app.data.api.response.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object TestRepo {

    fun getTest(
        networkFail: (String) -> Unit,
        success: (ApiResponse<Any>) -> Unit,
        failure: (Throwable) -> Unit
    ) {

        try {

            NetworkService.getService().getSupportPopup()
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(
                        call: Call<ApiResponse<Any>>,
                        response: Response<ApiResponse<Any>>
                    ) {

                        if (response.isSuccessful) {

                            val data = response.body() ?: return
                            success(data)

                        } else {
                            Logcat.d("Log register isSuccessful != ${response.code()}")
                            Logcat.d("Log register isSuccessful != ${response.message()}")
                            networkFail(response.code().toString())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                        Logcat.d("Log register failure : ${t.message}")
                        failure(t)
                    }

                })
        } catch (e: Exception) {
            Log.d("테슽흐에러 : ", "$e.message")
        }

    }
}