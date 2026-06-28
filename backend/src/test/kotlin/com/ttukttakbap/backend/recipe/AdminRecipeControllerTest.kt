package com.ttukttakbap.backend.recipe

import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.recipe.dto.RecipeResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AdminRecipeController::class)
@Import(SecurityConfig::class)
class AdminRecipeControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var recipeService: RecipeService

    private val body = """{"stepOrder":1,"description":"김치를 볶는다","tip":"센 불"}"""

    @Test
    fun `인증 없이 레시피 생성하면 401`() {
        mockMvc.post("/api/v1/admin/menus/1/recipes") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `어드민 인증으로 레시피를 생성하면 201`() {
        whenever(recipeService.createRecipe(eq(1L), any()))
            .thenReturn(RecipeResponse(id = 5, menuId = 1, stepOrder = 1, description = "김치를 볶는다", tip = "센 불"))

        mockMvc.post("/api/v1/admin/menus/1/recipes") {
            with(httpBasic("admin", "admin1234"))
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(5) }
            jsonPath("$.stepOrder") { value(1) }
        }
    }
}
