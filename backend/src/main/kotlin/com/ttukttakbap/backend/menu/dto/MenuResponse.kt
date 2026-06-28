package com.ttukttakbap.backend.menu.dto

import com.ttukttakbap.backend.menu.Menu

data class MenuResponse(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val category: String,
    val isFavorite: Boolean = false,
) {
    companion object {
        fun from(menu: Menu, isFavorite: Boolean = false) = MenuResponse(
            id = menu.id,
            name = menu.name,
            description = menu.description,
            imageUrl = menu.imageUrl,
            cookTimeMinutes = menu.cookTimeMinutes,
            difficulty = menu.difficulty.name,
            category = menu.category.label,
            isFavorite = isFavorite,
        )
    }
}
