package com.ttukttakbap.backend.history

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.favorite.FavoriteRepository
import com.ttukttakbap.backend.menu.MenuRepository
import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ViewHistoryService(
    private val viewHistoryRepository: ViewHistoryRepository,
    private val menuRepository: MenuRepository,
    private val favoriteRepository: FavoriteRepository,
) {
    // 같은 메뉴를 다시 보면 기존 기록의 조회 시각만 갱신(같은 id로 재저장). 메뉴가 없으면 404.
    @Transactional
    fun recordView(userId: Long, menuId: Long) {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        val existing = viewHistoryRepository.findByUserIdAndMenuId(userId, menuId)
        viewHistoryRepository.save(
            ViewHistory(id = existing?.id ?: 0, userId = userId, menuId = menuId, viewedAt = LocalDateTime.now()),
        )
    }

    @Transactional(readOnly = true)
    fun getHistory(userId: Long): List<MenuResponse> {
        val menuIds = viewHistoryRepository.findTop20ByUserIdOrderByViewedAtDesc(userId).map { it.menuId }
        val menusById = menuRepository.findAllById(menuIds).associateBy { it.id }
        val favoriteIds = favoriteRepository.findMenuIdsByUserId(userId).toSet()
        return menuIds.mapNotNull { id -> menusById[id]?.let { MenuResponse.from(it, it.id in favoriteIds) } }
    }
}
