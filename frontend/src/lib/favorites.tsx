'use client'

import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { authFetch, getUser } from './auth'

// 즐겨찾기 상태를 앱 전역에서 한 번만 로드해 공유한다.
// 목록 페이지는 서버 렌더라 JWT가 없어 isFavorite을 못 채우므로, 클라이언트에서 오버레이한다.
interface FavoritesContextValue {
  loggedIn: boolean
  isFavorite: (menuId: number) => boolean
  toggle: (menuId: number) => Promise<void>
}

const FavoritesContext = createContext<FavoritesContextValue | null>(null)

// 로그인 상태면 즐겨찾기 메뉴 id 목록을, 아니면 빈 배열을 반환한다(항상 비동기).
async function fetchFavoriteIds(): Promise<number[]> {
  if (!getUser()) return []
  try {
    const favorites = await authFetch<{ id: number }[]>('/me/favorites')
    return favorites.map((f) => f.id)
  } catch {
    return []
  }
}

export function FavoritesProvider({ children }: { children: React.ReactNode }) {
  const [ids, setIds] = useState<Set<number>>(new Set())
  const [loggedIn, setLoggedIn] = useState(false)

  useEffect(() => {
    let active = true
    const load = async () => {
      const isLoggedIn = !!getUser()
      const favoriteIds = await fetchFavoriteIds()
      if (!active) return
      setLoggedIn(isLoggedIn)
      setIds(new Set(favoriteIds))
    }
    load()
    window.addEventListener('auth-change', load)
    return () => {
      active = false
      window.removeEventListener('auth-change', load)
    }
  }, [])

  const isFavorite = useCallback((menuId: number) => ids.has(menuId), [ids])

  const toggle = useCallback(
    async (menuId: number) => {
      const wasFavorite = ids.has(menuId)
      // 낙관적 업데이트 후 실패 시 롤백.
      setIds((prev) => {
        const next = new Set(prev)
        if (wasFavorite) next.delete(menuId)
        else next.add(menuId)
        return next
      })
      try {
        await authFetch(`/me/favorites/${menuId}`, { method: wasFavorite ? 'DELETE' : 'POST' })
      } catch (e) {
        setIds((prev) => {
          const next = new Set(prev)
          if (wasFavorite) next.add(menuId)
          else next.delete(menuId)
          return next
        })
        throw e
      }
    },
    [ids],
  )

  return (
    <FavoritesContext.Provider value={{ loggedIn, isFavorite, toggle }}>
      {children}
    </FavoritesContext.Provider>
  )
}

export function useFavorites(): FavoritesContextValue {
  const ctx = useContext(FavoritesContext)
  if (!ctx) throw new Error('useFavorites must be used within FavoritesProvider')
  return ctx
}
