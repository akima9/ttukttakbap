package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.Ingredient
import com.ttukttakbap.backend.ingredient.IngredientRepository
import com.ttukttakbap.backend.menu.dto.MenuIngredientRequest
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
import java.math.BigDecimal
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MenuIngredientServiceTest {

    @Mock lateinit var menuIngredientRepository: MenuIngredientRepository
    @Mock lateinit var menuRepository: MenuRepository
    @Mock lateinit var ingredientRepository: IngredientRepository
    @InjectMocks lateinit var menuIngredientService: MenuIngredientService

    private val menu = Menu(
        id = 1, name = "김치찌개", description = "얼큰", imageUrl = "",
        cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE,
    )
    private val ingredient = Ingredient(id = 2, name = "김치", purchaseUnit = "1포기")
    private val request = MenuIngredientRequest(ingredientId = 2, amountPerPerson = BigDecimal("150.0"), unit = "g")

    @Test
    fun `메뉴별 재료 연결 목록을 id와 함께 조회한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(menuIngredientRepository.findByMenuId(1L))
            .thenReturn(listOf(MenuIngredient(id = 9, menu = menu, ingredient = ingredient, amountPerPerson = BigDecimal("150.0"), unit = "g")))

        val result = menuIngredientService.getLinks(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(9L)
        assertThat(result[0].ingredientName).isEqualTo("김치")
    }

    @Test
    fun `없는 메뉴의 연결 목록 조회 시 NotFoundException`() {
        whenever(menuRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { menuIngredientService.getLinks(99L) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `메뉴에 재료를 연결한다`() {
        whenever(menuRepository.findById(1L)).thenReturn(Optional.of(menu))
        whenever(ingredientRepository.findById(2L)).thenReturn(Optional.of(ingredient))
        whenever(menuIngredientRepository.save(any<MenuIngredient>()))
            .thenReturn(MenuIngredient(id = 9, menu = menu, ingredient = ingredient, amountPerPerson = BigDecimal("150.0"), unit = "g"))

        val result = menuIngredientService.linkIngredient(1L, request)

        assertThat(result.id).isEqualTo(9L)
        assertThat(result.ingredientName).isEqualTo("김치")
        assertThat(result.unit).isEqualTo("g")
    }

    @Test
    fun `없는 메뉴에 연결 시 NotFoundException`() {
        whenever(menuRepository.findById(99L)).thenReturn(Optional.empty())

        assertThatThrownBy { menuIngredientService.linkIngredient(99L, request) }
            .isInstanceOf(NotFoundException::class.java)
        verify(menuIngredientRepository, never()).save(any<MenuIngredient>())
    }

    @Test
    fun `없는 재료로 연결 시 NotFoundException`() {
        whenever(menuRepository.findById(1L)).thenReturn(Optional.of(menu))
        whenever(ingredientRepository.findById(2L)).thenReturn(Optional.empty())

        assertThatThrownBy { menuIngredientService.linkIngredient(1L, request) }
            .isInstanceOf(NotFoundException::class.java)
        verify(menuIngredientRepository, never()).save(any<MenuIngredient>())
    }

    @Test
    fun `없는 연결 삭제 시 NotFoundException`() {
        whenever(menuIngredientRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { menuIngredientService.unlinkIngredient(99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(menuIngredientRepository, never()).deleteById(any<Long>())
    }
}
