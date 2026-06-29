package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.dto.PageResponse
import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.favorite.FavoriteRepository
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuRequest
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.RecipeRepository
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class MenuService(
    private val menuRepository: MenuRepository,
    private val menuIngredientRepository: MenuIngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val favoriteRepository: FavoriteRepository,
) {
    fun getMenus(
        category: Category?,
        difficulty: Difficulty?,
        maxCookTime: Int?,
        pageable: Pageable,
        userId: Long?,
    ): PageResponse<MenuResponse> {
        val favoriteIds = favoriteMenuIds(userId)
        return PageResponse.from(
            menuRepository.search(category, difficulty, maxCookTime, pageable)
                .map { MenuResponse.from(it, it.id in favoriteIds) },
        )
    }

    // 게스트 추천: 조건 필터 후 조리시간 짧은 순(동률 시 id) 정렬. 냉장고 기반 정렬은 Phase 4에서.
    fun recommend(
        category: Category?,
        difficulty: Difficulty?,
        maxCookTime: Int?,
        pageable: Pageable,
        userId: Long?,
    ): PageResponse<MenuResponse> {
        val sorted = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(Sort.Direction.ASC, "cookTimeMinutes").and(Sort.by(Sort.Direction.ASC, "id")),
        )
        val favoriteIds = favoriteMenuIds(userId)
        return PageResponse.from(
            menuRepository.search(category, difficulty, maxCookTime, sorted)
                .map { MenuResponse.from(it, it.id in favoriteIds) },
        )
    }

    fun getMenu(menuId: Long, userId: Long?): MenuResponse {
        val menu = menuRepository.findById(menuId)
            .orElseThrow { NotFoundException("해당 메뉴를 찾을 수 없습니다.") }
        return MenuResponse.from(menu, menu.id in favoriteMenuIds(userId))
    }

    // 로그인 사용자면 즐겨찾기한 메뉴 id 집합, 비로그인이면 빈 집합.
    private fun favoriteMenuIds(userId: Long?): Set<Long> =
        if (userId == null) emptySet() else favoriteRepository.findMenuIdsByUserId(userId).toSet()

    fun getMenuIngredients(menuId: Long, people: Int): List<MenuIngredientResponse> {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        return menuIngredientRepository.findByMenuId(menuId)
            .map { MenuIngredientResponse.from(it, people) }
    }

    fun getMenuRecipe(menuId: Long): List<RecipeStepResponse> {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        return recipeRepository.findByMenuIdOrderByStepOrder(menuId)
            .map { RecipeStepResponse.from(it) }
    }

    fun getCategories(): List<String> = Category.entries.map { it.label }

    fun createMenu(request: MenuRequest): MenuResponse {
        val saved = menuRepository.save(request.toMenu())
        return MenuResponse.from(saved)
    }

    fun updateMenu(menuId: Long, request: MenuRequest): MenuResponse {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        val saved = menuRepository.save(request.toMenu(menuId))
        return MenuResponse.from(saved)
    }

    fun deleteMenu(menuId: Long) {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        menuRepository.deleteById(menuId)
    }

    private fun MenuRequest.toMenu(id: Long = 0): Menu = Menu(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        cookTimeMinutes = cookTimeMinutes,
        difficulty = parseDifficulty(difficulty),
        category = Category.fromLabel(category),
    )

    private fun parseDifficulty(value: String): Difficulty =
        try {
            Difficulty.valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("유효하지 않은 난이도입니다: $value")
        }
}
