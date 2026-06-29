package com.ttukttakbap.backend.favorite

import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/me/favorites")
class FavoriteController(private val favoriteService: FavoriteService) {

    @GetMapping
    fun list(@AuthenticationPrincipal userId: Long): List<MenuResponse> =
        favoriteService.getFavorites(userId)

    @PostMapping("/{menuId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@AuthenticationPrincipal userId: Long, @PathVariable menuId: Long) =
        favoriteService.addFavorite(userId, menuId)

    @DeleteMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@AuthenticationPrincipal userId: Long, @PathVariable menuId: Long) =
        favoriteService.removeFavorite(userId, menuId)
}
