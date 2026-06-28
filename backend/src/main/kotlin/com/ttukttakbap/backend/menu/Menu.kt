package com.ttukttakbap.backend.menu

import jakarta.persistence.*

enum class Difficulty { EASY, MEDIUM, HARD }

@Entity
class Menu(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    @Column(columnDefinition = "TEXT")
    val description: String,
    val imageUrl: String,
    val cookTimeMinutes: Int,
    @Enumerated(EnumType.STRING)
    val difficulty: Difficulty,
    @Enumerated(EnumType.STRING)
    val category: Category,
)
