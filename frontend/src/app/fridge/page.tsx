'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { authFetch, getUser } from '@/lib/auth'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'

interface Ingredient {
  id: number
  name: string
  purchaseUnit: string
}

const API = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'

// 로그인 상태면 전체 재료 + 내 냉장고 재료를, 아니면 null을 반환한다(항상 비동기).
async function loadFridge(): Promise<{ ingredients: Ingredient[]; fridgeIds: Set<number> } | null> {
  if (!getUser()) return null
  const [all, mine] = await Promise.all([
    fetch(`${API}/ingredients`, { cache: 'no-store' }).then((r) => r.json() as Promise<Ingredient[]>),
    authFetch<Ingredient[]>('/me/fridge'),
  ])
  return { ingredients: all, fridgeIds: new Set(mine.map((i) => i.id)) }
}

export default function FridgePage() {
  const [loggedIn, setLoggedIn] = useState<boolean | null>(null)
  const [ingredients, setIngredients] = useState<Ingredient[]>([])
  const [fridgeIds, setFridgeIds] = useState<Set<number>>(new Set())
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    loadFridge()
      .then((result) => {
        if (!active) return
        if (!result) {
          setLoggedIn(false)
          return
        }
        setLoggedIn(true)
        setIngredients(result.ingredients)
        setFridgeIds(result.fridgeIds)
      })
      .catch(() => {
        if (!active) return
        setLoggedIn(true)
        setError('냉장고 정보를 불러오지 못했습니다.')
      })
    return () => {
      active = false
    }
  }, [])

  const toggle = async (id: number) => {
    const has = fridgeIds.has(id)
    setFridgeIds((prev) => {
      const next = new Set(prev)
      if (has) next.delete(id)
      else next.add(id)
      return next
    })
    try {
      await authFetch(`/me/fridge/${id}`, { method: has ? 'DELETE' : 'POST' })
    } catch {
      setFridgeIds((prev) => {
        const next = new Set(prev)
        if (has) next.add(id)
        else next.delete(id)
        return next
      })
    }
  }

  if (loggedIn === false) {
    return (
      <div className="mt-16 text-center">
        <p className="text-sm text-gray-500">냉장고를 관리하려면 로그인이 필요해요.</p>
        <Link href="/login" className="mt-4 inline-block text-sm text-rose-500 hover:underline">
          로그인하러 가기
        </Link>
      </div>
    )
  }

  if (loggedIn === null) return <LoadingSpinner />
  if (error) return <ErrorMessage message={error} />

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-800">내 냉장고</h1>
      <p className="mt-1 mb-4 text-sm text-gray-400">
        가지고 있는 재료를 선택하면, 그 재료로 만들 수 있는 메뉴를 우선 추천해드려요.
      </p>

      <div className="flex flex-wrap gap-2">
        {ingredients.map((ing) => {
          const has = fridgeIds.has(ing.id)
          return (
            <button
              key={ing.id}
              onClick={() => toggle(ing.id)}
              className={`text-sm px-3 py-1.5 rounded-full border transition-colors ${
                has
                  ? 'bg-rose-500 text-white border-rose-500'
                  : 'bg-white text-gray-500 border-gray-200 hover:border-rose-300'
              }`}
            >
              {ing.name}
            </button>
          )
        })}
      </div>

      <Link
        href="/menu?useMyFridge=1"
        className="mt-8 block w-full max-w-xs mx-auto py-3 rounded-xl text-center font-semibold text-white bg-rose-500 hover:bg-rose-600 transition-colors"
      >
        내 냉장고 재료로 메뉴 추천받기
      </Link>
    </div>
  )
}
