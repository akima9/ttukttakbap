import Link from 'next/link'
import ErrorMessage from '@/components/ErrorMessage'
import MenuCard, { type Menu } from '@/components/MenuCard'
import FridgeMenuList from './FridgeMenuList'

interface PageResponse {
  content: Menu[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

const API = 'http://localhost:8080/api/v1'

function buildQuery(params: Record<string, string | undefined>) {
  const sp = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value) sp.set(key, value)
  }
  return sp.toString()
}

export default async function MenuPage({
  searchParams,
}: {
  searchParams: Promise<{ people?: string; category?: string; page?: string; useMyFridge?: string }>
}) {
  const { people = '2', category, page = '0', useMyFridge } = await searchParams

  // 냉장고 기반 추천은 JWT가 필요하므로 클라이언트에서 조회한다.
  if (useMyFridge === '1') return <FridgeMenuList people={people} />

  let categories: string[] = []
  let data: PageResponse | null = null
  let error = ''

  try {
    const [catRes, menuRes] = await Promise.all([
      fetch(`${API}/categories`, { cache: 'no-store' }),
      fetch(`${API}/menus/recommend?${buildQuery({ people, category, page })}`, { cache: 'no-store' }),
    ])
    if (!catRes.ok || !menuRes.ok) throw new Error('메뉴를 불러오지 못했습니다.')
    categories = await catRes.json()
    data = await menuRes.json()
  } catch (e) {
    error = e instanceof Error ? e.message : '메뉴를 불러오지 못했습니다.'
  }

  if (error || !data) return <ErrorMessage message={error || '메뉴를 불러오지 못했습니다.'} />

  const menus = data.content
  const currentPage = data.page

  return (
    <div>
      <div className="flex items-baseline gap-2 mb-4">
        <h1 className="text-xl font-bold text-gray-800">어떤 메뉴가 좋으세요?</h1>
        <span className="text-sm text-gray-400">{data.totalElements}개</span>
      </div>

      <div className="flex gap-2 overflow-x-auto pb-3 mb-1 -mx-4 px-4">
        <CategoryChip label="전체" people={people} category={undefined} active={!category} />
        {categories.map(c => (
          <CategoryChip key={c} label={c} people={people} category={c} active={category === c} />
        ))}
      </div>

      {menus.length === 0 ? (
        <p className="py-16 text-center text-sm text-gray-400">조건에 맞는 메뉴가 없어요.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {menus.map(menu => (
            <MenuCard key={menu.id} menu={menu} people={people} />
          ))}
        </div>
      )}

      {data.totalPages > 1 && (
        <div className="flex justify-between items-center mt-6">
          <PageLink people={people} category={category} page={currentPage - 1} disabled={currentPage <= 0}>
            이전
          </PageLink>
          <span className="text-sm text-gray-400">
            {currentPage + 1} / {data.totalPages}
          </span>
          <PageLink
            people={people}
            category={category}
            page={currentPage + 1}
            disabled={currentPage >= data.totalPages - 1}
          >
            다음
          </PageLink>
        </div>
      )}
    </div>
  )
}

function CategoryChip({
  label,
  people,
  category,
  active,
}: {
  label: string
  people: string
  category: string | undefined
  active: boolean
}) {
  return (
    <Link
      replace
      href={`/menu?${buildQuery({ people, category })}`}
      className={`shrink-0 text-sm px-3 py-1.5 rounded-full border transition-colors ${
        active
          ? 'bg-rose-500 text-white border-rose-500'
          : 'bg-white text-gray-500 border-gray-200 hover:border-rose-300'
      }`}
    >
      {label}
    </Link>
  )
}

function PageLink({
  people,
  category,
  page,
  disabled,
  children,
}: {
  people: string
  category: string | undefined
  page: number
  disabled: boolean
  children: React.ReactNode
}) {
  if (disabled) {
    return <span className="text-sm text-gray-300 px-3 py-1.5">{children}</span>
  }
  return (
    <Link
      replace
      href={`/menu?${buildQuery({ people, category, page: String(page) })}`}
      className="text-sm text-rose-500 px-3 py-1.5 rounded-lg hover:bg-rose-50 transition-colors"
    >
      {children}
    </Link>
  )
}
