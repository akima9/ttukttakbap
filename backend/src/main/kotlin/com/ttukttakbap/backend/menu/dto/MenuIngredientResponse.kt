package com.ttukttakbap.backend.menu.dto

import com.ttukttakbap.backend.menu.MenuIngredient
import java.math.BigDecimal
import java.math.RoundingMode

data class MenuIngredientResponse(
    val ingredientId: Long,
    val name: String,
    val requiredAmount: BigDecimal,
    val unit: String,
    val purchaseUnit: String,
    val purchaseLocation: String?,
    val coupangUrl: String?,
) {
    companion object {
        // 셀 수 있는 단위는 반쪽을 살 수 없으므로 올림, 그 외 양 단위는 소수 첫째 자리 반올림
        private val COUNTABLE_UNITS = setOf(
            "개", "장", "알", "마리", "봉", "모", "쪽", "줄", "통", "포기", "단", "톨", "팩", "조각",
        )

        fun roundAmount(rawAmount: BigDecimal, unit: String): BigDecimal =
            if (unit in COUNTABLE_UNITS) rawAmount.setScale(0, RoundingMode.CEILING)
            else rawAmount.setScale(1, RoundingMode.HALF_UP)

        fun from(menuIngredient: MenuIngredient, people: Int) = MenuIngredientResponse(
            ingredientId = menuIngredient.ingredient.id,
            name = menuIngredient.ingredient.name,
            requiredAmount = roundAmount(menuIngredient.amountPerPerson * people.toBigDecimal(), menuIngredient.unit),
            unit = menuIngredient.unit,
            purchaseUnit = menuIngredient.ingredient.purchaseUnit,
            purchaseLocation = menuIngredient.ingredient.purchaseLocation,
            coupangUrl = menuIngredient.ingredient.coupangUrl,
        )
    }
}
