'use client'

import { useCallback, useEffect, useState } from 'react'
import { adminFetch } from '@/lib/admin'
import ErrorMessage from '@/components/ErrorMessage'

interface Recipe {
  id: number
  stepOrder: number
  description: string
  tip: string | null
}

const inputClass = 'w-full rounded-lg border border-gray-200 px-3 py-2 text-sm'

export default function RecipeManager({ menuId }: { menuId: string }) {
  const [recipes, setRecipes] = useState<Recipe[]>([])
  const [stepOrder, setStepOrder] = useState(1)
  const [description, setDescription] = useState('')
  const [tip, setTip] = useState('')
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      const data = await adminFetch<Recipe[]>(`/admin/menus/${menuId}/recipes`)
      setRecipes(data)
      setStepOrder(data.length ? Math.max(...data.map((r) => r.stepOrder)) + 1 : 1)
    } catch (e) {
      setError(e instanceof Error ? e.message : '불러오기 실패')
    }
  }, [menuId])

  useEffect(() => {
    // 마운트 시 외부 API에서 1회 로드 (effect escape hatch)
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load()
  }, [load])

  const add = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await adminFetch(`/admin/menus/${menuId}/recipes`, {
        method: 'POST',
        body: JSON.stringify({ stepOrder, description, tip: tip || null }),
      })
      setDescription('')
      setTip('')
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '추가 실패')
    }
  }

  const remove = async (id: number) => {
    try {
      await adminFetch(`/admin/recipes/${id}`, { method: 'DELETE' })
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제 실패')
    }
  }

  return (
    <section className="mt-8">
      <h2 className="font-bold text-gray-800 mb-3">레시피 단계</h2>
      {error && <div className="mb-2"><ErrorMessage message={error} /></div>}

      <ol className="flex flex-col gap-2 mb-3">
        {recipes.map((r) => (
          <li key={r.id} className="bg-white rounded-lg p-3 border border-gray-100 flex justify-between items-start gap-2">
            <div className="text-sm">
              <span className="font-semibold text-orange-500 mr-2">{r.stepOrder}</span>
              <span className="text-gray-800">{r.description}</span>
              {r.tip && <p className="text-xs text-gray-400 mt-1">팁: {r.tip}</p>}
            </div>
            <button onClick={() => remove(r.id)} className="text-xs text-red-400 hover:underline shrink-0">
              삭제
            </button>
          </li>
        ))}
        {recipes.length === 0 && <p className="text-sm text-gray-400">아직 단계가 없습니다.</p>}
      </ol>

      <form onSubmit={add} className="flex flex-col gap-2 bg-gray-50 rounded-xl p-3">
        <div className="flex gap-2">
          <input
            type="number"
            min={1}
            value={stepOrder}
            onChange={(e) => setStepOrder(Number(e.target.value))}
            className={`${inputClass} w-20`}
            aria-label="순서"
          />
          <input className={inputClass} placeholder="조리 설명" value={description} onChange={(e) => setDescription(e.target.value)} required />
        </div>
        <input className={inputClass} placeholder="팁 (선택)" value={tip} onChange={(e) => setTip(e.target.value)} />
        <button className="self-end text-sm font-semibold text-white bg-orange-500 hover:bg-orange-600 rounded-lg px-4 py-2">
          단계 추가
        </button>
      </form>
    </section>
  )
}
