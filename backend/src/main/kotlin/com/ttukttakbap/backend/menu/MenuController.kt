package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.dto.PageResponse
import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class MenuController(private val menuService: MenuService) {

    @GetMapping("/menus")
    fun getMenus(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) difficulty: String?,
        @RequestParam(required = false) maxCookTime: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal userId: Long?,
    ): ResponseEntity<PageResponse<MenuResponse>> =
        ResponseEntity.ok(
            menuService.getMenus(parseCategory(category), parseDifficulty(difficulty), maxCookTime, PageRequest.of(page, size), userId),
        )

    @GetMapping("/menus/recommend")
    fun recommend(
        @RequestParam(defaultValue = "2") people: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) difficulty: String?,
        @RequestParam(required = false) maxCookTime: Int?,
        @RequestParam(defaultValue = "false") useMyFridge: Boolean,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal userId: Long?,
    ): ResponseEntity<PageResponse<MenuResponse>> {
        validatePeople(people)
        return ResponseEntity.ok(
            menuService.recommend(parseCategory(category), parseDifficulty(difficulty), maxCookTime, PageRequest.of(page, size), userId),
        )
    }

    @GetMapping("/menus/{menuId}")
    fun getMenu(
        @PathVariable menuId: Long,
        @AuthenticationPrincipal userId: Long?,
    ): ResponseEntity<MenuResponse> =
        ResponseEntity.ok(menuService.getMenu(menuId, userId))

    @GetMapping("/menus/{menuId}/ingredients")
    fun getMenuIngredients(
        @PathVariable menuId: Long,
        @RequestParam(defaultValue = "2") people: Int,
    ): ResponseEntity<List<MenuIngredientResponse>> {
        validatePeople(people)
        return ResponseEntity.ok(menuService.getMenuIngredients(menuId, people))
    }

    @GetMapping("/menus/{menuId}/recipe")
    fun getMenuRecipe(@PathVariable menuId: Long): ResponseEntity<List<RecipeStepResponse>> =
        ResponseEntity.ok(menuService.getMenuRecipe(menuId))

    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<List<String>> =
        ResponseEntity.ok(menuService.getCategories())

    private fun parseCategory(label: String?): Category? = label?.let { Category.fromLabel(it) }

    private fun parseDifficulty(value: String?): Difficulty? = value?.let {
        try {
            Difficulty.valueOf(it.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("유효하지 않은 난이도입니다: $it")
        }
    }

    private fun validatePeople(people: Int) {
        if (people !in 1..8) throw IllegalArgumentException("인원수는 1~8 사이여야 합니다.")
    }
}
