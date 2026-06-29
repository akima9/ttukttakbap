package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.dto.PageResponse
import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.favorite.FavoriteRepository
import com.ttukttakbap.backend.fridge.FridgeRepository
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuRequest
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.RecipeRepository
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.springframework.data.domain.PageImpl
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
    private val fridgeRepository: FridgeRepository,
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

    // 추천: useMyFridge면 냉장고 보유 재료와 겹치는 수가 많은 순, 아니면 조리시간 짧은 순(동률 시 id).
    fun recommend(
        category: Category?,
        difficulty: Difficulty?,
        maxCookTime: Int?,
        pageable: Pageable,
        userId: Long?,
        useMyFridge: Boolean,
    ): PageResponse<MenuResponse> {
        val fridgeIds = if (useMyFridge && userId != null) fridgeRepository.findIngredientIdsByUserId(userId) else emptyList()
        val favoriteIds = favoriteMenuIds(userId)
        if (fridgeIds.isEmpty()) {
            val sorted = PageRequest.of(
                pageable.pageNumber,
                pageable.pageSize,
                Sort.by(Sort.Direction.ASC, "cookTimeMinutes").and(Sort.by(Sort.Direction.ASC, "id")),
            )
            return PageResponse.from(
                menuRepository.search(category, difficulty, maxCookTime, sorted)
                    .map { MenuResponse.from(it, it.id in favoriteIds) },
            )
        }
        return recommendByFridge(category, difficulty, maxCookTime, pageable, fridgeIds, favoriteIds)
    }

    // 냉장고 매칭 수 desc, 동률 시 조리시간 asc, id asc. 매칭 집계는 한 번에 조회 후 메모리에서 정렬·페이징.
    private fun recommendByFridge(
        category: Category?,
        difficulty: Difficulty?,
        maxCookTime: Int?,
        pageable: Pageable,
        fridgeIds: List<Long>,
        favoriteIds: Set<Long>,
    ): PageResponse<MenuResponse> {
        val candidates = menuRepository.search(category, difficulty, maxCookTime, Pageable.unpaged()).content
        val matchCount = menuIngredientRepository.countMatchesByIngredientIds(fridgeIds)
            .associate { (it[0] as Long) to (it[1] as Long) }
        val ranked = candidates.sortedWith(
            compareByDescending<Menu> { matchCount[it.id] ?: 0L }
                .thenBy { it.cookTimeMinutes }
                .thenBy { it.id },
        )
        val pageReq = PageRequest.of(pageable.pageNumber, pageable.pageSize)
        val pageContent = ranked.drop(pageReq.offset.toInt()).take(pageReq.pageSize)
        return PageResponse.from(
            PageImpl(pageContent, pageReq, ranked.size.toLong())
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
