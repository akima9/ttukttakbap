package com.ttukttakbap.backend.menu

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MenuRepository : JpaRepository<Menu, Long> {
    @Query(
        """
        SELECT m FROM Menu m
        WHERE (:category IS NULL OR m.category = :category)
          AND (:difficulty IS NULL OR m.difficulty = :difficulty)
          AND (:maxCookTime IS NULL OR m.cookTimeMinutes <= :maxCookTime)
        """,
    )
    fun search(
        @Param("category") category: Category?,
        @Param("difficulty") difficulty: Difficulty?,
        @Param("maxCookTime") maxCookTime: Int?,
        pageable: Pageable,
    ): Page<Menu>
}
