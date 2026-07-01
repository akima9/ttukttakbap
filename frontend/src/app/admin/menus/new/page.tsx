'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { adminFetch } from '@/lib/admin'
import { useAdminGuard } from '@/lib/useAdminGuard'
import MenuForm, { MenuFormValues } from '../MenuForm'
import ErrorMessage from '@/components/ErrorMessage'

export default function NewMenuPage() {
  const ready = useAdminGuard()
  const router = useRouter()
  const [error, setError] = useState('')

  const create = async (values: MenuFormValues) => {
    setError('')
    try {
      const created = await adminFetch<{ id: number }>('/admin/menus', {
        method: 'POST',
        body: JSON.stringify(values),
      })
      router.push(`/admin/menus/${created.id}/edit`)
    } catch (e) {
      setError(e instanceof Error ? e.message : '생성 실패')
    }
  }

  if (!ready) return null

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-800 mb-4">새 메뉴</h1>
      {error && <div className="mb-3"><ErrorMessage message={error} /></div>}
      <MenuForm
        initial={{ name: '', description: '', imageUrl: '', cookTimeMinutes: 30, difficulty: 'EASY', category: '' }}
        submitLabel="메뉴 생성"
        onSubmit={create}
      />
      <p className="mt-3 text-xs text-gray-400">생성 후 재료 연결과 레시피 단계를 추가할 수 있어요.</p>
    </div>
  )
}
