package com.ttukttakbap.backend.recipe

import com.ttukttakbap.backend.recipe.dto.RecipeRequest
import com.ttukttakbap.backend.recipe.dto.RecipeResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminRecipeController(private val recipeService: RecipeService) {

    @GetMapping("/menus/{menuId}/recipes")
    fun list(@PathVariable menuId: Long): ResponseEntity<List<RecipeResponse>> =
        ResponseEntity.ok(recipeService.getRecipes(menuId))

    @PostMapping("/menus/{menuId}/recipes")
    fun create(@PathVariable menuId: Long, @RequestBody request: RecipeRequest): ResponseEntity<RecipeResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(recipeService.createRecipe(menuId, request))

    @PutMapping("/recipes/{recipeId}")
    fun update(@PathVariable recipeId: Long, @RequestBody request: RecipeRequest): ResponseEntity<RecipeResponse> =
        ResponseEntity.ok(recipeService.updateRecipe(recipeId, request))

    @DeleteMapping("/recipes/{recipeId}")
    fun delete(@PathVariable recipeId: Long): ResponseEntity<Void> {
        recipeService.deleteRecipe(recipeId)
        return ResponseEntity.noContent().build()
    }
}
