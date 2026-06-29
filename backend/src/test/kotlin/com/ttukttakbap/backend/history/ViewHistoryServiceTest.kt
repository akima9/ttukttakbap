package com.ttukttakbap.backend.history

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.favorite.FavoriteRepository
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ViewHistoryServiceTest {

    @Mock lateinit var viewHistoryRepository: ViewHistoryRepository
    @Mock lateinit var menuRepository: MenuRepository
    @Mock lateinit var favoriteRepository: FavoriteRepository

    @InjectMocks lateinit var service: ViewHistoryService

    private fun menu(id: Long, name: String) = Menu(
        id = id, name = name, description = "", imageUrl = "",
        cookTimeMinutes = 30, difficulty = Difficulty.EASY, category = Category.JJIGAE,
    )

    @Test
    fun `처음 본 메뉴는 새 기록으로 저장한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(viewHistoryRepository.findByUserIdAndMenuId(1L, 1L)).thenReturn(null)

        service.recordView(1L, 1L)

        val captor = argumentCaptor<ViewHistory>()
        verify(viewHistoryRepository).save(captor.capture())
        assertThat(captor.firstValue.id).isEqualTo(0L)
        assertThat(captor.firstValue.menuId).isEqualTo(1L)
    }

    @Test
    fun `다시 본 메뉴는 같은 id로 재저장해 중복 없이 시각만 갱신한다`() {
        whenever(menuRepository.existsById(1L)).thenReturn(true)
        whenever(viewHistoryRepository.findByUserIdAndMenuId(1L, 1L))
            .thenReturn(ViewHistory(id = 5, userId = 1L, menuId = 1L))

        service.recordView(1L, 1L)

        val captor = argumentCaptor<ViewHistory>()
        verify(viewHistoryRepository).save(captor.capture())
        assertThat(captor.firstValue.id).isEqualTo(5L)
    }

    @Test
    fun `존재하지 않는 메뉴를 기록하면 NotFoundException을 던진다`() {
        whenever(menuRepository.existsById(99L)).thenReturn(false)

        assertThatThrownBy { service.recordView(1L, 99L) }
            .isInstanceOf(NotFoundException::class.java)
        verify(viewHistoryRepository, never()).save(any<ViewHistory>())
    }

    @Test
    fun `최근 본 메뉴를 최신순으로 반환하며 즐겨찾기 여부를 반영한다`() {
        whenever(viewHistoryRepository.findTop20ByUserIdOrderByViewedAtDesc(1L)).thenReturn(
            listOf(
                ViewHistory(id = 1, userId = 1L, menuId = 2L),
                ViewHistory(id = 2, userId = 1L, menuId = 1L),
            ),
        )
        whenever(menuRepository.findAllById(listOf(2L, 1L)))
            .thenReturn(listOf(menu(1L, "김치찌개"), menu(2L, "된장찌개")))
        whenever(favoriteRepository.findMenuIdsByUserId(1L)).thenReturn(listOf(1L))

        val result = service.getHistory(1L)

        assertThat(result.map { it.id }).containsExactly(2L, 1L)
        assertThat(result[0].isFavorite).isFalse()
        assertThat(result[1].isFavorite).isTrue()
    }
}
