package com.ttukttakbap.backend.recipe

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.Category
import com.ttukttakbap.backend.menu.Difficulty
import com.ttukttakbap.backend.menu.Menu
import com.ttukttakbap.backend.menu.MenuRepository
import com.ttukttakbap.backend.recipe.dto.RecipeRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RecipeServiceTest {

    @Mock lateinit var recipeRepository: RecipeRepository
    @Mock lateinit var menuRepository: MenuRepository
    @InjectMocks lateinit var recipeService: RecipeService

    private val menu = Menu(
        id = 1, name = "김치찌개", description = "얼큰", imageUrl = "",
        cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE,
    )
    private val request = RecipeRequest(stepOrder = 1, description = "김치를 볶는다", tip = "센 불")

    @Test
    fun `메뉴별 레시피 목록을 id와 함께 조회한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(recipeRepository.findByMenuIdOrderByStepOrder(1L))
            .thenReturn(listOf(Recipe(id = 5, menu = menu, stepOrder = 1, description = "볶기")))

        val result = recipeService.getRecipes(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(5L)
    }

    @Test
    fun `없는 메뉴의 레시피 목록 조회 시 NotFoundException`() {
        whenever(menuRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { recipeService.getRecipes(99L) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `메뉴에 레시피 단계를 생성한다`() {
        whenever(menuRepository.findById(1L)).thenReturn(Optional.of(menu))
        whenever(recipeRepository.save(any<Recipe>()))
            .thenReturn(Recipe(id = 5, menu = menu, stepOrder = 1, description = "김치를 볶는다", tip = "센 불"))

        val result = recipeService.createRecipe(1L, request)

        assertThat(result.id).isEqualTo(5L)
        assertThat(result.menuId).isEqualTo(1L)
        assertThat(result.stepOrder).isEqualTo(1)
    }

    @Test
    fun `없는 메뉴에 레시피 생성 시 NotFoundException`() {
        whenever(menuRepository.findById(99L)).thenReturn(Optional.empty())

        assertThatThrownBy { recipeService.createRecipe(99L, request) }
            .isInstanceOf(NotFoundException::class.java)
        verify(recipeRepository, never()).save(any<Recipe>())
    }

    @Test
    fun `존재하는 레시피를 수정한다`() {
        whenever(recipeRepository.findById(5L))
            .thenReturn(Optional.of(Recipe(id = 5, menu = menu, stepOrder = 1, description = "이전")))
        whenever(recipeRepository.save(any<Recipe>()))
            .thenReturn(Recipe(id = 5, menu = menu, stepOrder = 1, description = "김치를 볶는다", tip = "센 불"))

        val result = recipeService.updateRecipe(5L, request)

        assertThat(result.description).isEqualTo("김치를 볶는다")
    }

    @Test
    fun `없는 레시피 수정 시 NotFoundException`() {
        whenever(recipeRepository.findById(99L)).thenReturn(Optional.empty())

        assertThatThrownBy { recipeService.updateRecipe(99L, request) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `없는 레시피 삭제 시 NotFoundException`() {
        whenever(recipeRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { recipeService.deleteRecipe(99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(recipeRepository, never()).deleteById(any<Long>())
    }
}
