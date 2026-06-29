package com.ttukttakbap.backend.fridge

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "fridge_item",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "ingredient_id"])],
)
class FridgeItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "ingredient_id")
    val ingredientId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
