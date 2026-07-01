'use client'

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { adminFetch, clearAdminAuth, publicFetch, DIFFICULTY_OPTIONS } from '@/lib/admin'
import { useAdminGuard } from '@/lib/useAdminGuard'
import ErrorMessage from '@/components/ErrorMessage'

interface Menu {
  id: number
  name: string
  category: string
  difficulty: string
  cookTimeMinutes: number
}

interface MenuPage {
  content: Menu[]
}

const DIFFICULTY_LABEL = Object.fromEntries(DIFFICULTY_OPTIONS.map((d) => [d.value, d.label]))

export default function AdminHomePage() {
  const ready = useAdminGuard()
  const router = useRouter()
  const [menus, setMenus] = useState<Menu[]>([])
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      const data = await publicFetch<MenuPage>('/menus?size=100')
      setMenus(data.content)
    } catch (e) {
      setError(e instanceof Error ? e.message : '불러오기 실패')
    }
  }, [])

  useEffect(() => {
    // 인증 준비 후 외부 API에서 1회 로드 (effect escape hatch)
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (ready) load()
  }, [ready, load])

  const remove = async (id: number, name: string) => {
    if (!confirm(`'${name}' 메뉴를 삭제할까요?`)) return
    try {
      await adminFetch(`/admin/menus/${id}`, { method: 'DELETE' })
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제 실패')
    }
  }

  const logout = () => {
    clearAdminAuth()
    router.push('/admin/login')
  }

  if (!ready) return null

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-bold text-gray-800">메뉴 관리</h1>
        <button onClick={logout} className="text-sm text-gray-400 hover:text-gray-600">
          로그아웃
        </button>
      </div>

      <div className="flex gap-2 mb-4">
        <Link href="/admin/menus/new" className="flex-1 text-center py-2.5 rounded-xl text-sm font-semibold text-white bg-orange-500 hover:bg-orange-600">
          + 새 메뉴
        </Link>
        <Link href="/admin/ingredients" className="flex-1 text-center py-2.5 rounded-xl text-sm font-semibold text-orange-500 bg-orange-50 hover:bg-orange-100">
          재료 관리
        </Link>
      </div>

      {error && <ErrorMessage message={error} />}

      <div className="flex flex-col gap-2 mt-2">
        {menus.map((menu) => (
          <div key={menu.id} className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 flex justify-between items-center">
            <div>
              <p className="font-semibold text-gray-800">{menu.name}</p>
              <p className="text-xs text-gray-400 mt-0.5">
                {menu.category} · {DIFFICULTY_LABEL[menu.difficulty] ?? menu.difficulty} · {menu.cookTimeMinutes}분
              </p>
            </div>
            <div className="flex gap-2 text-sm">
              <Link href={`/admin/menus/${menu.id}/edit`} className="text-orange-500 hover:underline">
                수정
              </Link>
              <button onClick={() => remove(menu.id, menu.name)} className="text-red-400 hover:underline">
                삭제
              </button>
            </div>
          </div>
        ))}
        {menus.length === 0 && !error && <p className="py-12 text-center text-sm text-gray-400">등록된 메뉴가 없습니다.</p>}
      </div>
    </div>
  )
}
