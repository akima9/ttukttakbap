'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { authFetch, getUser } from '@/lib/auth'
import MenuCard, { type Menu } from '@/components/MenuCard'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'

type Tab = 'favorites' | 'history'

const TABS: { key: Tab; label: string; empty: string }[] = [
  { key: 'favorites', label: '즐겨찾기', empty: '아직 즐겨찾기한 메뉴가 없어요.' },
  { key: 'history', label: '최근 본 메뉴', empty: '최근 본 메뉴가 없어요.' },
]

// 로그인 상태면 해당 탭 목록을, 아니면 null을 반환한다(항상 비동기).
async function loadMenus(tab: Tab): Promise<Menu[] | null> {
  if (!getUser()) return null
  return authFetch<Menu[]>(`/me/${tab}`)
}

export default function MyPage() {
  const [tab, setTab] = useState<Tab>('favorites')
  const [loggedIn, setLoggedIn] = useState<boolean | null>(null)
  // 어느 탭의 결과인지 함께 담아, 탭 전환 중에는 이전 목록 대신 로딩을 보여준다.
  const [loaded, setLoaded] = useState<{ tab: Tab; menus: Menu[] } | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    loadMenus(tab)
      .then((result) => {
        if (!active) return
        if (result === null) {
          setLoggedIn(false)
          return
        }
        setLoggedIn(true)
        setError('')
        setLoaded({ tab, menus: result })
      })
      .catch(() => {
        if (!active) return
        setLoggedIn(true)
        setError('목록을 불러오지 못했습니다.')
      })
    return () => {
      active = false
    }
  }, [tab])

  const menus = loaded && loaded.tab === tab ? loaded.menus : null

  if (loggedIn === false) {
    return (
      <div className="mt-16 text-center">
        <p className="text-sm text-gray-500">내 메뉴는 로그인이 필요해요.</p>
        <Link href="/login" className="mt-4 inline-block text-sm text-orange-500 hover:underline">
          로그인하러 가기
        </Link>
      </div>
    )
  }

  const currentTab = TABS.find((t) => t.key === tab)!

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-800 mb-4">내 메뉴</h1>

      <div className="flex gap-2 mb-4">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`text-sm px-3 py-1.5 rounded-full border transition-colors ${
              tab === t.key
                ? 'bg-orange-500 text-white border-orange-500'
                : 'bg-white text-gray-500 border-gray-200 hover:border-orange-300'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {error ? (
        <ErrorMessage message={error} />
      ) : menus === null ? (
        <LoadingSpinner />
      ) : menus.length === 0 ? (
        <p className="py-16 text-center text-sm text-gray-400">{currentTab.empty}</p>
      ) : (
        <div className="flex flex-col gap-3">
          {menus.map((menu) => (
            <MenuCard key={menu.id} menu={menu} people="2" />
          ))}
        </div>
      )}
    </div>
  )
}
