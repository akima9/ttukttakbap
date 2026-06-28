package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.menu.dto.MenuIngredientLinkResponse
import com.ttukttakbap.backend.menu.dto.MenuIngredientRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminMenuIngredientController(private val menuIngredientService: MenuIngredientService) {

    @GetMapping("/menus/{menuId}/ingredients")
    fun list(@PathVariable menuId: Long): ResponseEntity<List<MenuIngredientLinkResponse>> =
        ResponseEntity.ok(menuIngredientService.getLinks(menuId))

    @PostMapping("/menus/{menuId}/ingredients")
    fun link(@PathVariable menuId: Long, @RequestBody request: MenuIngredientRequest): ResponseEntity<MenuIngredientLinkResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(menuIngredientService.linkIngredient(menuId, request))

    @DeleteMapping("/menu-ingredients/{menuIngredientId}")
    fun unlink(@PathVariable menuIngredientId: Long): ResponseEntity<Void> {
        menuIngredientService.unlinkIngredient(menuIngredientId)
        return ResponseEntity.noContent().build()
    }
}
