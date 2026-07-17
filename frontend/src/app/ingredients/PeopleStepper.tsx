'use client'

import { useOptimistic, useTransition } from 'react'
import { useRouter } from 'next/navigation'

const MIN = 1
const MAX = 8

// 인원은 재료 양에만 영향을 주므로, 결과(재료)를 보면서 바로 조절한다.
// 재료 양은 서버가 계산하므로 쿼리를 바꿔 다시 받아오고, 숫자만 낙관적으로 먼저 반영해 조작감을 유지한다.
export default function PeopleStepper({ people, menuId }: { people: number; menuId: string }) {
  const router = useRouter()
  const [isPending, startTransition] = useTransition()
  const [optimisticPeople, setOptimisticPeople] = useOptimistic(people)

  const change = (next: number) => {
    if (next < MIN || next > MAX) return
    startTransition(() => {
      setOptimisticPeople(next)
      router.replace(`/ingredients?people=${next}&menuId=${menuId}`, { scroll: false })
    })
  }

  return (
    <div className={`flex items-center gap-2 transition-opacity ${isPending ? 'opacity-60' : ''}`}>
      <StepButton
        onClick={() => change(optimisticPeople - 1)}
        disabled={optimisticPeople <= MIN}
        label="인원 줄이기"
      >
        −
      </StepButton>
      <span className="text-sm font-semibold text-gray-800 w-12 text-center tabular-nums" aria-live="polite">
        {optimisticPeople}인분
      </span>
      <StepButton
        onClick={() => change(optimisticPeople + 1)}
        disabled={optimisticPeople >= MAX}
        label="인원 늘리기"
      >
        ＋
      </StepButton>
    </div>
  )
}

function StepButton({
  onClick,
  disabled,
  label,
  children,
}: {
  onClick: () => void
  disabled: boolean
  label: string
  children: React.ReactNode
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      aria-label={label}
      className="w-8 h-8 rounded-full bg-rose-100 text-rose-500 text-lg font-bold leading-none hover:bg-rose-200 transition-colors disabled:opacity-40 disabled:hover:bg-rose-100 disabled:cursor-not-allowed"
    >
      {children}
    </button>
  )
}
