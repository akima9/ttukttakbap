# 장보기 진행률 표시 — QA 검증 리포트

> 작성일: 2026-07-17 · 상태: 확정 · 슬러그: 01-shopping-progress · 다음 담당: 없음(완료/DoD) · 대상 버전: 워킹트리 미커밋(base `07c9c09`, `M frontend/src/app/ingredients/IngredientsChecklist.tsx`)

재료 체크리스트 상단 진행률 UI(`frontend/src/app/ingredients/IngredientsChecklist.tsx`)의 독립 검증 결과.
스펙: [`../planning/01-shopping-progress.md`](../planning/01-shopping-progress.md), [`../design/01-shopping-progress.md`](../design/01-shopping-progress.md).

## 판정 요약

- **결과: PASS** — Blocker 0 · Critical 0 · Major 0 · Minor 1(기능 무관, 사전 존재하는 lint 경고).
- 수용 기준 5개 전부 충족, 디자인 상태 스펙 전부 일치, ARIA progressbar 정상 렌더. DoD 3항(Blocker/Critical 0) 충족.

## 검증 환경

- 정적: `frontend`에서 `npm run lint`, `npm run build`.
- 동작: `API_INTERNAL_URL`/`NEXT_PUBLIC_API_BASE_URL` = 라이브(`https://ttukttakbap.duckdns.org/api/v1`)로 `next dev`(포트 3987, 백그라운드) 기동 후 `/ingredients?people=2&menuId=1` HTML을 curl로 수신하여 마크업 확인. 확인 후 dev 서버 종료·포트 해제 확인.
- 라이브 메뉴 1(된장찌개)은 재료 6개 반환 → 초기 상태 `0/6` 케이스로 검증.
- 인터랙션(체크/해제에 따른 실시간 갱신·완료 전환)은 클라이언트 React state이므로 서버 HTML로는 직접 관측 불가 → 파생값 로직을 코드 대조로 검증(아래 표 표기).

## 정적 검증 결과

| 항목 | 결과 |
|------|------|
| `npm run lint` | 0 errors, **1 warning** — `IngredientsChecklist.tsx:29` `@typescript-eslint/no-unused-expressions`(아래 QA-01). |
| `npm run build` | **성공**. Next.js 16.2.4, Compiled successfully, TypeScript 통과, 15/15 페이지 생성. `/ingredients`는 동적(ƒ) 라우트로 정상. |

## 수용 기준 점검 (planning §6)

| # | 기준 | 결과 | 근거 |
|---|------|------|------|
| 1 | 6개 중 3개 → "3/6 담음" + 50% 바 | PASS | `done=checked.size`, `percent=Math.round(done/total*100)`, 라벨 `${done}/${total} 담음`. 3/6→50% (코드 대조). |
| 2 | 체크/해제 시 즉시 갱신 | PASS | `done`·`percent`·`isComplete` 모두 `checked` state 파생 → 토글 시 재렌더. |
| 3 | 전체 체크 시 완료 상태 강조 | PASS | `isComplete = total>0 && done===total` → 라벨 `장보기 완료!` + `text-rose-500`. |
| 4 | 재료 0개면 진행률 영역 미렌더 | PASS | `{total > 0 && ( … )}` 가드로 블록 자체 조건부 렌더. |
| 5 | 복사·공유·레시피 이동 무영향 | PASS | 진행률 블록을 버튼 `div`와 `<ul>` 사이에 삽입, 기존 핸들러·요소 미변경. 라이브 HTML에서 복사/공유 버튼·레시피 링크 정상 렌더 확인. |

## 디자인 상태 스펙 점검 (design §3·§4)

| 상태/항목 | 스펙 | 결과 | 관측 |
|-----------|------|------|------|
| 0%(빈 상태) | 라벨 `0/N 담음`(gray-800), 퍼센트 `0%`(gray-400), 채움 width 0·트랙 보임 | PASS | 라이브 HTML: `text-gray-800">0/6 담음`, `text-gray-400">0%`, 채움 `style="width:0%"`, 트랙 `bg-gray-100`. |
| 부분 | 라벨 gray-800, 채움 `bg-rose-500` | PASS | 클래스 `bg-rose-500 transition-all duration-300 ease-out`, width `${percent}%`(코드 대조). |
| 완료(100%) | 라벨 `장보기 완료!` `text-rose-500 font-semibold`, 퍼센트 100% 유지 | PASS | `isComplete` 분기 일치. 퍼센트는 유지(스펙상 재량 허용). |
| 미렌더 | `ingredients.length===0` 시 블록 없음 | PASS | `total > 0` 가드(코드 대조). |
| ARIA progressbar | `role=progressbar` + `aria-valuemin/max/now` + `aria-label` | PASS | 라이브 HTML: `role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0" aria-label="장보기 진행률"`. |
| 진행 텍스트 비장식 | `aria-hidden` 금지, 색 외 텍스트로도 상태 전달 | PASS | 텍스트 노드에 `aria-hidden` 없음, `k/N 담음`·`장보기 완료!` 문구로 전달. |
| 배치 | 복사·공유 `div` 아래, `<ul>` 위, `mb-4` 컨테이너 | PASS | 삽입 위치·`<div className="mb-4">` 일치. |
| 대비(AA) | 핵심정보 gray-800, 보조 퍼센트 gray-400, 완료 rose-500+굵기 | PASS | 스펙 클래스 그대로 적용(신규 대비 위반 없음). |
| 반응형 | `w-full` 바 + `justify-between`, 브레이크포인트 분기 불필요 | PASS | 클래스 일치, 모바일 폭 분기 없음. |

## 결함 리포트

### QA-01 · Minor · lint 경고 `no-unused-expressions` (기능 무관·사전 존재)

- **심각도**: Minor (기능 정상, 스타일 경고). 진행률 기능 코드가 아니며 base 커밋에도 존재하는 기존 `toggle()` 구현부.
- **환경**: 정적(`npm run lint`), `frontend/src/app/ingredients/IngredientsChecklist.tsx:29`.
- **재현**: `cd frontend && npm run lint`.
- **기대**: 경고 0.
- **실제**: `29:7 warning Expected an assignment or function call and instead saw an expression @typescript-eslint/no-unused-expressions`.
- **추정 원인**: `next.has(id) ? next.delete(id) : next.add(id)` — 삼항을 부수효과(side-effect) 용도로 사용. `if/else`나 명시적 호출로 바꾸면 해소.
- **제안(→ frontend-dev, 선택)**: 기능·빌드에 영향 없어 이번 기능 완료를 막지 않음. 정리 시 `if (next.has(id)) next.delete(id); else next.add(id)` 형태 권장. 진행률 기능과 무관하므로 별도 정리 항목으로 분리 가능.

## 미검증/한계

- 체크/해제에 따른 실시간 갱신·완료 전환·0개 미렌더는 **클라이언트 상호작용**이라 curl(서버 HTML)로는 직접 관측하지 못함. 파생값 로직·조건부 렌더를 코드 대조로 확인했으며 초기 `0/6`·`0%`·`width:0%`·progressbar 마크업은 라이브 렌더로 실측함.
- 라이브 API에 재료 0개인 메뉴가 없어 미렌더 케이스는 코드 가드(`total > 0 &&`)로만 검증.
