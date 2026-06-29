package com.ttukttakbap.backend.favorite

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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(FavoriteController::class)
@Import(SecurityConfig::class)
class FavoriteControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var favoriteService: FavoriteService

    private fun bearer() = "Bearer ${jwtProvider.createAccessToken(1L, "USER")}"

    @Test
    fun `토큰 없이 즐겨찾기 목록 조회하면 401`() {
        mockMvc.get("/api/v1/me/favorites").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `즐겨찾기 목록을 반환한다`() {
        whenever(favoriteService.getFavorites(1L)).thenReturn(
            listOf(MenuResponse(1, "김치찌개", "얼큰한 찌개", "", 30, "EASY", "찌개", isFavorite = true)),
        )

        mockMvc.get("/api/v1/me/favorites") {
            header("Authorization", bearer())
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].name") { value("김치찌개") }
            jsonPath("$[0].isFavorite") { value(true) }
        }
    }

    @Test
    fun `즐겨찾기를 추가하면 201`() {
        mockMvc.post("/api/v1/me/favorites/1") {
            header("Authorization", bearer())
        }.andExpect { status { isCreated() } }
    }

    @Test
    fun `즐겨찾기를 삭제하면 204`() {
        mockMvc.delete("/api/v1/me/favorites/1") {
            header("Authorization", bearer())
        }.andExpect { status { isNoContent() } }
    }
}
