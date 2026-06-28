package com.ttukttakbap.backend.recipe

import com.ttukttakbap.backend.menu.Category
import com.ttukttakbap.backend.menu.Difficulty
import com.ttukttakbap.backend.menu.Menu
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class RecipeEntityTest {

    @Autowired lateinit var entityManager: TestEntityManager

    @Test
    fun `레시피 단계를 저장하고 조회할 수 있다`() {
        val menu = entityManager.persist(Menu(name = "김치찌개", description = "설명", imageUrl = "", cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE))
        val recipe = Recipe(menu = menu, stepOrder = 1, description = "김치를 볶는다", tip = "센 불에서 볶기")
        val saved = entityManager.persistAndFlush(recipe)
        val found = entityManager.find(Recipe::class.java, saved.id)
        assertThat(found.stepOrder).isEqualTo(1)
        assertThat(found.tip).isEqualTo("센 불에서 볶기")
    }

    @Test
    fun `tip은 null일 수 있다`() {
        val menu = entityManager.persist(Menu(name = "된장찌개", description = "설명", imageUrl = "", cookTimeMinutes = 25, difficulty = Difficulty.EASY, category = Category.JJIGAE))
        val recipe = Recipe(menu = menu, stepOrder = 1, description = "물을 끓인다")
        val saved = entityManager.persistAndFlush(recipe)
        val found = entityManager.find(Recipe::class.java, saved.id)
        assertThat(found.tip).isNull()
    }
}
