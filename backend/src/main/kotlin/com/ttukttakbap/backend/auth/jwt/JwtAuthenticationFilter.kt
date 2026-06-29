package com.ttukttakbap.backend.auth.jwt

import com.ttukttakbap.backend.common.exception.UnauthorizedException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

// Authorization: Bearer <accessToken> 가 있으면 검증해 SecurityContext에 인증을 채운다.
// principal = userId(Long), 권한 = ROLE_{role}. 토큰이 없거나 유효하지 않으면 인증 없이 통과(인가 단계에서 401 처리).
class JwtAuthenticationFilter(private val jwtProvider: JwtProvider) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            try {
                val userId = jwtProvider.getUserId(token)
                val role = jwtProvider.getRole(token)
                if (role != null) {
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_$role")),
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: UnauthorizedException) {
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}
