package com.ttukttakbap.backend.history

import com.ttukttakbap.backend.auth.jwt.JwtProvider
import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(ViewHistoryController::class)
@Import(SecurityConfig::class)
class ViewHistoryControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var viewHistoryService: ViewHistoryService

    private fun bearer() = "Bearer ${jwtProvider.createAccessToken(1L, "USER")}"

    @Test
    fun `토큰 없이 최근 본 메뉴 조회하면 401`() {
        mockMvc.get("/api/v1/me/history").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `최근 본 메뉴 목록을 반환한다`() {
        whenever(viewHistoryService.getHistory(1L)).thenReturn(
            listOf(MenuResponse(2, "된장찌개", "구수한 찌개", "", 25, "EASY", "찌개", isFavorite = false)),
        )

        mockMvc.get("/api/v1/me/history") {
            header("Authorization", bearer())
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].name") { value("된장찌개") }
        }
    }

    @Test
    fun `조회 기록을 남기면 204`() {
        mockMvc.post("/api/v1/me/history/1") {
            header("Authorization", bearer())
        }.andExpect { status { isNoContent() } }
    }
}
