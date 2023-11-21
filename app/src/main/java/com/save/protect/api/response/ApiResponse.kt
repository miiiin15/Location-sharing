package kr.co.essb.app.data.api.response

data class ApiResponse<T>(
    // 응답 상태
    var status: String,

    // 오류
    var error: ApiError,

    // 응답 데이터
    var data: T
)