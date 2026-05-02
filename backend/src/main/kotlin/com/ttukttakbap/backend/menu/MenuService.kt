package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.RecipeRepository
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.springframework.stereotype.Service

@Service
class MenuService(
    private val menuRepository: MenuRepository,
    private val menuIngredientRepository: MenuIngredientRepository,
    private val recipeRepository: RecipeRepository,
) {
    fun getMenus(): List<MenuResponse> =
        menuRepository.findAll().map { MenuResponse.from(it) }

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
}
