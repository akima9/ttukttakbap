package com.ttukttakbap.backend.recipe

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findByMenuIdOrderByStepOrder(menuId: Long): List<Recipe>
}
