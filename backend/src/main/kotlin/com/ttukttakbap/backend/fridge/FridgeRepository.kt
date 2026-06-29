package com.ttukttakbap.backend.fridge

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FridgeRepository : JpaRepository<FridgeItem, Long> {
    fun existsByUserIdAndIngredientId(userId: Long, ingredientId: Long): Boolean

    fun deleteByUserIdAndIngredientId(userId: Long, ingredientId: Long)

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<FridgeItem>

    @Query("SELECT f.ingredientId FROM FridgeItem f WHERE f.userId = :userId")
    fun findIngredientIdsByUserId(@Param("userId") userId: Long): List<Long>
}
