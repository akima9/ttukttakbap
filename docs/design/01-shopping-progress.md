# 장보기 진행률 표시 — UI 스펙

> 작성일: 2026-07-17 · 상태: 초안 · 슬러그: 01-shopping-progress · 다음 담당: frontend-dev

재료 체크리스트 상단에 "담은 수 / 전체 수 + 진행바"를 표시하는 진행률 UI의 비주얼 스펙.
기능·정책·엣지 케이스는 [`../planning/01-shopping-progress.md`](../planning/01-shopping-progress.md) 참조(중복 서술 안 함). **문서 스펙이며 코드 구현은 frontend-dev 몫.**

## 0. 토큰 현황 (근거)

- `globals.css`에는 커스텀 디자인 토큰이 사실상 없음(`--background`/`--foreground`만). 색·간격은 **Tailwind 기본 유틸리티**로 지정한다.
- 대상 컴포넌트(`IngredientsChecklist.tsx`)가 이미 쓰는 팔레트에 맞춘다:
  - 브랜드/체크 강조: `rose-500`(`#f43f5e`), 진한 상태 `rose-600`(`#e11d48`), 연한 배경 `rose-50`(`#fff1f2`), 연한 보더 `rose-200`(`#fecdd3`)
  - 중립: `gray-100`(`#f3f4f6`, 트랙/보더), `gray-300`(`#d1d5db`), `gray-400`(`#9ca3af`, 보조 텍스트), `gray-800`(`#1f2937`, 본문)
  - 흰색 `#ffffff`
- 여백은 컴포넌트가 쓰는 `mb-3`/`mb-4`, `gap-3`, `px-4`/`py-3` 스케일과 통일한다.

## 1. 배치

- 위치: **목록 복사·공유 버튼(`div.flex.gap-3`) 아래, 재료 목록(`<ul>`) 바로 위.** planning의 "체크리스트 상단" 요구와 일치.
- 컨테이너: `<div className="mb-4">` (아래 `<ul>`과 간격 확보). 별도 카드/보더 없이 페이지 배경 위에 얹는다 — 목록 카드와 위계를 구분하기 위함.
- 내부 세로 배치: 진행 텍스트(1행) → 진행바(1행), `gap` 대신 텍스트에 `mb-2`.

```
[목록 복사]  [목록 공유]      ← 기존
──────────────────────────
3/6 담음                50%   ← 진행 텍스트 행
▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░       ← 진행바(트랙+채움)
──────────────────────────
[ 재료 카드 목록 <ul> ]       ← 기존
```

## 2. 구성요소

### 2-1. 진행 텍스트 행

- 레이아웃: `flex items-center justify-between mb-2`
- 좌측 라벨: `"{담은수}/{전체수} 담음"` — 클래스 `text-sm font-semibold text-gray-800`
  - 담은수 = `checked.size`, 전체수 = `ingredients.length` (planning 2절).
- 우측 퍼센트: `"{퍼센트}%"` — 클래스 `text-sm font-medium text-gray-400`
  - 퍼센트 = `Math.round(담은수 / 전체수 * 100)` (전체수 0 방어는 planning 5절, 이 경우 영역 자체 미렌더).

### 2-2. 진행바 (트랙 + 채움)

- 트랙(바깥): `<div className="h-2 w-full rounded-full bg-gray-100 overflow-hidden">`
  - 두께 `h-2`(8px), 모서리 완전 라운드 `rounded-full`, 값 0에서도 트랙이 보여 위치 인지(planning 5절).
- 채움(안쪽): `<div className="h-full rounded-full bg-rose-500 transition-all duration-300 ease-out" style={{ width: `${퍼센트}%` }}>`
  - 폭은 인라인 `width: N%` (Tailwind 임의 폭 대신 동적값이므로 style). 색 `bg-rose-500`.
  - 전환 애니메이션 `transition-all duration-300 ease-out` — planning "Could: 부드러운 전환", 과하지 않게 300ms.

## 3. 상태별 스펙

| 상태 | 조건 | 텍스트 | 진행바 채움 | 비고 |
|------|------|--------|-------------|------|
| 0% (빈 상태) | `checked.size === 0` | `0/N 담음` · `0%` (`text-gray-800`/`text-gray-400`) | 폭 `0%`, 트랙만 보임 | 채움 요소는 렌더하되 width 0 |
| 부분 | `0 < checked.size < N` | `k/N 담음` · `p%` | 폭 `p%`, `bg-rose-500` | 실시간 갱신 |
| 완료 (100%) | `checked.size === N` (N>0) | **`장보기 완료!`** — `text-sm font-semibold text-rose-500` | 폭 `100%`, `bg-rose-500` | planning "Should" 완료 강조 |
| 미렌더 | `ingredients.length === 0` | 영역 자체를 렌더하지 않음 | — | planning 5절 방어 |

- **완료 강조 방식**: 좌측 라벨 문구를 `장보기 완료!`로 교체하고 색을 `text-gray-800` → `text-rose-500`로 승격. 우측 퍼센트(`100%`)는 유지하거나 완료 시 생략 가능(frontend-dev 재량, 데모이므로 라벨 교체만으로 충분). 진행바 색은 부분 상태와 동일한 `bg-rose-500` 유지(별도 완료색 신설 안 함 — 토큰 최소화).

## 4. 접근성

- 진행바 트랙 요소에 ARIA 부여:
  - `role="progressbar"`, `aria-valuemin={0}`, `aria-valuemax={100}`, `aria-valuenow={퍼센트}`
  - `aria-label="장보기 진행률"` (또는 완료 시 값이 100임을 스크린리더가 읽음).
- 진행 텍스트는 시각 라벨이자 정보 전달원이므로 별도 `aria-hidden` 금지. 텍스트만으로도 상태 파악 가능(색에만 의존하지 않음).
- 명도 대비(WCAG AA):
  - `text-gray-800`(#1f2937) on 흰 배경 ≈ 12.6:1 (통과).
  - `text-gray-400`(#9ca3af) on 흰 배경 ≈ 2.6:1 — **보조/장식 텍스트(퍼센트)에만 사용**, 핵심 정보인 "k/N 담음"은 `gray-800`로 대비 확보.
  - `text-rose-500`(#f43f5e) on 흰 배경 ≈ 3.6:1 — 완료 라벨은 `font-semibold` 굵기와 함께 사용(대형/굵은 텍스트 AA 3:1 충족). 
  - 진행바는 그래픽 요소: 채움 `rose-500` vs 트랙 `gray-100` 인접 대비로 채움 정도 식별 가능.

## 5. 반응형

- 전 폭(`w-full`) 진행바라 모바일에서도 그대로 동작. 별도 브레이크포인트 분기 불필요.
- 텍스트 행 `justify-between`으로 좁은 화면에서도 좌(라벨)·우(퍼센트) 양끝 정렬 유지. 라벨이 짧아 줄바꿈 위험 없음.

## 6. 구현 참고 (frontend-dev)

- 파생값만 사용, 새 state/API 없음(planning 2절): `const total = ingredients.length; const done = checked.size; const percent = total ? Math.round(done / total * 100) : 0;`
- `total === 0`이면 진행률 블록 조기 반환(렌더 안 함).
- 동적 폭은 `style={{ width: `${percent}%` }}`, 나머지는 위 Tailwind 클래스 그대로.
- 삽입 지점: 복사·공유 버튼 `div` 닫힌 직후, `<ul>` 시작 전.
