package com.ttukttakbap.backend.auth.token

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

// Redis 미사용 시 기본 저장소(refresh-token.store=memory, 미설정 시도 기본).
// TTL은 만료 시각으로 단순 관리한다(조회 시 만료 검사).
@Component
@ConditionalOnProperty(name = ["refresh-token.store"], havingValue = "memory", matchIfMissing = true)
class InMemoryRefreshTokenStore : RefreshTokenStore {
    private data class Entry(val token: String, val expiresAt: Long)

    private val store = ConcurrentHashMap<Long, Entry>()

    override fun save(userId: Long, token: String, ttlMillis: Long) {
        store[userId] = Entry(token, System.currentTimeMillis() + ttlMillis)
    }

    override fun find(userId: Long): String? {
        val entry = store[userId] ?: return null
        if (entry.expiresAt < System.currentTimeMillis()) {
            store.remove(userId)
            return null
        }
        return entry.token
    }

    override fun delete(userId: Long) {
        store.remove(userId)
    }
}
