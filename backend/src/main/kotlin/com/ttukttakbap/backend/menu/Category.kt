package com.ttukttakbap.backend.menu

enum class Category(val label: String) {
    JJIGAE("찌개"),
    GUK("국"),
    BAP("밥"),
    MYEON("면"),
    BANCHAN("반찬"),
    ANJU("안주"),
    DESSERT("디저트"),
    MAIN("메인요리"),
    ;

    companion object {
        fun fromLabel(label: String): Category =
            entries.firstOrNull { it.label == label }
                ?: throw IllegalArgumentException("유효하지 않은 카테고리입니다: $label")
    }
}
