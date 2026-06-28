package com.ttukttakbap.backend.ingredient

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.dto.IngredientRequest
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
class IngredientServiceTest {

    @Mock lateinit var ingredientRepository: IngredientRepository
    @InjectMocks lateinit var ingredientService: IngredientService

    private val sample = Ingredient(id = 1, name = "김치", purchaseUnit = "1포기", purchaseLocation = "마트")
    private val request = IngredientRequest(name = "김치", purchaseUnit = "1포기", purchaseLocation = "마트")

    @Test
    fun `재료를 생성한다`() {
        whenever(ingredientRepository.save(any<Ingredient>())).thenReturn(sample)

        val result = ingredientService.createIngredient(request)

        assertThat(result.name).isEqualTo("김치")
        assertThat(result.purchaseUnit).isEqualTo("1포기")
    }

    @Test
    fun `존재하는 재료를 수정한다`() {
        whenever(ingredientRepository.existsById(1L)).thenReturn(true)
        whenever(ingredientRepository.save(any<Ingredient>())).thenReturn(sample)

        val result = ingredientService.updateIngredient(1L, request)

        assertThat(result.name).isEqualTo("김치")
    }

    @Test
    fun `존재하지 않는 재료 수정 시 NotFoundException`() {
        whenever(ingredientRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { ingredientService.updateIngredient(99L, request) }
            .isInstanceOf(NotFoundException::class.java)
        verify(ingredientRepository, never()).save(any<Ingredient>())
    }

    @Test
    fun `존재하지 않는 재료 삭제 시 NotFoundException`() {
        whenever(ingredientRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { ingredientService.deleteIngredient(99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(ingredientRepository, never()).deleteById(any<Long>())
    }
}
