package com.ttukttakbap.backend.auth

import com.ttukttakbap.backend.auth.dto.RefreshResponse
import com.ttukttakbap.backend.auth.dto.TokenResponse
import com.ttukttakbap.backend.auth.jwt.JwtProvider
import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.user.dto.UserResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var authService: AuthService

    private val userResponse = UserResponse(1, "기영", "e@e.com", null, "USER")

    @Test
    fun `카카오 로그인하면 200과 토큰을 반환한다`() {
        whenever(authService.login(eq("kakao"), eq("code"), eq("uri")))
            .thenReturn(TokenResponse("access", "refresh", userResponse))

        mockMvc.post("/api/v1/auth/login/kakao") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"code":"code","redirectUri":"uri"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("access") }
            jsonPath("$.user.nickname") { value("기영") }
        }
    }

    @Test
    fun `리프레시하면 200과 새 토큰을 반환한다`() {
        whenever(authService.refresh("refresh")).thenReturn(RefreshResponse("newAccess", "newRefresh"))

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken":"refresh"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("newAccess") }
        }
    }

    @Test
    fun `로그아웃하면 204`() {
        mockMvc.post("/api/v1/auth/logout") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken":"refresh"}"""
        }.andExpect { status { isNoContent() } }
    }

    @Test
    fun `토큰 없이 내 정보 조회하면 401`() {
        mockMvc.get("/api/v1/auth/me").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `유효한 액세스 토큰으로 내 정보를 조회하면 200`() {
        whenever(authService.me(1L)).thenReturn(userResponse)
        val token = jwtProvider.createAccessToken(1L, "USER")

        mockMvc.get("/api/v1/auth/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.nickname") { value("기영") }
        }
    }
}
