package com.save.protect.api

import kr.co.essb.app.data.api.response.ApiResponse
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    /**
     * 공통
     */

    // TEST
    @GET("api/test")
    fun getSupportPopup(): Call<ApiResponse<Any>>

    // 주소검색
//    @POST("api/lookup/address")
//    fun requestAddress(@Body params: RequestBody): Call<ApiResponse<List<AddressSearch>>>

    //주소 검증
//    @POST("api/lookup/address/verify")
//    fun requestAddressVerify(@Body params: RequestBody): Call<ApiResponse<AddressVerify>>

    //약관 조회
//    @POST("api/policy")
//    fun requestPolicy(@Body params: RequestBody): Call<ApiResponse<List<Policy>>>

    //공지 사항 팝업
//    @GET("api/support/pop-up")
//    fun getSupportPopup(): Call<ApiResponse<Notice>>

//    @GET("api/lookup/auth/list")
//    fun getAuthType(): Call<ApiResponse<List<AuthType>>>


}

