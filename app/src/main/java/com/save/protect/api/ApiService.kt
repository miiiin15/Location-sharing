package com.save.protect.api

import com.save.protect.data.UserInfo
import kr.co.essb.app.data.api.response.ApiResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    /**
     * 공통
     */

    // 로그인
    @POST("api/auth/sign-in")
    fun requestSignIn(@Body params: RequestBody): Call<ApiResponse<String>>

    // 내정보 조회
    @GET("api/member")
    fun getUserInfo(): Call<ApiResponse<UserInfo>>

    //주소 검증
    @POST("api/location")
    fun saveLocationList(@Body params: RequestBody): Call<ApiResponse<String>>

    //약관 조회
//    @POST("api/policy")
//    fun requestPolicy(@Body params: RequestBody): Call<ApiResponse<List<Policy>>>

    // 약관 상세 조회
//    @GET("api/policy/auth")
//    fun getPolicyDetail(@Query("cd") cd: String): Call<ResponseBody>

//    @GET("api/lookup/auth/list")
//    fun getAuthType(): Call<ApiResponse<List<AuthType>>>


}

