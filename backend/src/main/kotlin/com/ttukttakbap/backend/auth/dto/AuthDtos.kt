package com.ttukttakbap.backend.auth.dto

import com.ttukttakbap.backend.user.dto.UserResponse

// 프론트가 소셜에서 받은 인가 코드 + 사용 리다이렉트 URI
data class LoginRequest(
    val code: String,
    val redirectUri: String,
)

data class RefreshRequest(val refreshToken: String)

data class LogoutRequest(val refreshToken: String)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
