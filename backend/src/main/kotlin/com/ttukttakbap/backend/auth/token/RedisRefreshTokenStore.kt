package com.ttukttakbap.backend.auth.token

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

// 운영 저장소(refresh-token.store=redis). 키 refresh:{userId}, Redis TTL이 만료를 관리.
@Component
@ConditionalOnProperty(name = ["refresh-token.store"], havingValue = "redis")
class RedisRefreshTokenStore(private val redisTemplate: StringRedisTemplate) : RefreshTokenStore {

    override fun save(userId: Long, token: String, ttlMillis: Long) {
        redisTemplate.opsForValue().set(key(userId), token, Duration.ofMillis(ttlMillis))
    }

    override fun find(userId: Long): String? = redisTemplate.opsForValue().get(key(userId))

    override fun delete(userId: Long) {
        redisTemplate.delete(key(userId))
    }

    private fun key(userId: Long) = "refresh:$userId"
}
