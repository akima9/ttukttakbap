package com.ttukttakbap.backend.auth

import com.ttukttakbap.backend.auth.dto.LoginRequest
import com.ttukttakbap.backend.auth.dto.LogoutRequest
import com.ttukttakbap.backend.auth.dto.RefreshRequest
import com.ttukttakbap.backend.auth.dto.RefreshResponse
import com.ttukttakbap.backend.auth.dto.TokenResponse
import com.ttukttakbap.backend.user.dto.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login/{provider}")
    fun login(@PathVariable provider: String, @RequestBody request: LoginRequest): TokenResponse =
        authService.login(provider, request.code, request.redirectUri)

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): RefreshResponse =
        authService.refresh(request.refreshToken)

    @PostMapping("/logout")
    fun logout(@RequestBody request: LogoutRequest): ResponseEntity<Void> {
        authService.logout(request.refreshToken)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userId: Long): UserResponse =
        authService.me(userId)
}
