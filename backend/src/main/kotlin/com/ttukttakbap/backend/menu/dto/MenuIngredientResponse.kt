package com.ttukttakbap.backend.menu.dto

import com.ttukttakbap.backend.menu.MenuIngredient
import java.math.BigDecimal

data class MenuIngredientResponse(
    val ingredientId: Long,
    val name: String,
    val amount: BigDecimal,
    val unit: String,
    val purchaseUnit: String,
    val purchaseLocation: String?,
) {
    companion object {
        fun from(menuIngredient: MenuIngredient, people: Int) = MenuIngredientResponse(
            ingredientId = menuIngredient.ingredient.id,
            name = menuIngredient.ingredient.name,
            amount = menuIngredient.amountPerPerson * people.toBigDecimal(),
            unit = menuIngredient.unit,
            purchaseUnit = menuIngredient.ingredient.purchaseUnit,
            purchaseLocation = menuIngredient.ingredient.purchaseLocation,
        )
    }
}
