package com.ttukttakbap.backend.ingredient.dto

data class IngredientRequest(
    val name: String,
    val purchaseUnit: String,
    val purchaseLocation: String? = null,
    val coupangUrl: String? = null,
)
