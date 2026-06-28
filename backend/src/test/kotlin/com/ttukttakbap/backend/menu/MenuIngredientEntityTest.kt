package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.ingredient.Ingredient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.math.BigDecimal

@DataJpaTest
class MenuIngredientEntityTest {

    @Autowired lateinit var entityManager: TestEntityManager

    @Test
    fun `메뉴와 재료를 연결하고 인분당 양을 저장할 수 있다`() {
        val menu = entityManager.persist(Menu(name = "김치찌개", description = "설명", imageUrl = "", cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE))
        val ingredient = entityManager.persist(Ingredient(name = "김치", purchaseUnit = "1포기"))
        val menuIngredient = MenuIngredient(menu = menu, ingredient = ingredient, amountPerPerson = BigDecimal("150.00"), unit = "g")
        val saved = entityManager.persistAndFlush(menuIngredient)
        val found = entityManager.find(MenuIngredient::class.java, saved.id)
        assertThat(found.amountPerPerson).isEqualByComparingTo(BigDecimal("150.00"))
        assertThat(found.unit).isEqualTo("g")
    }
}
