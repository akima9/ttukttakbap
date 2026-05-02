package com.ttukttakbap.backend.recipe.dto

import com.ttukttakbap.backend.recipe.Recipe

data class RecipeStepResponse(
    val stepOrder: Int,
    val description: String,
    val tip: String?,
) {
    companion object {
        fun from(recipe: Recipe) = RecipeStepResponse(
            stepOrder = recipe.stepOrder,
            description = recipe.description,
            tip = recipe.tip,
        )
    }
}
