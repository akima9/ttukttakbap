// 어드민 인증/요청 헬퍼 (클라이언트 전용).
// 백엔드가 HTTP Basic이라 username:password를 Base64로 만들어 sessionStorage에 보관하고,
// admin API 호출 시 Authorization 헤더로 전송한다.

const API = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1'
const STORAGE_KEY = 'adminAuth'

export function getAdminAuth(): string | null {
  if (typeof window === 'undefined') return null
  return sessionStorage.getItem(STORAGE_KEY)
}

export function clearAdminAuth() {
  if (typeof window !== 'undefined') sessionStorage.removeItem(STORAGE_KEY)
}

// 로그인 검증: 인증이 필요한 엔드포인트를 직접 호출해 성공 시 토큰을 저장한다.
export async function verifyAdminLogin(username: string, password: string): Promise<boolean> {
  const token = btoa(`${username}:${password}`)
  const res = await fetch(`${API}/admin/ingredients`, {
    headers: { Authorization: `Basic ${token}` },
    cache: 'no-store',
  })
  if (res.ok) {
    sessionStorage.setItem(STORAGE_KEY, token)
    return true
  }
  return false
}

// 인증 없이 읽는 공개 GET (메뉴 조회 등).
export async function publicFetch<T>(path: string): Promise<T> {
  const res = await fetch(`${API}${path}`, { cache: 'no-store' })
  if (!res.ok) throw new Error('데이터를 불러오지 못했습니다.')
  return res.json() as Promise<T>
}

// 인증이 필요한 admin 요청. 401이면 토큰을 비우고 로그인으로 보낸다.
export async function adminFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getAdminAuth()
  const headers = new Headers(init.headers)
  if (token) headers.set('Authorization', `Basic ${token}`)
  if (init.body) headers.set('Content-Type', 'application/json')

  const res = await fetch(`${API}${path}`, { ...init, headers, cache: 'no-store' })

  if (res.status === 401) {
    clearAdminAuth()
    if (typeof window !== 'undefined') window.location.href = '/admin/login'
    throw new Error('인증이 만료되었습니다. 다시 로그인해주세요.')
  }
  if (!res.ok) {
    const message = await res
      .json()
      .then((d) => d.message as string)
      .catch(() => null)
    throw new Error(message || '요청에 실패했습니다.')
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

export const DIFFICULTY_OPTIONS = [
  { value: 'EASY', label: '쉬움' },
  { value: 'MEDIUM', label: '보통' },
  { value: 'HARD', label: '어려움' },
]
