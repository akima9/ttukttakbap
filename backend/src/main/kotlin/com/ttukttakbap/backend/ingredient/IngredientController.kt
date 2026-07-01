package com.ttukttakbap.backend.ingredient

import com.ttukttakbap.backend.ingredient.dto.IngredientResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 냉장고 담기 등에서 재료를 고를 수 있도록 하는 공개 재료 목록 조회.
@RestController
@RequestMapping("/api/v1/ingredients")
class IngredientController(private val ingredientService: IngredientService) {

    @GetMapping
    fun list(): List<IngredientResponse> = ingredientService.getIngredients()
}
