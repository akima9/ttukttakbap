package com.ttukttakbap.backend.auth.jwt

import com.ttukttakbap.backend.common.exception.UnauthorizedException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date

// 액세스/리프레시 JWT를 발급·검증한다. 시크릿은 SecurityConfig에서 주입.
// 클레임: subject=userId, role(액세스만), type=access|refresh
class JwtProvider(
    secret: String,
    private val accessExpiryMillis: Long,
    private val refreshExpiryMillis: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createAccessToken(userId: Long, role: String): String =
        buildToken(userId, accessExpiryMillis) { it.claim("role", role).claim("type", "access") }

    fun createRefreshToken(userId: Long): String =
        buildToken(userId, refreshExpiryMillis) { it.claim("type", "refresh") }

    fun refreshExpiryMillis(): Long = refreshExpiryMillis

    // 유효하지 않거나 만료된 토큰이면 UnauthorizedException(401).
    fun getUserId(token: String): Long = parse(token).subject.toLong()

    fun getRole(token: String): String? = parse(token)["role"] as String?

    private fun parse(token: String) =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
        } catch (e: JwtException) {
            throw UnauthorizedException("유효하지 않은 토큰입니다.")
        }

    private inline fun buildToken(
        userId: Long,
        expiryMillis: Long,
        block: (io.jsonwebtoken.JwtBuilder) -> io.jsonwebtoken.JwtBuilder,
    ): String {
        val now = Date()
        return block(
            Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(Date(now.time + expiryMillis)),
        ).signWith(key).compact()
    }
}
