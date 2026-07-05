import Link from 'next/link'
import ErrorMessage from '@/components/ErrorMessage'

interface RecipeStep {
  stepOrder: number
  description: string
  tip: string | null
}

export default async function RecipePage({
  searchParams,
}: {
  searchParams: Promise<{ menuId?: string }>
}) {
  const { menuId } = await searchParams

  if (!menuId) return <ErrorMessage message="메뉴를 선택해주세요." />

  let steps: RecipeStep[] = []
  let error = ''

  try {
    const res = await fetch(`http://localhost:8080/api/v1/menus/${menuId}/recipe`, { cache: 'no-store' })
    if (!res.ok) throw new Error('레시피를 불러오지 못했습니다.')
    steps = await res.json()
  } catch (e) {
    error = e instanceof Error ? e.message : '레시피를 불러오지 못했습니다.'
  }

  if (error) return <ErrorMessage message={error} />

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-xl font-bold text-gray-800 mb-6">레시피</h1>
      <ol className="flex flex-col gap-4">
        {steps.map(step => (
          <li key={step.stepOrder} className="flex gap-3">
            <span className="shrink-0 w-8 h-8 rounded-full bg-rose-500 text-white flex items-center justify-center font-bold text-sm">
              {step.stepOrder}
            </span>
            <div className="flex-1 bg-white rounded-xl p-4 shadow-sm border border-gray-100">
              <p className="text-sm text-gray-800">{step.description}</p>
              {step.tip && (
                <p className="mt-2 text-xs text-rose-500 bg-rose-50 rounded-lg px-3 py-2">
                  {step.tip}
                </p>
              )}
            </div>
          </li>
        ))}
      </ol>
      <Link
        href="/"
        className="mt-8 block w-full py-3 rounded-xl text-center font-semibold text-rose-500 bg-rose-50 hover:bg-rose-100 transition-colors"
      >
        처음으로
      </Link>
    </div>
  )
}
