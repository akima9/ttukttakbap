package com.ttukttakbap.backend.favorite

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.menu.Category
import com.ttukttakbap.backend.menu.Difficulty
import com.ttukttakbap.backend.menu.Menu
import com.ttukttakbap.backend.menu.MenuRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class FavoriteServiceTest {

    @Mock lateinit var favoriteRepository: FavoriteRepository
    @Mock lateinit var menuRepository: MenuRepository

    @InjectMocks lateinit var service: FavoriteService

    private val menu = Menu(
        id = 1, name = "김치찌개", description = "얼큰한 찌개", imageUrl = "",
        cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE,
    )

    @Test
    fun `즐겨찾기를 추가한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(favoriteRepository.existsByUserIdAndMenuId(1L, 1L)).thenReturn(false)

        service.addFavorite(1L, 1L)

        verify(favoriteRepository).save(any<Favorite>())
    }

    @Test
    fun `이미 즐겨찾기한 메뉴는 다시 저장하지 않는다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(favoriteRepository.existsByUserIdAndMenuId(1L, 1L)).thenReturn(true)

        service.addFavorite(1L, 1L)

        verify(favoriteRepository, never()).save(any<Favorite>())
    }

    @Test
    fun `존재하지 않는 메뉴를 즐겨찾기하면 NotFoundException을 던진다`() {
        whenever(menuRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { service.addFavorite(1L, 99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(favoriteRepository, never()).save(any<Favorite>())
    }

    @Test
    fun `즐겨찾기를 삭제한다`() {
        service.removeFavorite(1L, 1L)

        verify(favoriteRepository).deleteByUserIdAndMenuId(1L, 1L)
    }

    @Test
    fun `즐겨찾기 목록을 최신순 메뉴로 반환하며 isFavorite는 true다`() {
        whenever(favoriteRepository.findByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(listOf(Favorite(id = 1, userId = 1L, menuId = 1L)))
        whenever(menuRepository.findAllById(listOf(1L))).thenReturn(listOf(menu))

        val result = service.getFavorites(1L)

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("김치찌개")
        assertThat(result[0].isFavorite).isTrue()
    }
}
