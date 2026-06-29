package com.ttukttakbap.backend.auth

import com.ttukttakbap.backend.auth.jwt.JwtProvider
import com.ttukttakbap.backend.auth.oauth.OAuthClient
import com.ttukttakbap.backend.auth.oauth.OAuthUserInfo
import com.ttukttakbap.backend.auth.token.RefreshTokenStore
import com.ttukttakbap.backend.common.exception.UnauthorizedException
import com.ttukttakbap.backend.user.SocialProvider
import com.ttukttakbap.backend.user.User
import com.ttukttakbap.backend.user.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import kotlin.test.assertEquals

class AuthServiceTest {

    private lateinit var kakaoClient: OAuthClient
    private lateinit var userRepository: UserRepository
    private lateinit var jwtProvider: JwtProvider
    private lateinit var refreshTokenStore: RefreshTokenStore
    private lateinit var service: AuthService

    private val user = User(
        id = 1, socialProvider = SocialProvider.KAKAO, socialId = "123",
        email = "e@e.com", nickname = "기영", profileImageUrl = null,
    )

    @BeforeEach
    fun setUp() {
        kakaoClient = mock { on { provider() } doReturn SocialProvider.KAKAO }
        userRepository = mock()
        jwtProvider = mock()
        refreshTokenStore = mock()
        service = AuthService(listOf(kakaoClient), userRepository, jwtProvider, refreshTokenStore)
    }

    @Test
    fun `최초 카카오 로그인은 사용자를 생성하고 토큰을 발급한다`() {
        whenever(kakaoClient.fetchUserInfo("code", "uri"))
            .thenReturn(OAuthUserInfo(SocialProvider.KAKAO, "123", "e@e.com", "기영", null))
        whenever(userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, "123")).thenReturn(null)
        whenever(userRepository.save(any<User>())).thenReturn(user)
        whenever(jwtProvider.createAccessToken(1L, "USER")).thenReturn("access")
        whenever(jwtProvider.createRefreshToken(1L)).thenReturn("refresh")
        whenever(jwtProvider.refreshExpiryMillis()).thenReturn(1000L)

        val result = service.login("kakao", "code", "uri")

        assertEquals("access", result.accessToken)
        assertEquals("refresh", result.refreshToken)
        assertEquals("기영", result.user.nickname)
        verify(userRepository).save(any<User>())
        verify(refreshTokenStore).save(1L, "refresh", 1000L)
    }

    @Test
    fun `기존 사용자는 재생성하지 않는다`() {
        whenever(kakaoClient.fetchUserInfo("code", "uri"))
            .thenReturn(OAuthUserInfo(SocialProvider.KAKAO, "123", "e@e.com", "기영", null))
        whenever(userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, "123")).thenReturn(user)
        whenever(jwtProvider.createAccessToken(1L, "USER")).thenReturn("access")
        whenever(jwtProvider.createRefreshToken(1L)).thenReturn("refresh")
        whenever(jwtProvider.refreshExpiryMillis()).thenReturn(1000L)

        service.login("kakao", "code", "uri")

        verify(userRepository, org.mockito.kotlin.never()).save(any<User>())
    }

    @Test
    fun `지원하지 않는 provider면 IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> { service.login("google", "code", "uri") }
        assertThrows<IllegalArgumentException> { service.login("facebook", "code", "uri") }
    }

    @Test
    fun `리프레시는 저장된 토큰과 일치할 때 회전한다`() {
        whenever(jwtProvider.getUserId("refresh")).thenReturn(1L)
        whenever(refreshTokenStore.find(1L)).thenReturn("refresh")
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
        whenever(jwtProvider.createAccessToken(1L, "USER")).thenReturn("newAccess")
        whenever(jwtProvider.createRefreshToken(1L)).thenReturn("newRefresh")
        whenever(jwtProvider.refreshExpiryMillis()).thenReturn(1000L)

        val result = service.refresh("refresh")

        assertEquals("newAccess", result.accessToken)
        assertEquals("newRefresh", result.refreshToken)
        verify(refreshTokenStore).save(1L, "newRefresh", 1000L)
    }

    @Test
    fun `저장된 리프레시 토큰과 다르면 401`() {
        whenever(jwtProvider.getUserId("refresh")).thenReturn(1L)
        whenever(refreshTokenStore.find(1L)).thenReturn("different")

        assertThrows<UnauthorizedException> { service.refresh("refresh") }
    }

    @Test
    fun `로그아웃은 저장소에서 리프레시 토큰을 삭제한다`() {
        whenever(jwtProvider.getUserId("refresh")).thenReturn(1L)

        service.logout("refresh")

        verify(refreshTokenStore).delete(1L)
    }
}
