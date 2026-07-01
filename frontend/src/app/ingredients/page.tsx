import ErrorMessage from '@/components/ErrorMessage'
import IngredientsChecklist from './IngredientsChecklist'
import RecordHistory from '@/components/RecordHistory'

interface Ingredient {
  ingredientId: number
  name: string
  requiredAmount: number
  unit: string
  purchaseUnit: string
  purchaseLocation: string | null
  coupangUrl: string | null
}

export default async function IngredientsPage({
  searchParams,
}: {
  searchParams: Promise<{ people?: string; menuId?: string }>
}) {
  const { people = '2', menuId } = await searchParams

  if (!menuId) return <ErrorMessage message="메뉴를 선택해주세요." />

  let ingredients: Ingredient[] = []
  let error = ''

  try {
    const res = await fetch(
      `http://localhost:8080/api/v1/menus/${menuId}/ingredients?people=${people}`,
      { cache: 'no-store' }
    )
    if (!res.ok) throw new Error('재료를 불러오지 못했습니다.')
    ingredients = await res.json()
  } catch (e) {
    error = e instanceof Error ? e.message : '재료를 불러오지 못했습니다.'
  }

  if (error) return <ErrorMessage message={error} />

  return (
    <div>
      <RecordHistory menuId={menuId} />
      <h1 className="text-xl font-bold text-gray-800">필요한 재료</h1>
      <p className="mt-1 mb-4 text-sm text-gray-400">{people}인분 기준</p>
      <IngredientsChecklist ingredients={ingredients} people={people} menuId={menuId} />
    </div>
  )
}
