'use client'

import { useEffect, useState } from 'react'
import { publicFetch, DIFFICULTY_OPTIONS } from '@/lib/admin'
import Button from '@/components/Button'

export interface MenuFormValues {
  name: string
  description: string
  imageUrl: string
  cookTimeMinutes: number
  difficulty: string
  category: string
}

const inputClass = 'w-full rounded-xl border border-gray-200 px-4 py-3 text-sm'

export default function MenuForm({
  initial,
  submitLabel,
  onSubmit,
}: {
  initial: MenuFormValues
  submitLabel: string
  onSubmit: (values: MenuFormValues) => Promise<void>
}) {
  const [values, setValues] = useState<MenuFormValues>(initial)
  const [categories, setCategories] = useState<string[]>([])
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    publicFetch<string[]>('/categories')
      .then(setCategories)
      .catch(() => {})
  }, [])

  const set = (patch: Partial<MenuFormValues>) => setValues((v) => ({ ...v, ...patch }))

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    try {
      await onSubmit(values)
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <input className={inputClass} placeholder="메뉴 이름" value={values.name} onChange={(e) => set({ name: e.target.value })} required />
      <textarea className={`${inputClass} resize-none`} rows={3} placeholder="메뉴 설명" value={values.description} onChange={(e) => set({ description: e.target.value })} />
      <input className={inputClass} placeholder="이미지 URL" value={values.imageUrl} onChange={(e) => set({ imageUrl: e.target.value })} />
      <label className="text-sm text-gray-500">
        조리 시간 (분)
        <input type="number" min={1} className={`${inputClass} mt-1`} value={values.cookTimeMinutes} onChange={(e) => set({ cookTimeMinutes: Number(e.target.value) })} required />
      </label>
      <select className={inputClass} value={values.difficulty} onChange={(e) => set({ difficulty: e.target.value })}>
        {DIFFICULTY_OPTIONS.map((d) => (
          <option key={d.value} value={d.value}>
            난이도: {d.label}
          </option>
        ))}
      </select>
      <select className={inputClass} value={values.category} onChange={(e) => set({ category: e.target.value })} required>
        <option value="" disabled>
          카테고리 선택
        </option>
        {categories.map((c) => (
          <option key={c} value={c}>
            {c}
          </option>
        ))}
      </select>
      <Button disabled={saving}>{saving ? '저장 중...' : submitLabel}</Button>
    </form>
  )
}
