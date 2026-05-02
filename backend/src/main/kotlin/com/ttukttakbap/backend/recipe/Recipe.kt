package com.ttukttakbap.backend.recipe

import com.ttukttakbap.backend.menu.Menu
import jakarta.persistence.*

@Entity
class Recipe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    val menu: Menu,
    val stepOrder: Int,
    @Column(columnDefinition = "TEXT")
    val description: String,
    @Column(columnDefinition = "TEXT")
    val tip: String? = null,
)
