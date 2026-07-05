import Link from 'next/link'
import FavoriteButton from '@/components/FavoriteButton'

export interface Menu {
  id: number
  name: string
  description: string
  cookTimeMinutes: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  category: string
}

const DIFFICULTY_LABEL: Record<string, string> = {
  EASY: '쉬움',
  MEDIUM: '보통',
  HARD: '어려움',
}

export default function MenuCard({ menu, people }: { menu: Menu; people: string }) {
  return (
    <div className="relative">
      <Link
        href={`/ingredients?people=${people}&menuId=${menu.id}`}
        className="block bg-white rounded-xl p-4 shadow-sm border border-gray-100 hover:border-rose-300 transition-colors"
      >
        <h2 className="text-base font-semibold text-gray-800 pr-8">{menu.name}</h2>
        <p className="mt-1 text-sm text-gray-500 line-clamp-2">{menu.description}</p>
        <div className="mt-2 flex gap-1.5 items-center">
          <span className="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
            <svg className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <circle cx="12" cy="12" r="10" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6l4 2" />
            </svg>
            {menu.cookTimeMinutes}분
          </span>
          <span className="inline-block text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
            {menu.category}
          </span>
          <span className="inline-block text-xs px-2 py-0.5 rounded-full bg-rose-50 text-rose-500">
            {DIFFICULTY_LABEL[menu.difficulty]}
          </span>
        </div>
      </Link>
      <FavoriteButton menuId={menu.id} />
    </div>
  )
}
