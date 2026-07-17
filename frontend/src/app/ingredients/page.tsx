import ErrorMessage from '@/components/ErrorMessage'
import IngredientsChecklist from './IngredientsChecklist'
import PeopleStepper from './PeopleStepper'
import RecordHistory from '@/components/RecordHistory'
import MenuThumbnail from '@/components/MenuThumbnail'

interface Ingredient {
  ingredientId: number
  name: string
  requiredAmount: number
  unit: string
  purchaseUnit: string
  purchaseLocation: string | null
  coupangUrl: string | null
}

interface MenuDetail {
  id: number
  name: string
  description: string
  imageUrl: string
}

const API = process.env.API_INTERNAL_URL || 'http://localhost:8080/api/v1'

export default async function IngredientsPage({
  searchParams,
}: {
  searchParams: Promise<{ people?: string; menuId?: string }>
}) {
  const { people = '2', menuId } = await searchParams

  if (!menuId) return <ErrorMessage message="메뉴를 선택해주세요." />

  let menu: MenuDetail | null = null
  let ingredients: Ingredient[] = []
  let error = ''

  try {
    const [menuRes, ingRes] = await Promise.all([
      fetch(`${API}/menus/${menuId}`, { cache: 'no-store' }),
      fetch(`${API}/menus/${menuId}/ingredients?people=${people}`, {
        cache: 'no-store',
      }),
    ])
    if (!menuRes.ok || !ingRes.ok) throw new Error('메뉴 정보를 불러오지 못했습니다.')
    menu = await menuRes.json()
    ingredients = await ingRes.json()
  } catch (e) {
    error = e instanceof Error ? e.message : '메뉴 정보를 불러오지 못했습니다.'
  }

  if (error || !menu) return <ErrorMessage message={error || '메뉴 정보를 불러오지 못했습니다.'} />

  return (
    <div className="max-w-2xl mx-auto">
      <RecordHistory menuId={menuId} />
      <MenuThumbnail src={menu.imageUrl} alt={menu.name} className="w-full aspect-[16/9] rounded-xl" />
      <h1 className="mt-3 text-2xl font-bold text-gray-800">{menu.name}</h1>
      {menu.description && <p className="mt-1 text-sm text-gray-500">{menu.description}</p>}
      <div className="mt-6 mb-4 flex items-center justify-between gap-3">
        <h2 className="text-xl font-bold text-gray-800">필요한 재료</h2>
        <PeopleStepper people={Number(people)} menuId={menuId} />
      </div>
      <IngredientsChecklist ingredients={ingredients} people={people} menuId={menuId} />
    </div>
  )
}
