package com.ttukttakbap.backend.ingredient

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.dto.IngredientRequest
import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.springframework.stereotype.Service

@Service
class IngredientService(private val ingredientRepository: IngredientRepository) {

    fun getIngredients(): List<IngredientResponse> =
        ingredientRepository.findAll().map { IngredientResponse.from(it) }

    fun createIngredient(request: IngredientRequest): IngredientResponse =
        IngredientResponse.from(ingredientRepository.save(request.toIngredient()))

    fun updateIngredient(ingredientId: Long, request: IngredientRequest): IngredientResponse {
        if (!ingredientRepository.existsById(ingredientId)) throw NotFoundException("해당 재료를 찾을 수 없습니다.")
        return IngredientResponse.from(ingredientRepository.save(request.toIngredient(ingredientId)))
    }

    fun deleteIngredient(ingredientId: Long) {
        if (!ingredientRepository.existsById(ingredientId)) throw NotFoundException("해당 재료를 찾을 수 없습니다.")
        ingredientRepository.deleteById(ingredientId)
    }

    private fun IngredientRequest.toIngredient(id: Long = 0): Ingredient = Ingredient(
        id = id,
        name = name,
        purchaseUnit = purchaseUnit,
        purchaseLocation = purchaseLocation,
        coupangUrl = coupangUrl,
    )
}
