// 사용자 인증 헬퍼 (클라이언트 전용).
// 카카오 인가 코드를 백엔드와 교환해 받은 JWT(access/refresh) + 사용자 정보를
// sessionStorage에 보관하고, 인증이 필요한 API 호출 시 Authorization 헤더로 전송한다.

const API = 'http://localhost:8080/api/v1'
const ACCESS_KEY = 'accessToken'
const REFRESH_KEY = 'refreshToken'
const USER_KEY = 'authUser'

export interface AuthUser {
  id: number
  nickname: string
  email: string | null
  profileImageUrl: string | null
  role: string
}

interface TokenResponse {
  accessToken: string
  refreshToken: string
  user: AuthUser
}

// 로그인 상태 변화를 Header 등 다른 컴포넌트에 알린다.
function notifyAuthChange() {
  if (typeof window !== 'undefined') window.dispatchEvent(new Event('auth-change'))
}

export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null
  return sessionStorage.getItem(ACCESS_KEY)
}

export function getUser(): AuthUser | null {
  if (typeof window === 'undefined') return null
  const raw = sessionStorage.getItem(USER_KEY)
  return raw ? (JSON.parse(raw) as AuthUser) : null
}

function setAuth(data: TokenResponse) {
  sessionStorage.setItem(ACCESS_KEY, data.accessToken)
  sessionStorage.setItem(REFRESH_KEY, data.refreshToken)
  sessionStorage.setItem(USER_KEY, JSON.stringify(data.user))
  notifyAuthChange()
}

function clearAuth() {
  sessionStorage.removeItem(ACCESS_KEY)
  sessionStorage.removeItem(REFRESH_KEY)
  sessionStorage.removeItem(USER_KEY)
  notifyAuthChange()
}

// 프론트에서 사용하는 카카오 콜백 URI. 백엔드 토큰 교환·카카오 콘솔 등록값과 일치해야 한다.
export function kakaoRedirectUri(): string {
  return `${window.location.origin}/login/kakao/callback`
}

// 카카오 인가 페이지로 이동시킬 URL.
export function kakaoAuthorizeUrl(): string {
  const clientId = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID
  if (!clientId) throw new Error('NEXT_PUBLIC_KAKAO_CLIENT_ID 가 설정되지 않았습니다.')
  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: kakaoRedirectUri(),
    response_type: 'code',
    scope: 'profile_nickname',
  })
  return `https://kauth.kakao.com/oauth/authorize?${params.toString()}`
}

// 카카오 인가 코드를 백엔드와 교환해 로그인한다.
export async function loginWithKakao(code: string): Promise<void> {
  const res = await fetch(`${API}/auth/login/kakao`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, redirectUri: kakaoRedirectUri() }),
  })
  if (!res.ok) throw new Error('로그인에 실패했습니다.')
  setAuth((await res.json()) as TokenResponse)
}

export async function logout(): Promise<void> {
  const refreshToken = sessionStorage.getItem(REFRESH_KEY)
  if (refreshToken) {
    await fetch(`${API}/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    }).catch(() => {})
  }
  clearAuth()
}

// access 토큰이 만료(401)되면 refresh로 한 번 갱신 후 재시도한다.
async function tryRefresh(): Promise<boolean> {
  const refreshToken = sessionStorage.getItem(REFRESH_KEY)
  if (!refreshToken) return false
  const res = await fetch(`${API}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!res.ok) return false
  const data = (await res.json()) as { accessToken: string; refreshToken: string }
  sessionStorage.setItem(ACCESS_KEY, data.accessToken)
  sessionStorage.setItem(REFRESH_KEY, data.refreshToken)
  return true
}

// 인증이 필요한 요청. 401이면 refresh 후 1회 재시도, 그래도 실패하면 로그아웃 처리한다.
export async function authFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const call = () => {
    const headers = new Headers(init.headers)
    const token = getAccessToken()
    if (token) headers.set('Authorization', `Bearer ${token}`)
    if (init.body) headers.set('Content-Type', 'application/json')
    return fetch(`${API}${path}`, { ...init, headers, cache: 'no-store' })
  }

  let res = await call()
  if (res.status === 401 && (await tryRefresh())) {
    res = await call()
  }
  if (res.status === 401) {
    clearAuth()
    throw new Error('로그인이 필요합니다.')
  }
  if (!res.ok) {
    const message = await res
      .json()
      .then((d) => d.message as string)
      .catch(() => null)
    throw new Error(message || '요청에 실패했습니다.')
  }
  // 201/204 등 body가 없는 응답(즐겨찾기·기록 추가)에서 res.json()이 터지지 않도록 텍스트로 파싱.
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}
