'use client'

import { useEffect, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import { loginWithKakao } from '@/lib/auth'
import LoadingSpinner from '@/components/LoadingSpinner'
import ErrorMessage from '@/components/ErrorMessage'

export default function KakaoCallback({ code, error }: { code?: string; error?: string }) {
  const router = useRouter()
  const [failed, setFailed] = useState(error ? '카카오 로그인이 취소되었습니다.' : '')
  // React 18+ StrictMode에서 effect가 두 번 실행돼 code를 중복 교환하지 않도록 가드.
  const done = useRef(false)

  useEffect(() => {
    if (done.current || error || !code) return
    done.current = true
    loginWithKakao(code)
      .then(() => router.replace('/'))
      .catch(() => setFailed('로그인에 실패했습니다. 다시 시도해주세요.'))
  }, [code, error, router])

  if (failed) {
    return (
      <div className="mt-16 flex flex-col items-center">
        <ErrorMessage message={failed} />
        <button
          onClick={() => router.replace('/login')}
          className="mt-4 text-sm text-orange-500 hover:underline"
        >
          로그인으로 돌아가기
        </button>
      </div>
    )
  }

  return (
    <div className="mt-16">
      <LoadingSpinner />
      <p className="mt-3 text-center text-sm text-gray-500">로그인 중...</p>
    </div>
  )
}
