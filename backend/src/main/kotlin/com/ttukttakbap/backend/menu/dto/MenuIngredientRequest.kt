package com.ttukttakbap.backend.menu.dto

import java.math.BigDecimal

// 메뉴에 재료를 연결. amountPerPerson은 1인분 기준 양.
data class MenuIngredientRequest(
    val ingredientId: Long,
    val amountPerPerson: BigDecimal,
    val unit: String,
)
