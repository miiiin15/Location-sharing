package kr.co.essb.app.data.api.response

data class ApiError(
    // 오류 코드
    val code: String? = null,

    // 오류 메시지
    val message: String? = null
)