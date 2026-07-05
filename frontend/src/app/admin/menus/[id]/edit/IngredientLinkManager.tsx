'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { adminFetch } from '@/lib/admin'
import ErrorMessage from '@/components/ErrorMessage'

interface MenuIngredientLink {
  id: number
  ingredientId: number
  ingredientName: string
  amountPerPerson: number
  unit: string
}

interface Ingredient {
  id: number
  name: string
}

const inputClass = 'rounded-lg border border-gray-200 px-3 py-2 text-sm'

export default function IngredientLinkManager({ menuId }: { menuId: string }) {
  const [links, setLinks] = useState<MenuIngredientLink[]>([])
  const [ingredients, setIngredients] = useState<Ingredient[]>([])
  const [ingredientId, setIngredientId] = useState('')
  const [amountPerPerson, setAmountPerPerson] = useState('')
  const [unit, setUnit] = useState('')
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      const [linkData, ingredientData] = await Promise.all([
        adminFetch<MenuIngredientLink[]>(`/admin/menus/${menuId}/ingredients`),
        adminFetch<Ingredient[]>('/admin/ingredients'),
      ])
      setLinks(linkData)
      setIngredients(ingredientData)
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
      await adminFetch(`/admin/menus/${menuId}/ingredients`, {
        method: 'POST',
        body: JSON.stringify({ ingredientId: Number(ingredientId), amountPerPerson: Number(amountPerPerson), unit }),
      })
      setIngredientId('')
      setAmountPerPerson('')
      setUnit('')
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '추가 실패')
    }
  }

  const remove = async (id: number) => {
    try {
      await adminFetch(`/admin/menu-ingredients/${id}`, { method: 'DELETE' })
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제 실패')
    }
  }

  return (
    <section className="mt-8">
      <h2 className="font-bold text-gray-800 mb-3">재료 연결 (1인분 기준)</h2>
      {error && <div className="mb-2"><ErrorMessage message={error} /></div>}

      <ul className="flex flex-col gap-2 mb-3">
        {links.map((l) => (
          <li key={l.id} className="bg-white rounded-lg p-3 border border-gray-100 flex justify-between items-center">
            <span className="text-sm text-gray-800">
              {l.ingredientName} · {l.amountPerPerson}
              {l.unit}
            </span>
            <button onClick={() => remove(l.id)} className="text-xs text-red-400 hover:underline">
              삭제
            </button>
          </li>
        ))}
        {links.length === 0 && <p className="text-sm text-gray-400">연결된 재료가 없습니다.</p>}
      </ul>

      {ingredients.length === 0 ? (
        <p className="text-sm text-gray-400">
          먼저{' '}
          <Link href="/admin/ingredients" className="text-rose-500 underline">
            재료 관리
          </Link>
          에서 재료를 등록해주세요.
        </p>
      ) : (
        <form onSubmit={add} className="flex flex-col gap-2 bg-gray-50 rounded-xl p-3">
          <select className={inputClass} value={ingredientId} onChange={(e) => setIngredientId(e.target.value)} required>
            <option value="" disabled>
              재료 선택
            </option>
            {ingredients.map((i) => (
              <option key={i.id} value={i.id}>
                {i.name}
              </option>
            ))}
          </select>
          <div className="flex gap-2">
            <input
              type="number"
              step="0.1"
              min={0}
              className={`${inputClass} flex-1`}
              placeholder="1인분 양"
              value={amountPerPerson}
              onChange={(e) => setAmountPerPerson(e.target.value)}
              required
            />
            <input className={`${inputClass} w-24`} placeholder="단위(g/개)" value={unit} onChange={(e) => setUnit(e.target.value)} required />
          </div>
          <button className="self-end text-sm font-semibold text-white bg-rose-500 hover:bg-rose-600 rounded-lg px-4 py-2">
            재료 연결
          </button>
        </form>
      )}
    </section>
  )
}
