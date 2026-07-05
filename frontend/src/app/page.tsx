'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Button from '@/components/Button'

export default function HomePage() {
  const [people, setPeople] = useState(2)
  const router = useRouter()

  return (
    <div className="min-h-[calc(100vh-6.5rem)] flex flex-col items-center justify-center gap-10">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-800">오늘 몇 명이 드세요?</h1>
        <p className="mt-2 text-gray-500">인원 수에 맞게 재료를 계산해드려요</p>
      </div>

      <div className="flex flex-col items-center gap-4">
        <div className="flex items-center gap-8">
          <button
            onClick={() => setPeople(p => Math.max(1, p - 1))}
            className="w-12 h-12 rounded-full bg-rose-100 text-rose-500 text-2xl font-bold hover:bg-rose-200 transition-colors"
          >
            -
          </button>
          <span className="text-5xl font-bold text-gray-800 w-16 text-center">{people}</span>
          <button
            onClick={() => setPeople(p => Math.min(8, p + 1))}
            className="w-12 h-12 rounded-full bg-rose-100 text-rose-500 text-2xl font-bold hover:bg-rose-200 transition-colors"
          >
            +
          </button>
        </div>
        <p className="text-sm text-gray-400">1 ~ 8명</p>
      </div>

      <Button onClick={() => router.push(`/menu?people=${people}`)} className="max-w-xs">
        메뉴 추천받기
      </Button>
    </div>
  )
}
