package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.favorite.FavoriteRepository
import com.ttukttakbap.backend.ingredient.Ingredient
import com.ttukttakbap.backend.menu.dto.MenuRequest
import com.ttukttakbap.backend.recipe.Recipe
import com.ttukttakbap.backend.recipe.RecipeRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MenuServiceTest {

    @Mock lateinit var menuRepository: MenuRepository
    @Mock lateinit var menuIngredientRepository: MenuIngredientRepository
    @Mock lateinit var recipeRepository: RecipeRepository
    @Mock lateinit var favoriteRepository: FavoriteRepository

    @InjectMocks lateinit var menuService: MenuService

    private val sampleMenu = Menu(
        id = 1, name = "김치찌개", description = "얼큰한 찌개", imageUrl = "",
        cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE,
    )

    @Test
    fun `필터 조건으로 페이지 형태의 메뉴 목록을 반환한다`() {
        whenever(menuRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any()))
            .thenReturn(PageImpl(listOf(sampleMenu)))

        val result = menuService.getMenus(null, null, null, PageRequest.of(0, 20), null)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("김치찌개")
        assertThat(result.content[0].category).isEqualTo("찌개")
        assertThat(result.content[0].isFavorite).isFalse()
        assertThat(result.totalElements).isEqualTo(1)
    }

    @Test
    fun `로그인 사용자의 즐겨찾기한 메뉴는 isFavorite가 true다`() {
        whenever(menuRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any()))
            .thenReturn(PageImpl(listOf(sampleMenu)))
        whenever(favoriteRepository.findMenuIdsByUserId(1L)).thenReturn(listOf(1L))

        val result = menuService.getMenus(null, null, null, PageRequest.of(0, 20), userId = 1L)

        assertThat(result.content[0].isFavorite).isTrue()
    }

    @Test
    fun `추천 목록을 반환한다`() {
        whenever(menuRepository.search(anyOrNull(), anyOrNull(), anyOrNull(), any()))
            .thenReturn(PageImpl(listOf(sampleMenu)))

        val result = menuService.recommend(null, null, null, PageRequest.of(0, 20), null)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].name).isEqualTo("김치찌개")
    }

    @Test
    fun `카테고리 목록은 확정된 8종을 반환한다`() {
        val result = menuService.getCategories()

        assertThat(result).containsExactly("찌개", "국", "밥", "면", "반찬", "안주", "디저트", "메인요리")
    }

    @Test
    fun `메뉴 단건을 조회한다`() {
        whenever(menuRepository.findById(1L)).thenReturn(Optional.of(sampleMenu))

        val result = menuService.getMenu(1L, null)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.name).isEqualTo("김치찌개")
    }

    @Test
    fun `존재하지 않는 메뉴 조회 시 NotFoundException을 던진다`() {
        whenever(menuRepository.findById(99L)).thenReturn(Optional.empty())

        assertThatThrownBy { menuService.getMenu(99L, null) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("해당 메뉴를 찾을 수 없습니다.")
    }

    @Test
    fun `인원 수에 맞게 재료 양을 계산한다 - 양 단위는 소수 첫째 자리 반올림`() {
        val ingredient = Ingredient(id = 1, name = "김치", purchaseUnit = "1포기")
        val menuIngredient = MenuIngredient(id = 1, menu = sampleMenu, ingredient = ingredient, amountPerPerson = BigDecimal("150.00"), unit = "g")
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(menuIngredientRepository.findByMenuId(1L)).thenReturn(listOf(menuIngredient))

        val result = menuService.getMenuIngredients(1L, people = 2)

        assertThat(result).hasSize(1)
        assertThat(result[0].requiredAmount).isEqualByComparingTo(BigDecimal("300.0"))
        assertThat(result[0].unit).isEqualTo("g")
    }

    @Test
    fun `셀 수 있는 단위는 올림 처리한다`() {
        val ingredient = Ingredient(id = 2, name = "계란", purchaseUnit = "10개")
        val menuIngredient = MenuIngredient(id = 2, menu = sampleMenu, ingredient = ingredient, amountPerPerson = BigDecimal("0.5"), unit = "개")
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(menuIngredientRepository.findByMenuId(1L)).thenReturn(listOf(menuIngredient))

        val result = menuService.getMenuIngredients(1L, people = 3)

        // 0.5 * 3 = 1.5 → 올림 → 2
        assertThat(result[0].requiredAmount).isEqualByComparingTo(BigDecimal("2"))
    }

    private val sampleRequest = MenuRequest(
        name = "김치찌개", description = "얼큰한 찌개", imageUrl = "",
        cookTimeMinutes = 30, difficulty = "EASY", category = "찌개",
    )

    @Test
    fun `메뉴를 생성한다`() {
        whenever(menuRepository.save(any<Menu>())).thenReturn(sampleMenu)

        val result = menuService.createMenu(sampleRequest)

        assertThat(result.name).isEqualTo("김치찌개")
        assertThat(result.category).isEqualTo("찌개")
    }

    @Test
    fun `유효하지 않은 카테고리로 생성하면 IllegalArgumentException을 던진다`() {
        assertThatThrownBy { menuService.createMenu(sampleRequest.copy(category = "없는카테고리")) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `유효하지 않은 난이도로 생성하면 IllegalArgumentException을 던진다`() {
        assertThatThrownBy { menuService.createMenu(sampleRequest.copy(difficulty = "VERY_HARD")) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `존재하는 메뉴를 수정한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(menuRepository.save(any<Menu>())).thenReturn(sampleMenu)

        val result = menuService.updateMenu(1L, sampleRequest)

        assertThat(result.name).isEqualTo("김치찌개")
    }

    @Test
    fun `존재하지 않는 메뉴 수정 시 NotFoundException을 던진다`() {
        whenever(menuRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { menuService.updateMenu(99L, sampleRequest) }
            .isInstanceOf(NotFoundException::class.java)
        verify(menuRepository, never()).save(any<Menu>())
    }

    @Test
    fun `메뉴를 삭제한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)

        menuService.deleteMenu(1L)

        verify(menuRepository).deleteById(1L)
    }

    @Test
    fun `존재하지 않는 메뉴 삭제 시 NotFoundException을 던진다`() {
        whenever(menuRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { menuService.deleteMenu(99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(menuRepository, never()).deleteById(any())
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
