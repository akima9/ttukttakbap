package com.ttukttakbap.backend.menu

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class MenuEntityTest {

    @Autowired lateinit var entityManager: TestEntityManager

    @Test
    fun `메뉴를 저장하고 조회할 수 있다`() {
        val menu = Menu(name = "김치찌개", description = "얼큰한 김치찌개", imageUrl = "", cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE)
        val saved = entityManager.persistAndFlush(menu)
        val found = entityManager.find(Menu::class.java, saved.id)
        assertThat(found.name).isEqualTo("김치찌개")
        assertThat(found.difficulty).isEqualTo(Difficulty.EASY)
    }

    @Test
    fun `difficulty는 문자열로 저장된다`() {
        val menu = Menu(name = "불고기", description = "달콤한 불고기", imageUrl = "", cookTimeMinutes = 20, difficulty = Difficulty.MEDIUM, category = Category.MAIN)
        entityManager.persistAndFlush(menu)
        val result = entityManager.entityManager
            .createNativeQuery("SELECT difficulty FROM menu WHERE name = '불고기'")
            .singleResult as String
        assertThat(result).isEqualTo("MEDIUM")
    }
}
