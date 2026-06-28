package com.ttukttakbap.backend.ingredient.dto

import com.ttukttakbap.backend.ingredient.Ingredient

data class IngredientResponse(
    val id: Long,
    val name: String,
    val purchaseUnit: String,
    val purchaseLocation: String?,
    val coupangUrl: String?,
) {
    companion object {
        fun from(ingredient: Ingredient) = IngredientResponse(
            id = ingredient.id,
            name = ingredient.name,
            purchaseUnit = ingredient.purchaseUnit,
            purchaseLocation = ingredient.purchaseLocation,
            coupangUrl = ingredient.coupangUrl,
        )
    }
}
