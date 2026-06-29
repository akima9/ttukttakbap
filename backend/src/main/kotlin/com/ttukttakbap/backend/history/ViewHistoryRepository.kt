package com.ttukttakbap.backend.history

import org.springframework.data.jpa.repository.JpaRepository

interface ViewHistoryRepository : JpaRepository<ViewHistory, Long> {
    fun findByUserIdAndMenuId(userId: Long, menuId: Long): ViewHistory?

    fun findTop20ByUserIdOrderByViewedAtDesc(userId: Long): List<ViewHistory>
}
