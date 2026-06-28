package com.ttukttakbap.backend.menu.dto

import com.ttukttakbap.backend.menu.MenuIngredient
import java.math.BigDecimal

// 어드민용 메뉴-재료 연결 응답. 연결 id를 포함해 이후 삭제에 사용한다.
data class MenuIngredientLinkResponse(
    val id: Long,
    val menuId: Long,
    val ingredientId: Long,
    val ingredientName: String,
    val amountPerPerson: BigDecimal,
    val unit: String,
) {
    companion object {
        fun from(link: MenuIngredient) = MenuIngredientLinkResponse(
            id = link.id,
            menuId = link.menu.id,
            ingredientId = link.ingredient.id,
            ingredientName = link.ingredient.name,
            amountPerPerson = link.amountPerPerson,
            unit = link.unit,
        )
    }
}
