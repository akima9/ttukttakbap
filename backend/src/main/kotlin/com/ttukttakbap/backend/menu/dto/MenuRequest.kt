package com.ttukttakbap.backend.menu.dto

// 어드민 메뉴 생성/수정 요청. difficulty/category는 문자열로 받아 서비스에서 enum으로 파싱한다.
data class MenuRequest(
    val name: String,
    val description: String,
    val imageUrl: String,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val category: String,
)
