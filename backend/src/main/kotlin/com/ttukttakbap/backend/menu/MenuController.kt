package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.menu.dto.MenuIngredientResponse
import com.ttukttakbap.backend.menu.dto.MenuResponse
import com.ttukttakbap.backend.recipe.dto.RecipeStepResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/menus")
class MenuController(private val menuService: MenuService) {

    @GetMapping
    fun getMenus(): ResponseEntity<List<MenuResponse>> =
        ResponseEntity.ok(menuService.getMenus())

    @GetMapping("/{menuId}")
    fun getMenu(@PathVariable menuId: Long): ResponseEntity<MenuResponse> =
        ResponseEntity.ok(menuService.getMenu(menuId))

    @GetMapping("/{menuId}/ingredients")
    fun getMenuIngredients(
        @PathVariable menuId: Long,
        @RequestParam(defaultValue = "2") people: Int,
    ): ResponseEntity<List<MenuIngredientResponse>> =
        ResponseEntity.ok(menuService.getMenuIngredients(menuId, people))

    @GetMapping("/{menuId}/recipe")
    fun getMenuRecipe(@PathVariable menuId: Long): ResponseEntity<List<RecipeStepResponse>> =
        ResponseEntity.ok(menuService.getMenuRecipe(menuId))
}
