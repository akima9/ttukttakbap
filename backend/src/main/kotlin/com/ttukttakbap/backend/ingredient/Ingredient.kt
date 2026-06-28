package com.ttukttakbap.backend.ingredient

import jakarta.persistence.*

@Entity
class Ingredient(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val purchaseUnit: String,
    val purchaseLocation: String? = null,
    val coupangUrl: String? = null,
)
