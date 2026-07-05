'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { authFetch, getUser } from '@/lib/auth'
import MenuCard, { type Menu } from '@/components/MenuCard'
import LoadingSpinner from '@/components/LoadingSpinner'

interface PageResponse {
  content: Menu[]
}

// 로그인 상태면 냉장고 우선 추천 목록을, 아니면 null을 반환한다(항상 비동기).
async function loadFridgeMenus(people: string): Promise<Menu[] | null> {
  if (!getUser()) return null
  try {
    const data = await authFetch<PageResponse>(`/menus/recommend?people=${people}&useMyFridge=true&size=20`)
    return data.content
  } catch {
    return []
  }
}

// 냉장고 보유 재료로 우선 추천받는 목록. useMyFridge는 JWT가 필요해 클라이언트에서 조회한다.
export default function FridgeMenuList({ people }: { people: string }) {
  const [menus, setMenus] = useState<Menu[] | null>(null)
  const [loggedIn, setLoggedIn] = useState<boolean | null>(null)

  useEffect(() => {
    let active = true
    loadFridgeMenus(people).then((result) => {
      if (!active) return
      if (result === null) {
        setLoggedIn(false)
        return
      }
      setLoggedIn(true)
      setMenus(result)
    })
    return () => {
      active = false
    }
  }, [people])

  if (loggedIn === false) {
    return (
      <div className="mt-16 text-center">
        <p className="text-sm text-gray-500">냉장고 기반 추천은 로그인이 필요해요.</p>
        <Link href="/login" className="mt-4 inline-block text-sm text-rose-500 hover:underline">
          로그인하러 가기
        </Link>
      </div>
    )
  }

  if (menus === null) return <LoadingSpinner />

  return (
    <div>
      <div className="flex items-baseline justify-between mb-4">
        <h1 className="text-xl font-bold text-gray-800">냉장고 재료 우선 추천</h1>
        <Link href={`/menu?people=${people}`} className="text-sm text-gray-400 hover:text-rose-500">
          일반 추천
        </Link>
      </div>

      {menus.length === 0 ? (
        <p className="py-16 text-center text-sm text-gray-400">
          냉장고에 재료를 담으면 우선 추천해드려요.{' '}
          <Link href="/fridge" className="text-rose-500 hover:underline">
            냉장고 관리
          </Link>
        </p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          {menus.map((menu) => (
            <MenuCard key={menu.id} menu={menu} people={people} />
          ))}
        </div>
      )}
    </div>
  )
}
