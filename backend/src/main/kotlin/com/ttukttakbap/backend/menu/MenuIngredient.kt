package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.ingredient.Ingredient
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
class MenuIngredient(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    val menu: Menu,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    val ingredient: Ingredient,
    val amountPerPerson: BigDecimal,
    val unit: String,
)
