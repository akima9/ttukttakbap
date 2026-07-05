'use client'

import { useState } from 'react'

// 메뉴 이미지 썸네일. imageUrl이 비었거나 로드에 실패하면 플레이스홀더를 보여준다.
// 관리자가 임의 도메인의 URL을 입력하므로 next/image의 remotePatterns로 제한할 수 없어 <img>를 쓴다.
export default function MenuThumbnail({
  src,
  alt,
  className = '',
}: {
  src: string
  alt: string
  className?: string
}) {
  const [error, setError] = useState(false)
  const valid = src.trim() !== '' && !error

  if (!valid) {
    return (
      <div className={`flex items-center justify-center bg-rose-50 text-4xl ${className}`} aria-hidden>
        🍚
      </div>
    )
  }

  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img src={src} alt={alt} className={`object-cover ${className}`} onError={() => setError(true)} />
  )
}
