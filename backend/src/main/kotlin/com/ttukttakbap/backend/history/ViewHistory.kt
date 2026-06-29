package com.ttukttakbap.backend.history

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "view_history",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "menu_id"])],
)
class ViewHistory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "menu_id")
    val menuId: Long,
    val viewedAt: LocalDateTime = LocalDateTime.now(),
)
