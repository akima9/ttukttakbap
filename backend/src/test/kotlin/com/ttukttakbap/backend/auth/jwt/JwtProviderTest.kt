package com.ttukttakbap.backend.auth.jwt

import com.ttukttakbap.backend.common.exception.UnauthorizedException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JwtProviderTest {

    private val provider = JwtProvider(
        secret = "test-secret-key-for-junit-please-32bytes-long!!",
        accessExpiryMillis = 1_800_000,
        refreshExpiryMillis = 1_209_600_000,
    )

    @Test
    fun `액세스 토큰에서 userId와 role을 복원한다`() {
        val token = provider.createAccessToken(7L, "USER")

        assertEquals(7L, provider.getUserId(token))
        assertEquals("USER", provider.getRole(token))
    }

    @Test
    fun `리프레시 토큰은 role 클레임이 없다`() {
        val token = provider.createRefreshToken(7L)

        assertEquals(7L, provider.getUserId(token))
        assertNull(provider.getRole(token))
    }

    @Test
    fun `위조된 토큰은 UnauthorizedException`() {
        assertThrows<UnauthorizedException> { provider.getUserId("not-a-jwt") }
    }

    @Test
    fun `다른 시크릿으로 서명된 토큰은 거부한다`() {
        val other = JwtProvider("another-secret-key-32bytes-minimum-length!!", 1000, 1000)
        val token = other.createAccessToken(1L, "USER")

        assertThrows<UnauthorizedException> { provider.getUserId(token) }
    }
}
