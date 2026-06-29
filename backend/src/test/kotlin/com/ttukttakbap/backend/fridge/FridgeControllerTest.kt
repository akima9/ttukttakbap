package com.ttukttakbap.backend.fridge

import com.ttukttakbap.backend.auth.jwt.JwtProvider
import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
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

@WebMvcTest(FridgeController::class)
@Import(SecurityConfig::class)
class FridgeControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtProvider: JwtProvider
    @MockBean lateinit var fridgeService: FridgeService

    private fun bearer() = "Bearer ${jwtProvider.createAccessToken(1L, "USER")}"

    @Test
    fun `토큰 없이 냉장고 조회하면 401`() {
        mockMvc.get("/api/v1/me/fridge").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `냉장고 재료 목록을 반환한다`() {
        whenever(fridgeService.getFridge(1L)).thenReturn(
            listOf(IngredientResponse(1, "김치", "1포기", "마트", null)),
        )

        mockMvc.get("/api/v1/me/fridge") {
            header("Authorization", bearer())
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].name") { value("김치") }
        }
    }

    @Test
    fun `냉장고에 재료를 담으면 201`() {
        mockMvc.post("/api/v1/me/fridge/1") {
            header("Authorization", bearer())
        }.andExpect { status { isCreated() } }
    }

    @Test
    fun `냉장고에서 재료를 빼면 204`() {
        mockMvc.delete("/api/v1/me/fridge/1") {
            header("Authorization", bearer())
        }.andExpect { status { isNoContent() } }
    }
}
