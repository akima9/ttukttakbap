package com.ttukttakbap.backend.ingredient

import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AdminIngredientController::class)
@Import(SecurityConfig::class)
class AdminIngredientControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var ingredientService: IngredientService

    private val body = """{"name":"김치","purchaseUnit":"1포기","purchaseLocation":"마트"}"""

    @Test
    fun `인증 없이 재료 생성하면 401`() {
        mockMvc.post("/api/v1/admin/ingredients") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `어드민 인증으로 재료를 생성하면 201`() {
        whenever(ingredientService.createIngredient(any()))
            .thenReturn(IngredientResponse(id = 1, name = "김치", purchaseUnit = "1포기", purchaseLocation = "마트", coupangUrl = null))

        mockMvc.post("/api/v1/admin/ingredients") {
            with(httpBasic("admin", "admin1234"))
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("김치") }
        }
    }
}
