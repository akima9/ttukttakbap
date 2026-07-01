'use client'

import { useState } from 'react'
import { kakaoAuthorizeUrl } from '@/lib/auth'
import ErrorMessage from '@/components/ErrorMessage'

export default function LoginPage() {
  const [error, setError] = useState('')

  const loginKakao = () => {
    try {
      window.location.href = kakaoAuthorizeUrl()
    } catch {
      setError('카카오 로그인 설정이 올바르지 않습니다.')
    }
  }

  return (
    <div className="mt-16 flex flex-col items-center text-center">
      <h1 className="text-xl font-bold text-gray-800">로그인</h1>
      <p className="mt-2 text-sm text-gray-500">
        즐겨찾기, 최근 본 메뉴, 내 냉장고를 이용하려면 로그인하세요.
      </p>

      <button
        onClick={loginKakao}
        className="mt-8 w-full max-w-xs rounded-xl bg-[#FEE500] px-4 py-3 text-sm font-semibold text-[#191600] hover:brightness-95 transition"
      >
        카카오로 로그인
      </button>

      {error && (
        <div className="mt-4 w-full max-w-xs">
          <ErrorMessage message={error} />
        </div>
      )}
    </div>
  )
}
