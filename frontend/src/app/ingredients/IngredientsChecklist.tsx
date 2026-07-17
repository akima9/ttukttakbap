'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'

interface Ingredient {
  ingredientId: number
  name: string
  requiredAmount: number
  unit: string
  coupangUrl: string | null
}

interface Props {
  ingredients: Ingredient[]
  people: string
  menuId: string
}

const STORAGE_PREFIX = 'ttukttak:checked:'
// 마지막 담기 후 24시간이 지나면 지난 장보기로 보고 버린다.
const EXPIRY_MS = 24 * 60 * 60 * 1000

// 저장은 effect가 아니라 사용자 액션에서 한다 — 마운트 시 빈 상태가 저장본을 덮어쓰는 경쟁을 원천 차단.
// (컴포넌트 밖에 둬 렌더 순수성 규칙과 무관하게 Date.now()를 쓴다.)
function persist(key: string, next: Set<number>) {
  try {
    if (next.size === 0) localStorage.removeItem(key)
    else localStorage.setItem(key, JSON.stringify({ ids: [...next], savedAt: Date.now() }))
  } catch {
    // 저장 실패(용량 초과·시크릿 모드)해도 화면 동작은 유지한다.
  }
}

export default function IngredientsChecklist({ ingredients, people, menuId }: Props) {
  const [checked, setChecked] = useState<Set<number>>(new Set())
  const [copied, setCopied] = useState(false)

  const storageKey = `${STORAGE_PREFIX}${menuId}`
  // 재료 ID 목록을 원시값으로 고정해, 부모 리렌더로 배열 참조만 바뀔 때 로드 effect가 재실행되지 않게 한다.
  const ingredientIdsKey = ingredients.map(item => item.ingredientId).join(',')

  useEffect(() => {
    try {
      const raw = localStorage.getItem(storageKey)
      if (!raw) return
      const { ids, savedAt } = JSON.parse(raw) as { ids: number[]; savedAt: number }
      if (!Array.isArray(ids) || Date.now() - savedAt > EXPIRY_MS) {
        localStorage.removeItem(storageKey)
        return
      }
      // 메뉴 재료가 바뀌었을 수 있으니 현재 목록에 있는 ID만 살린다(진행률이 깨지지 않게).
      const valid = new Set(ingredientIdsKey.split(',').map(Number))
      const restored = ids.filter(id => valid.has(id))
      // localStorage는 브라우저 전용이라 마운트 후 effect에서 로드해야 SSR 하이드레이션 미스매치가 없다.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      if (restored.length) setChecked(new Set(restored))
    } catch {
      // 저장본이 손상됐으면 빈 상태로 시작한다.
    }
  }, [storageKey, ingredientIdsKey])

  const toggle = (id: number) => {
    const next = new Set(checked)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    setChecked(next)
    persist(storageKey, next)
  }

  const reset = () => {
    setChecked(new Set())
    persist(storageKey, new Set())
  }

  const total = ingredients.length
  const done = checked.size
  const percent = total ? Math.round((done / total) * 100) : 0
  const isComplete = total > 0 && done === total

  // 남은(미체크) 재료를 위로, 담은 재료를 아래로. 각 그룹 내부 순서는 유지되는 안정 정렬.
  const ordered = [...ingredients].sort(
    (a, b) => Number(checked.has(a.ingredientId)) - Number(checked.has(b.ingredientId))
  )

  const listText = ingredients
    .map(item => `${item.name} ${item.requiredAmount}${item.unit}`)
    .join('\n')

  const copyList = async () => {
    try {
      await navigator.clipboard.writeText(listText)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
      return true
    } catch {
      return false
    }
  }

  const shareList = async () => {
    if (navigator.share) {
      try {
        await navigator.share({ title: '뚝딱밥 재료 목록', text: listText })
        return
      } catch (e) {
        // 사용자가 공유 시트를 취소하면 AbortError가 발생 — 정상이므로 무시한다.
        if (e instanceof DOMException && e.name === 'AbortError') return
        // 그 외 실패는 아래 복사로 폴백한다.
      }
    }
    // 공유 미지원이거나 실패 시 클립보드 복사로 대체한다.
    const ok = await copyList()
    if (!ok) alert('이 브라우저에서는 공유·복사를 지원하지 않습니다.')
  }

return (
    <>
      <div className="flex gap-3 mb-3">
        <button
          onClick={copyList}
          className="flex items-center gap-1.5 text-sm text-gray-400 hover:text-rose-500 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
          </svg>
          {copied ? '복사됨!' : '목록 복사'}
        </button>
        <button
          onClick={shareList}
          className="flex items-center gap-1.5 text-sm text-gray-400 hover:text-rose-500 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
          </svg>
          목록 공유
        </button>
        {done > 0 && (
          <button
            onClick={reset}
            className="flex items-center gap-1.5 text-sm text-gray-400 hover:text-rose-500 transition-colors"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            초기화
          </button>
        )}
      </div>

      {total > 0 && (
        <div className="mb-4">
          <div className="flex items-center justify-between mb-2">
            <span className={`text-sm font-semibold ${isComplete ? 'text-rose-500' : 'text-gray-800'}`}>
              {isComplete ? '장보기 완료!' : `${done}/${total} 담음`}
            </span>
            <span className="text-sm font-medium text-gray-400">{percent}%</span>
          </div>
          <div
            role="progressbar"
            aria-valuemin={0}
            aria-valuemax={100}
            aria-valuenow={percent}
            aria-label="장보기 진행률"
            className="h-2 w-full rounded-full bg-gray-100 overflow-hidden"
          >
            <div
              className="h-full rounded-full bg-rose-500 transition-all duration-300 ease-out"
              style={{ width: `${percent}%` }}
            />
          </div>
        </div>
      )}

      {isComplete && (
        <div className="mb-4 flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3">
          <span className="text-xl">🎉</span>
          <span className="text-sm font-semibold text-rose-500">
            재료를 다 담았어요! 이제 요리를 시작해볼까요?
          </span>
        </div>
      )}

      <ul className="flex flex-col gap-3 mb-4">
        {ordered.map(item => {
          const isChecked = checked.has(item.ingredientId)
          return (
            <li
              key={item.ingredientId}
              onClick={() => toggle(item.ingredientId)}
              className={`bg-white rounded-xl px-4 py-3 flex justify-between items-center shadow-sm border transition-colors cursor-pointer ${
                isChecked ? 'border-rose-200 bg-rose-50' : 'border-gray-100'
              }`}
            >
              <div className="flex items-center gap-3 min-w-0">
                <span
                  className={`w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 ${
                    isChecked ? 'border-rose-500 bg-rose-500' : 'border-gray-300'
                  }`}
                >
                  {isChecked && (
                    <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </span>
                <span
                  className={`font-medium break-keep ${
                    isChecked ? 'text-gray-600 line-through' : 'text-gray-800'
                  }`}
                >
                  {item.name}
                </span>
              </div>
              <div className="flex items-center gap-3 shrink-0">
                <p
                  className={`font-semibold text-right min-w-[3.5rem] ${
                    isChecked ? 'text-gray-600' : 'text-rose-600'
                  }`}
                >
                  {item.requiredAmount}{item.unit}
                </p>
                {item.coupangUrl ? (
                  // before 의사요소로 히트 영역만 44px로 넓힌다 — 시각 크기(36px)는 그대로 두고 탭 타깃만 확보.
                  <a
                    href={item.coupangUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={e => e.stopPropagation()}
                    aria-label={`${item.name} 구매하러 가기 (쿠팡, 새 창)`}
                    className={`relative -my-1 w-16 h-9 shrink-0 inline-flex items-center justify-center gap-1 rounded-lg border text-xs font-medium transition-colors before:absolute before:content-[''] before:inset-x-0 before:-inset-y-1 hover:bg-gray-100 hover:border-gray-300 hover:text-gray-800 active:bg-gray-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-500 focus-visible:ring-offset-2 border-gray-200 text-gray-600 ${
                      isChecked ? 'bg-transparent' : 'bg-gray-50'
                    }`}
                  >
                    구매
                    <svg className="w-3 h-3 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5} aria-hidden="true">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                    </svg>
                  </a>
                ) : (
                  // 링크 없는 재료도 같은 폭을 예약해 필요량 우측 열이 어긋나지 않게 한다.
                  <span className="w-16 shrink-0" aria-hidden="true" />
                )}
              </div>
            </li>
          )
        })}
      </ul>

      <p className="mb-4 text-xs text-gray-400 text-center">
        이 페이지의 일부 링크는 쿠팡 파트너스 링크로, 구매 시 일정 수수료를 받을 수 있습니다.
      </p>

      <Link
        href={`/recipe?people=${people}&menuId=${menuId}`}
        className={`block w-full py-3 rounded-xl text-center font-semibold text-white bg-rose-500 hover:bg-rose-600 transition-all ${
          isComplete ? 'scale-[1.02] ring-2 ring-rose-300 ring-offset-2' : ''
        }`}
      >
        레시피 보기
      </Link>
    </>
  )
}
