package com.ttukttakbap.backend.recipe

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.MenuRepository
import com.ttukttakbap.backend.recipe.dto.RecipeRequest
import com.ttukttakbap.backend.recipe.dto.RecipeResponse
import org.springframework.stereotype.Service

@Service
class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val menuRepository: MenuRepository,
) {
    fun createRecipe(menuId: Long, request: RecipeRequest): RecipeResponse {
        val menu = menuRepository.findById(menuId)
            .orElseThrow { NotFoundException("해당 메뉴를 찾을 수 없습니다.") }
        val saved = recipeRepository.save(
            Recipe(menu = menu, stepOrder = request.stepOrder, description = request.description, tip = request.tip),
        )
        return RecipeResponse.from(saved)
    }

    fun updateRecipe(recipeId: Long, request: RecipeRequest): RecipeResponse {
        val existing = recipeRepository.findById(recipeId)
            .orElseThrow { NotFoundException("해당 레시피를 찾을 수 없습니다.") }
        val saved = recipeRepository.save(
            Recipe(
                id = existing.id,
                menu = existing.menu,
                stepOrder = request.stepOrder,
                description = request.description,
                tip = request.tip,
            ),
        )
        return RecipeResponse.from(saved)
    }

    fun deleteRecipe(recipeId: Long) {
        if (!recipeRepository.existsById(recipeId)) throw NotFoundException("해당 레시피를 찾을 수 없습니다.")
        recipeRepository.deleteById(recipeId)
    }
}
