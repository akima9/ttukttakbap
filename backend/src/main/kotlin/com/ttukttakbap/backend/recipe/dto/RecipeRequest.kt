package com.ttukttakbap.backend.recipe.dto

data class RecipeRequest(
    val stepOrder: Int,
    val description: String,
    val tip: String? = null,
)
