package com.ttukttakbap.backend.fridge

import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/me/fridge")
class FridgeController(private val fridgeService: FridgeService) {

    @GetMapping
    fun list(@AuthenticationPrincipal userId: Long): List<IngredientResponse> =
        fridgeService.getFridge(userId)

    @PostMapping("/{ingredientId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@AuthenticationPrincipal userId: Long, @PathVariable ingredientId: Long) =
        fridgeService.addItem(userId, ingredientId)

    @DeleteMapping("/{ingredientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@AuthenticationPrincipal userId: Long, @PathVariable ingredientId: Long) =
        fridgeService.removeItem(userId, ingredientId)
}
