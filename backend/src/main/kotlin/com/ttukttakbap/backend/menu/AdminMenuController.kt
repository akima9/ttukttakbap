package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.menu.dto.MenuRequest
import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/menus")
class AdminMenuController(private val menuService: MenuService) {

    @PostMapping
    fun create(@RequestBody request: MenuRequest): ResponseEntity<MenuResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(menuService.createMenu(request))

    @PutMapping("/{menuId}")
    fun update(@PathVariable menuId: Long, @RequestBody request: MenuRequest): ResponseEntity<MenuResponse> =
        ResponseEntity.ok(menuService.updateMenu(menuId, request))

    @DeleteMapping("/{menuId}")
    fun delete(@PathVariable menuId: Long): ResponseEntity<Void> {
        menuService.deleteMenu(menuId)
        return ResponseEntity.noContent().build()
    }
}
