package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.dto.PageResponse
import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.common.security.SecurityConfig
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

@WebMvcTest(MenuController::class)
@Import(SecurityConfig::class)
class MenuControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var menuService: MenuService

    private val sampleMenu = MenuResponse(
        id = 1, name = "김치찌개", description = "얼큰한 찌개", imageUrl = "",
        cookTimeMinutes = 30, difficulty = "EASY", category = "찌개", isFavorite = false,
    )

    private fun pageOf(vararg menus: MenuResponse) =
        PageResponse(content = menus.toList(), page = 0, size = 20, totalElements = menus.size.toLong(), totalPages = 1)

    @Test
    fun `GET api_v1_menus 는 페이지 형태의 메뉴 목록을 반환한다`() {
        whenever(menuService.getMenus(anyOrNull(), anyOrNull(), anyOrNull(), any(), anyOrNull())).thenReturn(pageOf(sampleMenu))

        mockMvc.get("/api/v1/menus")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("김치찌개") }
                jsonPath("$.content[0].category") { value("찌개") }
                jsonPath("$.content[0].isFavorite") { value(false) }
                jsonPath("$.totalElements") { value(1) }
            }
    }

    @Test
    fun `GET api_v1_menus_recommend 는 추천 목록을 반환한다`() {
        whenever(menuService.recommend(anyOrNull(), anyOrNull(), anyOrNull(), any(), anyOrNull(), any())).thenReturn(pageOf(sampleMenu))

        mockMvc.get("/api/v1/menus/recommend?people=2")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("김치찌개") }
            }
    }

    @Test
    fun `GET api_v1_menus_recommend 인원수가 범위를 벗어나면 400을 반환한다`() {
        mockMvc.get("/api/v1/menus/recommend?people=0")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value(400) }
            }
    }

    @Test
    fun `GET api_v1_categories 는 카테고리 목록을 반환한다`() {
        whenever(menuService.getCategories()).thenReturn(listOf("찌개", "국", "밥"))

        mockMvc.get("/api/v1/categories")
            .andExpect {
                status { isOk() }
                jsonPath("$[0]") { value("찌개") }
            }
    }

    @Test
    fun `GET api_v1_menus_id 는 메뉴 단건을 반환한다`() {
        whenever(menuService.getMenu(eq(1L), anyOrNull())).thenReturn(sampleMenu)

        mockMvc.get("/api/v1/menus/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("김치찌개") }
                jsonPath("$.category") { value("찌개") }
            }
    }

    @Test
    fun `GET api_v1_menus_id 존재하지 않으면 404를 반환한다`() {
        whenever(menuService.getMenu(eq(99L), anyOrNull())).thenThrow(NotFoundException("해당 메뉴를 찾을 수 없습니다."))

        mockMvc.get("/api/v1/menus/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { value("해당 메뉴를 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `GET api_v1_menus_id_ingredients 는 인원 수 기반 재료 목록을 반환한다`() {
        val response = MenuIngredientResponse(
            ingredientId = 1, name = "김치", requiredAmount = BigDecimal("300.0"), unit = "g",
            purchaseUnit = "1포기", purchaseLocation = "마트", coupangUrl = null,
        )
        whenever(menuService.getMenuIngredients(1L, 2)).thenReturn(listOf(response))

        mockMvc.get("/api/v1/menus/1/ingredients?people=2")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("김치") }
                jsonPath("$[0].requiredAmount") { value(300.0) }
            }
    }

    @Test
    fun `GET api_v1_menus_id_ingredients 인원수가 범위를 벗어나면 400을 반환한다`() {
        mockMvc.get("/api/v1/menus/1/ingredients?people=99")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.status") { value(400) }
            }
    }

    @Test
    fun `GET api_v1_menus_id_recipe 는 레시피 단계를 반환한다`() {
        val steps = listOf(
            RecipeStepResponse(stepOrder = 1, description = "김치를 볶는다", tip = null),
            RecipeStepResponse(stepOrder = 2, description = "물을 붓는다", tip = "센 불로"),
        )
        whenever(menuService.getMenuRecipe(1L)).thenReturn(steps)

        mockMvc.get("/api/v1/menus/1/recipe")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].stepOrder") { value(1) }
                jsonPath("$[1].tip") { value("센 불로") }
            }
    }
}
