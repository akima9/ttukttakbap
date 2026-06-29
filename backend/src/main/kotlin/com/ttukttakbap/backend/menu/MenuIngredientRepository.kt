package com.ttukttakbap.backend.menu

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MenuIngredientRepository : JpaRepository<MenuIngredient, Long> {
    fun findByMenuId(menuId: Long): List<MenuIngredient>

    // 냉장고 보유 재료(ingredientIds)와 겹치는 재료 수를 메뉴별로 집계: [menuId, matchCount]
    @Query(
        """
        SELECT mi.menu.id, COUNT(mi)
        FROM MenuIngredient mi
        WHERE mi.ingredient.id IN :ingredientIds
        GROUP BY mi.menu.id
        """,
    )
    fun countMatchesByIngredientIds(@Param("ingredientIds") ingredientIds: List<Long>): List<Array<Any>>
}
