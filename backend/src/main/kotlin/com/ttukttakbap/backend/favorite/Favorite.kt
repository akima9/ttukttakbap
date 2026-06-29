package com.ttukttakbap.backend.favorite

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "menu_id"])],
)
class Favorite(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "menu_id")
    val menuId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
