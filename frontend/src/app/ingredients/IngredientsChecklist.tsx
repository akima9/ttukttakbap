'use client'

import { useState } from 'react'
import Link from 'next/link'

interface Ingredient {
  ingredientId: number
  name: string
  requiredAmount: number
  unit: string
  purchaseUnit: string
  purchaseLocation: string | null
  coupangUrl: string | null
}

interface Props {
  ingredients: Ingredient[]
  people: string
  menuId: string
}

export default function IngredientsChecklist({ ingredients, people, menuId }: Props) {
  const [checked, setChecked] = useState<Set<number>>(new Set())
  const [copied, setCopied] = useState(false)

  const toggle = (id: number) =>
    setChecked(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })

  const listText = ingredients
    .map(item => `${item.name} ${item.requiredAmount}${item.unit}`)
    .join('\n')

  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(listText)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const shareList = async () => {
    if (navigator.share) {
      await navigator.share({ title: '뚝딱밥 재료 목록', text: listText })
    } else {
      alert('공유 기능은 모바일 브라우저에서 사용 가능합니다.')
    }
  }

return (
    <>
      <div className="flex gap-3 mb-3">
        <button
          onClick={copyToClipboard}
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
      </div>

      <ul className="flex flex-col gap-3 mb-4">
        {ingredients.map(item => {
          const isChecked = checked.has(item.ingredientId)
          return (
            <li
              key={item.ingredientId}
              onClick={() => toggle(item.ingredientId)}
              className={`bg-white rounded-xl px-4 py-3 flex justify-between items-center shadow-sm border transition-colors cursor-pointer ${
                isChecked ? 'border-rose-200 bg-rose-50' : 'border-gray-100'
              }`}
            >
              <div className="flex items-center gap-3">
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
                <div>
                  <span className={`font-medium ${isChecked ? 'text-gray-400 line-through' : 'text-gray-800'}`}>
                    {item.name}
                  </span>
                  {item.purchaseLocation && item.purchaseLocation !== '마트' && (
                    <span className="ml-2 text-xs text-gray-400">{item.purchaseLocation}</span>
                  )}
                </div>
              </div>
              <div className="text-right">
                <p className={`font-semibold ${isChecked ? 'text-gray-400' : 'text-rose-500'}`}>
                  {item.requiredAmount}{item.unit}
                </p>
                <p className="text-xs text-gray-400">{item.purchaseUnit}</p>
                {item.coupangUrl && (
                  <a
                    href={item.coupangUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={e => e.stopPropagation()}
                    className="mt-1 inline-block text-xs text-[#C0392B] hover:underline"
                  >
                    쿠팡에서 보기
                  </a>
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
        className="block w-full py-3 rounded-xl text-center font-semibold text-white bg-rose-500 hover:bg-rose-600 transition-colors"
      >
        레시피 보기
      </Link>
    </>
  )
}
