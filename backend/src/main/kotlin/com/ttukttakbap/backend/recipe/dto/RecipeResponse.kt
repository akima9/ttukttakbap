package com.ttukttakbap.backend.recipe.dto

import com.ttukttakbap.backend.recipe.Recipe

// 어드민용 응답. 소비자용 RecipeStepResponse와 달리 id/menuId를 포함해 이후 수정·삭제에 사용한다.
data class RecipeResponse(
    val id: Long,
    val menuId: Long,
    val stepOrder: Int,
    val description: String,
    val tip: String?,
) {
    companion object {
        fun from(recipe: Recipe) = RecipeResponse(
            id = recipe.id,
            menuId = recipe.menu.id,
            stepOrder = recipe.stepOrder,
            description = recipe.description,
            tip = recipe.tip,
        )
    }
}
