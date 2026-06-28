package com.ttukttakbap.backend.menu

import com.ttukttakbap.backend.common.exception.NotFoundException
import com.ttukttakbap.backend.ingredient.IngredientRepository
import com.ttukttakbap.backend.menu.dto.MenuIngredientLinkResponse
import com.ttukttakbap.backend.menu.dto.MenuIngredientRequest
import org.springframework.stereotype.Service

@Service
class MenuIngredientService(
    private val menuIngredientRepository: MenuIngredientRepository,
    private val menuRepository: MenuRepository,
    private val ingredientRepository: IngredientRepository,
) {
    fun getLinks(menuId: Long): List<MenuIngredientLinkResponse> {
        if (!menuRepository.existsById(menuId)) throw NotFoundException("해당 메뉴를 찾을 수 없습니다.")
        return menuIngredientRepository.findByMenuId(menuId).map { MenuIngredientLinkResponse.from(it) }
    }

    fun linkIngredient(menuId: Long, request: MenuIngredientRequest): MenuIngredientLinkResponse {
        val menu = menuRepository.findById(menuId)
            .orElseThrow { NotFoundException("해당 메뉴를 찾을 수 없습니다.") }
        val ingredient = ingredientRepository.findById(request.ingredientId)
            .orElseThrow { NotFoundException("해당 재료를 찾을 수 없습니다.") }
        val saved = menuIngredientRepository.save(
            MenuIngredient(
                menu = menu,
                ingredient = ingredient,
                amountPerPerson = request.amountPerPerson,
                unit = request.unit,
            ),
        )
        return MenuIngredientLinkResponse.from(saved)
    }

    fun unlinkIngredient(menuIngredientId: Long) {
        if (!menuIngredientRepository.existsById(menuIngredientId)) {
            throw NotFoundException("해당 메뉴-재료 연결을 찾을 수 없습니다.")
        }
        menuIngredientRepository.deleteById(menuIngredientId)
    }
}
