package com.ttukttakbap.backend.history

import com.ttukttakbap.backend.menu.dto.MenuResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/me/history")
class ViewHistoryController(private val viewHistoryService: ViewHistoryService) {

    @GetMapping
    fun list(@AuthenticationPrincipal userId: Long): List<MenuResponse> =
        viewHistoryService.getHistory(userId)

    @PostMapping("/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun record(@AuthenticationPrincipal userId: Long, @PathVariable menuId: Long) =
        viewHistoryService.recordView(userId, menuId)
}
