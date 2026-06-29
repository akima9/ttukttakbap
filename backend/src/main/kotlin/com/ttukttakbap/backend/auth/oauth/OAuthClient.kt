package com.ttukttakbap.backend.auth.oauth

import com.ttukttakbap.backend.user.SocialProvider

data class OAuthUserInfo(
    val provider: SocialProvider,
    val socialId: String,
    val email: String?,
    val nickname: String,
    val profileImageUrl: String?,
)

// 소셜 제공자별 클라이언트. 인가 코드를 토큰으로 교환하고 사용자 정보를 가져온다.
interface OAuthClient {
    fun provider(): SocialProvider
    fun fetchUserInfo(code: String, redirectUri: String): OAuthUserInfo
}
