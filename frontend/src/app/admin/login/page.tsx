'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { verifyAdminLogin } from '@/lib/admin'
import Button from '@/components/Button'
import ErrorMessage from '@/components/ErrorMessage'

export default function AdminLoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const router = useRouter()

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const ok = await verifyAdminLogin(username, password)
      if (ok) router.push('/admin')
      else setError('아이디 또는 비밀번호가 올바르지 않습니다.')
    } catch {
      setError('로그인 중 오류가 발생했습니다. 서버 상태를 확인해주세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mt-10">
      <h1 className="text-xl font-bold text-gray-800 mb-6">어드민 로그인</h1>
      <form onSubmit={submit} className="flex flex-col gap-3">
        <input
          type="text"
          placeholder="아이디"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          className="rounded-xl border border-gray-200 px-4 py-3 text-sm"
          autoComplete="username"
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="rounded-xl border border-gray-200 px-4 py-3 text-sm"
          autoComplete="current-password"
        />
        {error && <ErrorMessage message={error} />}
        <Button disabled={loading || !username || !password}>{loading ? '확인 중...' : '로그인'}</Button>
      </form>
    </div>
  )
}
