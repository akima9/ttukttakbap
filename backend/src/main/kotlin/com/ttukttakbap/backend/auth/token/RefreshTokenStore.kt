package com.ttukttakbap.backend.auth.token

// 리프레시 토큰 저장소. 키는 userId, 값은 리프레시 토큰. TTL 만료 시 자동 삭제.
// 설계상 운영은 Redis(refresh:{userId}). 로컬/테스트는 인메모리 fallback.
interface RefreshTokenStore {
    fun save(userId: Long, token: String, ttlMillis: Long)
    fun find(userId: Long): String?
    fun delete(userId: Long)
}
