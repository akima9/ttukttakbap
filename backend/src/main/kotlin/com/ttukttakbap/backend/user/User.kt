package com.ttukttakbap.backend.user

import jakarta.persistence.*
import java.time.LocalDateTime

enum class SocialProvider { KAKAO, GOOGLE, NAVER }

enum class Role { USER, ADMIN }

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["social_provider", "social_id"])],
)
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider")
    val socialProvider: SocialProvider,
    @Column(name = "social_id")
    val socialId: String,
    val email: String?,
    val nickname: String,
    val profileImageUrl: String?,
    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
