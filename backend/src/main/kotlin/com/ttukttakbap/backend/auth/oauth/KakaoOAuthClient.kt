package com.ttukttakbap.backend.auth.oauth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.ttukttakbap.backend.common.exception.UnauthorizedException
import com.ttukttakbap.backend.user.SocialProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

// 카카오 인가 코드 → 액세스 토큰 교환(kauth) → 사용자 정보 조회(kapi).
@Component
class KakaoOAuthClient(
    @Value("\${oauth.kakao.client-id}") private val clientId: String,
    @Value("\${oauth.kakao.client-secret:}") private val clientSecret: String,
    private val restClientBuilder: RestClient.Builder,
) : OAuthClient {

    override fun provider(): SocialProvider = SocialProvider.KAKAO

    override fun fetchUserInfo(code: String, redirectUri: String): OAuthUserInfo {
        val accessToken = exchangeToken(code, redirectUri)
        return fetchUser(accessToken)
    }

    private fun exchangeToken(code: String, redirectUri: String): String {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("redirect_uri", redirectUri)
            add("code", code)
            if (clientSecret.isNotBlank()) add("client_secret", clientSecret)
        }
        val response = try {
            restClientBuilder.build()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse::class.java)
        } catch (e: RestClientException) {
            throw UnauthorizedException("카카오 토큰 교환에 실패했습니다.")
        }
        return response?.accessToken ?: throw UnauthorizedException("카카오 토큰 교환에 실패했습니다.")
    }

    private fun fetchUser(accessToken: String): OAuthUserInfo {
        val user = try {
            restClientBuilder.build()
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body(KakaoUserResponse::class.java)
        } catch (e: RestClientException) {
            throw UnauthorizedException("카카오 사용자 정보 조회에 실패했습니다.")
        } ?: throw UnauthorizedException("카카오 사용자 정보 조회에 실패했습니다.")

        val profile = user.kakaoAccount?.profile
        return OAuthUserInfo(
            provider = SocialProvider.KAKAO,
            socialId = user.id.toString(),
            email = user.kakaoAccount?.email,
            nickname = profile?.nickname ?: "카카오사용자",
            profileImageUrl = profile?.profileImageUrl,
        )
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KakaoTokenResponse(
        @JsonProperty("access_token") val accessToken: String?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KakaoUserResponse(
        val id: Long,
        @JsonProperty("kakao_account") val kakaoAccount: KakaoAccount?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KakaoAccount(
        val email: String?,
        val profile: KakaoProfile?,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KakaoProfile(
        val nickname: String?,
        @JsonProperty("profile_image_url") val profileImageUrl: String?,
    )
}
