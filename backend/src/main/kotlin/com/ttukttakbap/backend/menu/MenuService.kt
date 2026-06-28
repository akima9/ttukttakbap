package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.dto.PageResponse
import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
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
) {
    fun getMenus(
        category: Category?,
        difficulty: Difficulty?,
        maxCookTime: Int?,
        pageable: Pageable,
    ): PageResponse<MenuResponse> =
        PageResponse.from(
            menuRepository.search(category, difficulty, maxCookTime, pageable).map { MenuResponse.from(it) },
        )

    // 게스트 추천: 조건 필터 후 조리시간 짧은 순(동률 시 id) 정렬. 냉장고 기반 정렬은 Phase 4에서.
    fun recommend(
        category: Category?,
        difficulty: Difficulty?,
        maxCookTime: Int?,
        pageable: Pageable,
    ): PageResponse<MenuResponse> {
        val sorted = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(Sort.Direction.ASC, "cookTimeMinutes").and(Sort.by(Sort.Direction.ASC, "id")),
        )
        return PageResponse.from(
            menuRepository.search(category, difficulty, maxCookTime, sorted).map { MenuResponse.from(it) },
        )
    }

    fun getMenu(menuId: Long): MenuResponse {
        val menu = menuRepository.findById(menuId)
            .orElseThrow { NotFoundException("해당 메뉴를 찾을 수 없습니다.") }
        return MenuResponse.from(menu)
    }

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
}
