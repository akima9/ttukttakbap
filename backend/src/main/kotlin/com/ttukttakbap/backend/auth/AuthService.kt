package com.ttukttakbap.backend.auth

import com.ttukttakbap.backend.auth.dto.RefreshResponse
import com.ttukttakbap.backend.auth.dto.TokenResponse
import com.ttukttakbap.backend.auth.jwt.JwtProvider
import com.ttukttakbap.backend.auth.oauth.OAuthClient
import com.ttukttakbap.backend.auth.oauth.OAuthUserInfo
import com.ttukttakbap.backend.auth.token.RefreshTokenStore
import com.ttukttakbap.backend.common.exception.UnauthorizedException
import com.ttukttakbap.backend.user.SocialProvider
import com.ttukttakbap.backend.user.User
import com.ttukttakbap.backend.user.UserRepository
import com.ttukttakbap.backend.user.dto.UserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    oAuthClients: List<OAuthClient>,
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val refreshTokenStore: RefreshTokenStore,
) {
    private val clientsByProvider = oAuthClients.associateBy { it.provider() }

    @Transactional
    fun login(provider: String, code: String, redirectUri: String): TokenResponse {
        val socialProvider = parseProvider(provider)
        val client = clientsByProvider[socialProvider]
            ?: throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다: $provider")

        val user = findOrCreate(client.fetchUserInfo(code, redirectUri))
        return issueTokens(user)
    }

    @Transactional
    fun refresh(refreshToken: String): RefreshResponse {
        val userId = jwtProvider.getUserId(refreshToken)
        if (refreshTokenStore.find(userId) != refreshToken) {
            throw UnauthorizedException("유효하지 않은 리프레시 토큰입니다.")
        }
        val user = userRepository.findById(userId)
            .orElseThrow { UnauthorizedException("유효하지 않은 리프레시 토큰입니다.") }
        val tokens = issueTokens(user)
        return RefreshResponse(tokens.accessToken, tokens.refreshToken)
    }

    fun logout(refreshToken: String) {
        // 만료/위조 토큰이어도 멱등하게 처리한다.
        try {
            refreshTokenStore.delete(jwtProvider.getUserId(refreshToken))
        } catch (e: UnauthorizedException) {
            // no-op
        }
    }

    fun me(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UnauthorizedException("인증 정보가 유효하지 않습니다.") }
        return UserResponse.from(user)
    }

    private fun findOrCreate(info: OAuthUserInfo): User =
        userRepository.findBySocialProviderAndSocialId(info.provider, info.socialId)
            ?: userRepository.save(
                User(
                    socialProvider = info.provider,
                    socialId = info.socialId,
                    email = info.email,
                    nickname = info.nickname,
                    profileImageUrl = info.profileImageUrl,
                ),
            )

    private fun issueTokens(user: User): TokenResponse {
        val accessToken = jwtProvider.createAccessToken(user.id, user.role.name)
        val refreshToken = jwtProvider.createRefreshToken(user.id)
        refreshTokenStore.save(user.id, refreshToken, jwtProvider.refreshExpiryMillis())
        return TokenResponse(accessToken, refreshToken, UserResponse.from(user))
    }

    private fun parseProvider(provider: String): SocialProvider =
        try {
            SocialProvider.valueOf(provider.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다: $provider")
        }
}
