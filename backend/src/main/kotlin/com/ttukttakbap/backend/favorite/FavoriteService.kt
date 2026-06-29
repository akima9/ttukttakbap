package com.ttukttakbap.backend.favorite

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.MenuRepository
import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val menuRepository: MenuRepository,
) {
    // 이미 즐겨찾기한 메뉴면 무시(멱등). 메뉴가 없으면 404.
    @Transactional
    fun addFavorite(userId: Long, menuId: Long) {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        if (!favoriteRepository.existsByUserIdAndMenuId(userId, menuId)) {
            favoriteRepository.save(Favorite(userId = userId, menuId = menuId))
        }
    }

    @Transactional
    fun removeFavorite(userId: Long, menuId: Long) {
        favoriteRepository.deleteByUserIdAndMenuId(userId, menuId)
    }

    @Transactional(readOnly = true)
    fun getFavorites(userId: Long): List<MenuResponse> {
        val menuIds = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).map { it.menuId }
        val menusById = menuRepository.findAllById(menuIds).associateBy { it.id }
        return menuIds.mapNotNull { id -> menusById[id]?.let { MenuResponse.from(it, isFavorite = true) } }
    }
}
