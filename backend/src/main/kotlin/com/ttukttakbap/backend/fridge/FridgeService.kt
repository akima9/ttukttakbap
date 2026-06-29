package com.ttukttakbap.backend.fridge

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.IngredientRepository
import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FridgeService(
    private val fridgeRepository: FridgeRepository,
    private val ingredientRepository: IngredientRepository,
) {
    // 이미 담긴 재료면 무시(멱등). 재료가 없으면 404.
    @Transactional
    fun addItem(userId: Long, ingredientId: Long) {
        if (!ingredientRepository.existsById(ingredientId)) throw NotFoundException("해당 재료를 찾을 수 없습니다.")
        if (!fridgeRepository.existsByUserIdAndIngredientId(userId, ingredientId)) {
            fridgeRepository.save(FridgeItem(userId = userId, ingredientId = ingredientId))
        }
    }

    @Transactional
    fun removeItem(userId: Long, ingredientId: Long) {
        fridgeRepository.deleteByUserIdAndIngredientId(userId, ingredientId)
    }

    @Transactional(readOnly = true)
    fun getFridge(userId: Long): List<IngredientResponse> {
        val ingredientIds = fridgeRepository.findByUserIdOrderByCreatedAtDesc(userId).map { it.ingredientId }
        val ingredientsById = ingredientRepository.findAllById(ingredientIds).associateBy { it.id }
        return ingredientIds.mapNotNull { id -> ingredientsById[id]?.let { IngredientResponse.from(it) } }
    }
}
