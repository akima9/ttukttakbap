'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { getUser, logout, type AuthUser } from '@/lib/auth'

export default function Header() {
  const pathname = usePathname()
  const router = useRouter()
  const isHome = pathname === '/'
  const [user, setUser] = useState<AuthUser | null>(null)

  // 로그인 상태는 sessionStorage에 있으므로 마운트/경로 변경/auth-change 이벤트마다 다시 읽는다.
  useEffect(() => {
    const sync = () => setUser(getUser())
    sync()
    window.addEventListener('auth-change', sync)
    return () => window.removeEventListener('auth-change', sync)
  }, [pathname])

  const handleLogout = async () => {
    await logout()
    router.push('/')
  }

  return (
    <header className="sticky top-0 z-10 bg-white border-b border-gray-100">
      <div className="max-w-5xl mx-auto px-4 h-14 flex items-center gap-3">
        {!isHome && (
          <button
            onClick={() => router.back()}
            className="text-gray-500 hover:text-gray-800 transition-colors p-2 -ml-2"
            aria-label="뒤로 가기"
          >
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
        )}
        <Link href="/" className="text-xl font-bold text-rose-500">
          뚝딱밥
        </Link>

        <div className="ml-auto flex items-center gap-3 text-sm">
          {user ? (
            <>
              <Link href="/my" className="text-gray-500 hover:text-rose-500 transition-colors">
                내 메뉴
              </Link>
              <Link href="/fridge" className="text-gray-500 hover:text-rose-500 transition-colors">
                냉장고
              </Link>
              <span className="text-gray-600">{user.nickname}</span>
              <button onClick={handleLogout} className="text-gray-400 hover:text-gray-700 transition-colors">
                로그아웃
              </button>
            </>
          ) : (
            <Link href="/login" className="text-gray-500 hover:text-rose-500 transition-colors">
              로그인
            </Link>
          )}
        </div>
      </div>
    </header>
  )
}
