package com.ttukttakbap.backend.ingredient

import com.ttukttakbap.backend.ingredient.dto.IngredientRequest
import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/ingredients")
class AdminIngredientController(private val ingredientService: IngredientService) {

    @GetMapping
    fun list(): ResponseEntity<List<IngredientResponse>> =
        ResponseEntity.ok(ingredientService.getIngredients())

    @PostMapping
    fun create(@RequestBody request: IngredientRequest): ResponseEntity<IngredientResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.createIngredient(request))

    @PutMapping("/{ingredientId}")
    fun update(@PathVariable ingredientId: Long, @RequestBody request: IngredientRequest): ResponseEntity<IngredientResponse> =
        ResponseEntity.ok(ingredientService.updateIngredient(ingredientId, request))

    @DeleteMapping("/{ingredientId}")
    fun delete(@PathVariable ingredientId: Long): ResponseEntity<Void> {
        ingredientService.deleteIngredient(ingredientId)
        return ResponseEntity.noContent().build()
    }
}
