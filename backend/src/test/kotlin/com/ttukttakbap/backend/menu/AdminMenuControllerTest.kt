package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(AdminMenuController::class)
@Import(SecurityConfig::class)
class AdminMenuControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var menuService: MenuService

    private val body = """
        {"name":"김치찌개","description":"얼큰한 찌개","imageUrl":"","cookTimeMinutes":30,"difficulty":"EASY","category":"찌개"}
    """.trimIndent()

    private val sample = MenuResponse(
        id = 1, name = "김치찌개", description = "얼큰한 찌개", imageUrl = "",
        cookTimeMinutes = 30, difficulty = "EASY", category = "찌개", isFavorite = false,
    )

    @Test
    fun `인증 없이 메뉴 생성하면 401`() {
        mockMvc.post("/api/v1/admin/menus") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `어드민 인증으로 메뉴를 생성하면 201`() {
        whenever(menuService.createMenu(any())).thenReturn(sample)

        mockMvc.post("/api/v1/admin/menus") {
            with(httpBasic("admin", "admin1234"))
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("김치찌개") }
        }
    }

    @Test
    fun `어드민 인증으로 메뉴를 수정하면 200`() {
        whenever(menuService.updateMenu(eq(1L), any())).thenReturn(sample)

        mockMvc.put("/api/v1/admin/menus/1") {
            with(httpBasic("admin", "admin1234"))
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("김치찌개") }
        }
    }

    @Test
    fun `어드민 인증으로 메뉴를 삭제하면 204`() {
        mockMvc.delete("/api/v1/admin/menus/1") {
            with(httpBasic("admin", "admin1234"))
        }.andExpect { status { isNoContent() } }
    }
}
