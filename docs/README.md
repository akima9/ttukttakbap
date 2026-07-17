# 뚝딱밥 협업 파이프라인

역할별 서브에이전트(`.claude/agents/`)가 **공유 문서를 인계 매체로** 삼아 한 방향으로 일한다.
서브에이전트끼리는 서로를 직접 부르지 못하므로, **메인 세션이 오케스트레이터**가 되어 앞 단계 산출물을 다음 단계 입력으로 넘긴다.

## 흐름

```
기능 요청
  └─▶ web-planner   요구사항·플로우·화면정의   → docs/planning/NN-slug.md
        └─▶ web-designer  UI 스펙·토큰·컴포넌트   → docs/design/NN-slug.md
              └─▶ frontend-dev / backend-dev  구현+검증(build·lint·test)  → 코드
                    └─▶ qa-qc   검증·결함 리포트   → docs/qa/NN-slug.md
                          └─▶ 결함 있으면 해당 dev로 되돌림
                          └─▶ Blocker/Critical 0 이면 "완료(DoD)"
```

UI가 없는 순수 로직/백엔드 기능은 designer 단계를 건너뛸 수 있다.

## 역할·입출력

| 에이전트 | 입력 | 출력 | 다음 담당 |
|----------|------|------|-----------|
| `web-planner` | 기능 요청 | `docs/planning/NN-slug.md` | web-designer(UI 필요) 또는 dev |
| `web-designer` | `docs/planning/NN-slug.md` | `docs/design/NN-slug.md` | frontend-dev |
| `frontend-dev` | planning+design(같은 slug) | 코드 + build/lint | qa-qc |
| `backend-dev` | planning(같은 slug) | 코드 + build/test | qa-qc |
| `qa-qc` | 스펙 문서 + 구현 | `docs/qa/NN-slug.md` | 결함→해당 dev / 없으면 완료 |

## 인계 규칙 (traceability)

- **슬러그 공유**: 한 기능은 단계가 바뀌어도 **같은 `NN-<영문-슬러그>`** 를 쓴다.
  예) `docs/planning/12-favorites.md` ↔ `docs/design/12-favorites.md` ↔ `docs/qa/12-favorites.md`.
  `NN`은 그 기능의 번호(단계별 문서가 아니라 **기능별**로 공유).
- **문서 헤더**: 모든 산출 문서 최상단에 아래를 적는다.
  ```
  > 작성일: YYYY-MM-DD · 상태: 초안|검토|확정 · 슬러그: NN-slug · 다음 담당: <에이전트/사람>
  ```
- **인덱스 갱신**: 각 `docs/<stage>/README.md` 표에 `파일 | 내용` 한 줄을 추가/갱신.
- **되돌림 경로**: qa-qc가 결함을 열면 상태를 `검토`로 낮추고 다음 담당을 해당 dev로 지정한다.

## Definition of Done (DoD)

기능은 다음을 모두 만족해야 "완료"다.
1. planning(+필요 시 design) 문서가 `확정` 상태
2. 구현이 build·lint(FE) / build·test(BE) 통과
3. qa-qc 리포트에 **Blocker/Critical 0**
4. 커밋·배포 여부는 **사용자가 결정**(에이전트는 커밋/배포하지 않음)

## 산출물 맵

```
docs/
├─ README.md      ← (이 문서) 파이프라인 지도
├─ planning/      ← web-planner : 요구사항·플로우·화면정의
├─ design/        ← web-designer: 디자인 시스템·컴포넌트 스펙
└─ qa/            ← qa-qc       : 테스트 플랜·버그 리포트
```
