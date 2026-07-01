'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { adminFetch, publicFetch } from '@/lib/admin'
import { useAdminGuard } from '@/lib/useAdminGuard'
import MenuForm, { MenuFormValues } from '../../MenuForm'
import RecipeManager from './RecipeManager'
import IngredientLinkManager from './IngredientLinkManager'
import ErrorMessage from '@/components/ErrorMessage'

export default function EditMenuPage() {
  const ready = useAdminGuard()
  const router = useRouter()
  const params = useParams<{ id: string }>()
  const menuId = params.id

  const [initial, setInitial] = useState<MenuFormValues | null>(null)
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (!ready) return
    publicFetch<MenuFormValues>(`/menus/${menuId}`)
      .then((m) =>
        setInitial({
          name: m.name,
          description: m.description,
          imageUrl: m.imageUrl,
          cookTimeMinutes: m.cookTimeMinutes,
          difficulty: m.difficulty,
          category: m.category,
        }),
      )
      .catch((e) => setError(e instanceof Error ? e.message : '불러오기 실패'))
  }, [ready, menuId])

  const save = async (values: MenuFormValues) => {
    setError('')
    setSaved(false)
    try {
      await adminFetch(`/admin/menus/${menuId}`, { method: 'PUT', body: JSON.stringify(values) })
      setSaved(true)
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장 실패')
    }
  }

  if (!ready) return null

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold text-gray-800">메뉴 수정</h1>
        <button onClick={() => router.push('/admin')} className="text-sm text-gray-400 hover:text-gray-600">
          목록으로
        </button>
      </div>

      {error && <div className="mb-3"><ErrorMessage message={error} /></div>}
      {saved && <p className="mb-3 text-sm text-green-600">저장되었습니다.</p>}

      {initial ? (
        <>
          <MenuForm initial={initial} submitLabel="메뉴 저장" onSubmit={save} />
          <RecipeManager menuId={menuId} />
          <IngredientLinkManager menuId={menuId} />
        </>
      ) : (
        !error && <p className="text-sm text-gray-400">불러오는 중...</p>
      )}

      <Link href="/admin" className="mt-8 block text-center text-sm text-gray-400 hover:text-gray-600">
        메뉴 목록으로 돌아가기
      </Link>
    </div>
  )
}
