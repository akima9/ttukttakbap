package com.ttukttakbap.backend.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findBySocialProviderAndSocialId(socialProvider: SocialProvider, socialId: String): User?
}
