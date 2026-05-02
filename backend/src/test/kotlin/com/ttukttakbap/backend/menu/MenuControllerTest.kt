package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

@WebMvcTest(MenuController::class)
class MenuControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockBean lateinit var menuService: MenuService

    private val sampleMenu = MenuResponse(id = 1, name = "김치찌개", description = "얼큰한 찌개", imageUrl = "", cookTimeMinutes = 30, difficulty = "EASY")

    @Test
    fun `GET api_v1_menus 는 메뉴 목록을 반환한다`() {
        whenever(menuService.getMenus()).thenReturn(listOf(sampleMenu))

        mockMvc.get("/api/v1/menus")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("김치찌개") }
                jsonPath("$[0].difficulty") { value("EASY") }
            }
    }

    @Test
    fun `GET api_v1_menus_id 는 메뉴 단건을 반환한다`() {
        whenever(menuService.getMenu(1L)).thenReturn(sampleMenu)

        mockMvc.get("/api/v1/menus/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("김치찌개") }
            }
    }

    @Test
    fun `GET api_v1_menus_id 존재하지 않으면 404를 반환한다`() {
        whenever(menuService.getMenu(99L)).thenThrow(NotFoundException("해당 메뉴를 찾을 수 없습니다."))

        mockMvc.get("/api/v1/menus/99")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(404) }
                jsonPath("$.message") { value("해당 메뉴를 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `GET api_v1_menus_id_ingredients 는 인원 수 기반 재료 목록을 반환한다`() {
        val response = MenuIngredientResponse(ingredientId = 1, name = "김치", amount = BigDecimal("300.00"), unit = "g", purchaseUnit = "1포기", purchaseLocation = "마트")
        whenever(menuService.getMenuIngredients(1L, 2)).thenReturn(listOf(response))

        mockMvc.get("/api/v1/menus/1/ingredients?people=2")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].name") { value("김치") }
                jsonPath("$[0].amount") { value(300.0) }
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
