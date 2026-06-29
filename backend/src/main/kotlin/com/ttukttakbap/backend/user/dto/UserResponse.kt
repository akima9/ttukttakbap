package com.ttukttakbap.backend.user.dto

import com.ttukttakbap.backend.user.User

data class UserResponse(
    val id: Long,
    val nickname: String,
    val email: String?,
    val profileImageUrl: String?,
    val role: String,
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id,
            nickname = user.nickname,
            email = user.email,
            profileImageUrl = user.profileImageUrl,
            role = user.role.name,
        )
    }
}
