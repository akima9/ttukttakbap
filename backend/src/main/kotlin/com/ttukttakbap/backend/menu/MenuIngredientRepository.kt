package com.ttukttakbap.backend.menu

import org.springframework.data.jpa.repository.JpaRepository

interface MenuIngredientRepository : JpaRepository<MenuIngredient, Long> {
    fun findByMenuId(menuId: Long): List<MenuIngredient>
}
