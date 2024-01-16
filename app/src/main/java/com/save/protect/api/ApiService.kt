package com.save.protect.api

import kr.co.essb.app.data.api.response.ApiResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    /**
     * 공통
     */

    // TEST
    @GET("api/test")
    fun getSupportPopup(): Call<ApiResponse<Any>>

    // 로그인
    @POST("api/auth/sign-in")
    fun requestSignIn(@Body params: RequestBody): Call<ApiResponse<String>>

    //주소 검증
//    @POST("api/lookup/address/verify")
//    fun requestAddressVerify(@Body params: RequestBody): Call<ApiResponse<AddressVerify>>

    //약관 조회
//    @POST("api/policy")
//    fun requestPolicy(@Body params: RequestBody): Call<ApiResponse<List<Policy>>>

    // 약관 상세 조회
//    @GET("api/policy/auth")
//    fun getPolicyDetail(@Query("cd") cd: String): Call<ResponseBody>

//    @GET("api/lookup/auth/list")
//    fun getAuthType(): Call<ApiResponse<List<AuthType>>>


}

