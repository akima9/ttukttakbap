package com.ttukttakbap.backend.ingredient

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class IngredientEntityTest {

    @Autowired lateinit var entityManager: TestEntityManager

    @Test
    fun `재료를 저장하고 조회할 수 있다`() {
        val ingredient = Ingredient(name = "김치", purchaseUnit = "1포기", purchaseLocation = "마트")
        val saved = entityManager.persistAndFlush(ingredient)
        val found = entityManager.find(Ingredient::class.java, saved.id)
        assertThat(found.name).isEqualTo("김치")
        assertThat(found.purchaseUnit).isEqualTo("1포기")
    }

    @Test
    fun `purchaseLocation은 null일 수 있다`() {
        val ingredient = Ingredient(name = "소금", purchaseUnit = "1kg")
        val saved = entityManager.persistAndFlush(ingredient)
        val found = entityManager.find(Ingredient::class.java, saved.id)
        assertThat(found.purchaseLocation).isNull()
    }
}
