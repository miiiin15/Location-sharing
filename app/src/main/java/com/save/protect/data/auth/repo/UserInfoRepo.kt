package com.save.protect.data.auth.repo

import com.save.protect.api.NetworkService
import com.save.protect.data.UserInfo
import kr.co.essb.app.data.api.response.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UserInfoRepo {

    fun getUserInfo(
        networkFail: (String) -> Unit,
        success: (ApiResponse<UserInfo>) -> Unit,
        failure: (Throwable) -> Unit
    ) {
        NetworkService.getService().getUserInfo()
            .enqueue(object : Callback<ApiResponse<UserInfo>> {
                override fun onResponse(
                    call: Call<ApiResponse<UserInfo>>,
                    response: Response<ApiResponse<UserInfo>>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        success(data)
                    } else {
                        networkFail(response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UserInfo>>, t: Throwable) {
                    failure(t)
                }
            })
    }
}


