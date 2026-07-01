package com.ttukttakbap.backend.ingredient

import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(IngredientController::class)
@Import(SecurityConfig::class)
class IngredientControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var ingredientService: IngredientService

    @Test
    fun `인증 없이 재료 목록을 조회할 수 있다`() {
        whenever(ingredientService.getIngredients()).thenReturn(
            listOf(
                IngredientResponse(id = 1, name = "김치", purchaseUnit = "1포기", purchaseLocation = "마트", coupangUrl = null),
                IngredientResponse(id = 2, name = "두부", purchaseUnit = "1모", purchaseLocation = "마트", coupangUrl = null),
            ),
        )

        mockMvc.get("/api/v1/ingredients").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].name") { value("김치") }
        }
    }
}
