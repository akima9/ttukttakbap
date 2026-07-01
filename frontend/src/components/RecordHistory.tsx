'use client'

import { useEffect, useRef } from 'react'
import { authFetch, getUser } from '@/lib/auth'

// 메뉴 상세(재료) 진입 시 최근 본 메뉴로 1회 기록한다. 비로그인이면 아무것도 하지 않는다.
export default function RecordHistory({ menuId }: { menuId: string }) {
  const done = useRef(false)

  useEffect(() => {
    if (done.current || !getUser()) return
    done.current = true
    authFetch(`/me/history/${menuId}`, { method: 'POST' }).catch(() => {})
  }, [menuId])

  return null
}
