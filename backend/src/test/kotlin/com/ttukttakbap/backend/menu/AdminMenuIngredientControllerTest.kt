package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.menu.dto.MenuIngredientLinkResponse
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@WebMvcTest(AdminMenuIngredientController::class)
@Import(SecurityConfig::class)
class AdminMenuIngredientControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var menuIngredientService: MenuIngredientService

    private val body = """{"ingredientId":2,"amountPerPerson":150.0,"unit":"g"}"""

    @Test
    fun `인증 없이 메뉴-재료 연결하면 401`() {
        mockMvc.post("/api/v1/admin/menus/1/ingredients") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `어드민 인증으로 메뉴에 재료를 연결하면 201`() {
        whenever(menuIngredientService.linkIngredient(eq(1L), any()))
            .thenReturn(
                MenuIngredientLinkResponse(
                    id = 9, menuId = 1, ingredientId = 2, ingredientName = "김치",
                    amountPerPerson = BigDecimal("150.0"), unit = "g",
                ),
            )

        mockMvc.post("/api/v1/admin/menus/1/ingredients") {
            with(httpBasic("admin", "admin1234"))
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(9) }
            jsonPath("$.ingredientName") { value("김치") }
        }
    }

    @Test
    fun `어드민 인증으로 연결을 삭제하면 204`() {
        mockMvc.delete("/api/v1/admin/menu-ingredients/9") {
            with(httpBasic("admin", "admin1234"))
        }.andExpect { status { isNoContent() } }
    }
}
