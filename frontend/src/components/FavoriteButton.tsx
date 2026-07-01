'use client'

import { useFavorites } from '@/lib/favorites'

// 메뉴 카드 위에 올리는 즐겨찾기 하트. 비로그인이면 렌더하지 않는다.
export default function FavoriteButton({ menuId }: { menuId: number }) {
  const { loggedIn, isFavorite, toggle } = useFavorites()
  if (!loggedIn) return null

  const favorite = isFavorite(menuId)

  const onClick = (e: React.MouseEvent) => {
    // 카드 Link 클릭으로 번지지 않도록 막는다.
    e.preventDefault()
    e.stopPropagation()
    toggle(menuId).catch(() => {})
  }

  return (
    <button
      onClick={onClick}
      aria-label={favorite ? '즐겨찾기 해제' : '즐겨찾기 추가'}
      className="absolute top-2.5 right-2.5 z-10 p-1.5 rounded-full hover:bg-orange-50 transition-colors"
    >
      <svg
        className={`w-5 h-5 ${favorite ? 'text-orange-500' : 'text-gray-300'}`}
        fill={favorite ? 'currentColor' : 'none'}
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
        />
      </svg>
    </button>
  )
}
