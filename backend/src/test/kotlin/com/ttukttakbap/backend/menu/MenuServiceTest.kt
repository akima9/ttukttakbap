package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.Ingredient
import com.ttukttakbap.backend.recipe.Recipe
import com.ttukttakbap.backend.recipe.RecipeRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MenuServiceTest {

    @Mock lateinit var menuRepository: MenuRepository
    @Mock lateinit var menuIngredientRepository: MenuIngredientRepository
    @Mock lateinit var recipeRepository: RecipeRepository

    @InjectMocks lateinit var menuService: MenuService

    private val sampleMenu = Menu(id = 1, name = "김치찌개", description = "얼큰한 찌개", imageUrl = "", cookTimeMinutes = 30, difficulty = Difficulty.EASY)

    @Test
    fun `전체 메뉴 목록을 반환한다`() {
        whenever(menuRepository.findAll()).thenReturn(listOf(sampleMenu))

        val result = menuService.getMenus()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("김치찌개")
        assertThat(result[0].difficulty).isEqualTo("EASY")
    }

    @Test
    fun `메뉴 단건을 조회한다`() {
        whenever(menuRepository.findById(1L)).thenReturn(Optional.of(sampleMenu))

        val result = menuService.getMenu(1L)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo("김치찌개")
    }

    @Test
    fun `존재하지 않는 메뉴 조회 시 NotFoundException을 던진다`() {
        whenever(menuRepository.findById(99L)).thenReturn(Optional.empty())

        assertThatThrownBy { menuService.getMenu(99L) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("해당 메뉴를 찾을 수 없습니다.")
    }

    @Test
    fun `인원 수에 맞게 재료 양을 계산한다`() {
        val ingredient = Ingredient(id = 1, name = "김치", purchaseUnit = "1포기")
        val menuIngredient = MenuIngredient(id = 1, menu = sampleMenu, ingredient = ingredient, amountPerPerson = BigDecimal("150.00"), unit = "g")
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(menuIngredientRepository.findByMenuId(1L)).thenReturn(listOf(menuIngredient))

        val result = menuService.getMenuIngredients(1L, people = 2)

        assertThat(result).hasSize(1)
        assertThat(result[0].amount).isEqualByComparingTo(BigDecimal("300.00"))
        assertThat(result[0].unit).isEqualTo("g")
    }

    @Test
    fun `레시피 단계를 순서대로 반환한다`() {
        val step1 = Recipe(id = 1, menu = sampleMenu, stepOrder = 1, description = "김치를 볶는다")
        val step2 = Recipe(id = 2, menu = sampleMenu, stepOrder = 2, description = "물을 붓는다", tip = "센 불로")
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(recipeRepository.findByMenuIdOrderByStepOrder(1L)).thenReturn(listOf(step1, step2))

        val result = menuService.getMenuRecipe(1L)

        assertThat(result).hasSize(2)
        assertThat(result[0].stepOrder).isEqualTo(1)
        assertThat(result[1].tip).isEqualTo("센 불로")
    }
}
