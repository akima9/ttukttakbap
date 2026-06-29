package com.ttukttakbap.backend.favorite

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FavoriteRepository : JpaRepository<Favorite, Long> {
    fun existsByUserIdAndMenuId(userId: Long, menuId: Long): Boolean

    fun deleteByUserIdAndMenuId(userId: Long, menuId: Long)

    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Favorite>

    @Query("SELECT f.menuId FROM Favorite f WHERE f.userId = :userId")
    fun findMenuIdsByUserId(@Param("userId") userId: Long): List<Long>
}
