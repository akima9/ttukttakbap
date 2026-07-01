'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { getAdminAuth } from './admin'

// 어드민 페이지 진입 가드. 토큰이 없으면 로그인으로 보내고, 있으면 ready=true.
export function useAdminGuard(): boolean {
  const router = useRouter()
  const [ready, setReady] = useState(false)

  useEffect(() => {
    // 토큰은 클라이언트 전용(sessionStorage)이라 마운트 후 effect에서만 확인 가능
    if (!getAdminAuth()) router.replace('/admin/login')
    // eslint-disable-next-line react-hooks/set-state-in-effect
    else setReady(true)
  }, [router])

  return ready
}
