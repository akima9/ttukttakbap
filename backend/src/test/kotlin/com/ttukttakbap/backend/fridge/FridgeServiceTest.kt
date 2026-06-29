package com.ttukttakbap.backend.fridge

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.Ingredient
import com.ttukttakbap.backend.ingredient.IngredientRepository
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

@ExtendWith(MockitoExtension::class)
class FridgeServiceTest {

    @Mock lateinit var fridgeRepository: FridgeRepository
    @Mock lateinit var ingredientRepository: IngredientRepository

    @InjectMocks lateinit var service: FridgeService

    private val ingredient = Ingredient(id = 1, name = "김치", purchaseUnit = "1포기")

    @Test
    fun `냉장고에 재료를 담는다`() {
        whenever(ingredientRepository.existsById(1L)).thenReturn(true)
        whenever(fridgeRepository.existsByUserIdAndIngredientId(1L, 1L)).thenReturn(false)

        service.addItem(1L, 1L)

        verify(fridgeRepository).save(any<FridgeItem>())
    }

    @Test
    fun `이미 담긴 재료는 다시 저장하지 않는다`() {
        whenever(ingredientRepository.existsById(1L)).thenReturn(true)
        whenever(fridgeRepository.existsByUserIdAndIngredientId(1L, 1L)).thenReturn(true)

        service.addItem(1L, 1L)

        verify(fridgeRepository, never()).save(any<FridgeItem>())
    }

    @Test
    fun `존재하지 않는 재료를 담으면 NotFoundException을 던진다`() {
        whenever(ingredientRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { service.addItem(1L, 99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(fridgeRepository, never()).save(any<FridgeItem>())
    }

    @Test
    fun `냉장고에서 재료를 뺀다`() {
        service.removeItem(1L, 1L)

        verify(fridgeRepository).deleteByUserIdAndIngredientId(1L, 1L)
    }

    @Test
    fun `냉장고 재료 목록을 최신순으로 반환한다`() {
        whenever(fridgeRepository.findByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(listOf(FridgeItem(id = 1, userId = 1L, ingredientId = 1L)))
        whenever(ingredientRepository.findAllById(listOf(1L))).thenReturn(listOf(ingredient))

        val result = service.getFridge(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("김치")
    }
}
