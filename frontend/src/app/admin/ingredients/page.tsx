'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { adminFetch } from '@/lib/admin'
import { useAdminGuard } from '@/lib/useAdminGuard'
import ErrorMessage from '@/components/ErrorMessage'

interface Ingredient {
  id: number
  name: string
  purchaseUnit: string
  purchaseLocation: string | null
  coupangUrl: string | null
}

type FormState = {
  name: string
  purchaseUnit: string
  purchaseLocation: string
  coupangUrl: string
}

const EMPTY: FormState = { name: '', purchaseUnit: '', purchaseLocation: '', coupangUrl: '' }
const inputClass = 'w-full rounded-lg border border-gray-200 px-3 py-2 text-sm'

export default function AdminIngredientsPage() {
  const ready = useAdminGuard()
  const [ingredients, setIngredients] = useState<Ingredient[]>([])
  const [form, setForm] = useState<FormState>(EMPTY)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      setIngredients(await adminFetch<Ingredient[]>('/admin/ingredients'))
    } catch (e) {
      setError(e instanceof Error ? e.message : '불러오기 실패')
    }
  }, [])

  useEffect(() => {
    // 인증 준비 후 외부 API에서 1회 로드 (effect escape hatch)
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (ready) load()
  }, [ready, load])

  const set = (patch: Partial<FormState>) => setForm((f) => ({ ...f, ...patch }))

  const resetForm = () => {
    setForm(EMPTY)
    setEditingId(null)
  }

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    const body = JSON.stringify({
      name: form.name,
      purchaseUnit: form.purchaseUnit,
      purchaseLocation: form.purchaseLocation || null,
      coupangUrl: form.coupangUrl || null,
    })
    try {
      if (editingId === null) {
        await adminFetch('/admin/ingredients', { method: 'POST', body })
      } else {
        await adminFetch(`/admin/ingredients/${editingId}`, { method: 'PUT', body })
      }
      resetForm()
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장 실패')
    }
  }

  const edit = (i: Ingredient) => {
    setEditingId(i.id)
    setForm({
      name: i.name,
      purchaseUnit: i.purchaseUnit,
      purchaseLocation: i.purchaseLocation ?? '',
      coupangUrl: i.coupangUrl ?? '',
    })
  }

  const remove = async (id: number, name: string) => {
    if (!confirm(`'${name}' 재료를 삭제할까요?`)) return
    try {
      await adminFetch(`/admin/ingredients/${id}`, { method: 'DELETE' })
      if (editingId === id) resetForm()
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제 실패')
    }
  }

  if (!ready) return null

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold text-gray-800">재료 관리</h1>
        <Link href="/admin" className="text-sm text-gray-400 hover:text-gray-600">
          메뉴 관리
        </Link>
      </div>

      {error && <div className="mb-3"><ErrorMessage message={error} /></div>}

      <form onSubmit={submit} className="flex flex-col gap-2 bg-gray-50 rounded-xl p-3 mb-5">
        <p className="text-sm font-semibold text-gray-600">{editingId === null ? '새 재료 추가' : '재료 수정'}</p>
        <input className={inputClass} placeholder="재료 이름" value={form.name} onChange={(e) => set({ name: e.target.value })} required />
        <input className={inputClass} placeholder="구매 단위 (예: 1포기, 500g)" value={form.purchaseUnit} onChange={(e) => set({ purchaseUnit: e.target.value })} required />
        <input className={inputClass} placeholder="장보기 추천 (선택)" value={form.purchaseLocation} onChange={(e) => set({ purchaseLocation: e.target.value })} />
        <input className={inputClass} placeholder="쿠팡 URL (선택)" value={form.coupangUrl} onChange={(e) => set({ coupangUrl: e.target.value })} />
        <div className="flex gap-2 self-end">
          {editingId !== null && (
            <button type="button" onClick={resetForm} className="text-sm text-gray-500 px-4 py-2">
              취소
            </button>
          )}
          <button className="text-sm font-semibold text-white bg-orange-500 hover:bg-orange-600 rounded-lg px-4 py-2">
            {editingId === null ? '추가' : '저장'}
          </button>
        </div>
      </form>

      <div className="flex flex-col gap-2">
        {ingredients.map((i) => (
          <div key={i.id} className="bg-white rounded-xl p-3 border border-gray-100 flex justify-between items-center">
            <div>
              <p className="text-sm font-semibold text-gray-800">{i.name}</p>
              <p className="text-xs text-gray-400 mt-0.5">
                {i.purchaseUnit}
                {i.purchaseLocation ? ` · ${i.purchaseLocation}` : ''}
              </p>
            </div>
            <div className="flex gap-2 text-sm">
              <button onClick={() => edit(i)} className="text-orange-500 hover:underline">
                수정
              </button>
              <button onClick={() => remove(i.id, i.name)} className="text-red-400 hover:underline">
                삭제
              </button>
            </div>
          </div>
        ))}
        {ingredients.length === 0 && <p className="py-8 text-center text-sm text-gray-400">등록된 재료가 없습니다.</p>}
      </div>
    </div>
  )
}
