'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import { getUser, logout, type AuthUser } from '@/lib/auth'

export default function Header() {
  const pathname = usePathname()
  const router = useRouter()
  const isHome = pathname === '/'
  const [user, setUser] = useState<AuthUser | null>(null)
  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  // 로그인 상태는 sessionStorage에 있으므로 마운트/경로 변경/auth-change 이벤트마다 다시 읽는다.
  useEffect(() => {
    const sync = () => setUser(getUser())
    sync()
    window.addEventListener('auth-change', sync)
    return () => window.removeEventListener('auth-change', sync)
  }, [pathname])

  // 드롭다운이 열려 있을 때만 바깥 클릭/Escape로 닫는 리스너를 건다.
  useEffect(() => {
    if (!open) return
    const onPointerDown = (e: PointerEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) setOpen(false)
    }
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('pointerdown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('pointerdown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open])

  const handleLogout = async () => {
    setOpen(false)
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
              <div className="relative" ref={menuRef}>
                <button
                  onClick={() => setOpen((o) => !o)}
                  aria-haspopup="menu"
                  aria-expanded={open}
                  className="flex items-center gap-1 rounded-full hover:opacity-90 transition-opacity"
                >
                  <span className="w-8 h-8 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center font-semibold">
                    {user.nickname.charAt(0)}
                  </span>
                  <svg className="w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>
                {open && (
                  <div
                    role="menu"
                    className="absolute right-0 top-full mt-1 w-40 bg-white border border-gray-100 rounded-xl shadow-sm py-1"
                  >
                    <p className="px-3 py-2 text-gray-600 border-b border-gray-100">
                      {user.nickname}님
                    </p>
                    <button
                      role="menuitem"
                      onClick={handleLogout}
                      className="w-full text-left px-3 py-2 text-gray-500 hover:text-gray-800 hover:bg-gray-50 transition-colors"
                    >
                      로그아웃
                    </button>
                  </div>
                )}
              </div>
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
