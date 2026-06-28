# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

## 5. 세션로그 (작업 완료 시 필수)

하나의 **작업/기능 단위**가 끝나 사용자에게 "완료"를 보고할 때마다 `session-log` 스킬을 호출해 세션로그를 남긴다. 사용자가 매번 요청하지 않아도 자동으로 남긴다. 형식·위치는 스킬 정의를 따른다.

## 프로젝트 개요

요리가 어렵게 느껴지는 사람들을 위해 **장보기부터 레시피까지 한 번에** 안내하는 서비스.
식사 인원을 설정하면 메뉴를 추천하고, 필요한 재료와 장보기 정보, 단계별 레시피를 제공한다.

`ttukttak-bap`은 `/frontend`와 `/backend`로 구성된 모노레포입니다.

- **프론트엔드**: Next.js 16.2.4 (App Router) + React 19 + TypeScript 5 + Tailwind CSS 4
- **백엔드**: Spring Boot 3.5 (SNAPSHOT) + Kotlin 1.9.25 + Spring Data JPA + MySQL

## 명령어

### 프론트엔드 (`/frontend` 디렉토리에서 실행)

```bash
npm run dev      # 개발 서버 시작 (포트 3000)
npm run build    # 프로덕션 빌드
npm run lint     # ESLint 실행
```

### 백엔드 (`/backend` 디렉토리에서 실행)

```bash
./gradlew bootRun                                              # 애플리케이션 실행
./gradlew build                                                # 컴파일 및 패키징
./gradlew test                                                 # 전체 테스트 실행
./gradlew test --tests "com.ttukttakbap.backend.ClassName"    # 특정 테스트 실행
```

## 아키텍처

### 페이지 구조 (프론트엔드)

| 경로 | 역할 |
|------|------|
| `src/app/page.tsx` | 메인 — 식사 인원 설정 |
| `src/app/menu/page.tsx` | 메뉴 추천 및 선택 |
| `src/app/ingredients/page.tsx` | 재료 및 장보기 정보 |
| `src/app/recipe/page.tsx` | 단계별 레시피 상세 |

- **라우팅**: Next.js App Router — 모든 페이지/레이아웃은 `src/app/` 하위 파일 시스템 구조를 따름
- **경로 별칭**: `@/*`는 `./src/*`로 매핑됨 (`tsconfig.json`)
- **주의**: Next.js 16은 이전 버전과 비교해 **파괴적 변경(breaking changes)**이 있음 — 코드 작성 전 반드시 `node_modules/next/dist/docs/`의 관련 가이드를 확인할 것

### 패키지 구조 (백엔드)

```
com.ttukttakbap.backend/
├── menu/
├── ingredient/
├── recipe/
└── common/
```

- **JPA 엔티티**: Kotlin all-open 플러그인이 `@Entity`, `@MappedSuperclass`, `@Embeddable`에 대해 자동 적용되도록 `build.gradle.kts`에 이미 설정됨
- **의존성 저장소**: Spring Boot 3.5 SNAPSHOT은 `https://repo.spring.io/snapshot`에서 받아옴
- **데이터베이스**: 실행 전 `application.properties`에 데이터소스 설정(`spring.datasource.*`)을 추가해야 함

### DB 설계

```
menu ──< menu_ingredient >── ingredient
menu ──< recipe
```

**menu**

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 메뉴 ID |
| name | VARCHAR | 메뉴 이름 |
| description | TEXT | 메뉴 설명 |
| image_url | VARCHAR | 메뉴 이미지 |
| cook_time_minutes | INT | 조리 시간 (분) |
| difficulty | ENUM | 난이도 (EASY / MEDIUM / HARD) |

**ingredient**

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 재료 ID |
| name | VARCHAR | 재료 이름 |
| purchase_unit | VARCHAR | 구매 단위 (예: 1포기, 500g) |
| purchase_location | VARCHAR | 장보기 추천 |

**menu_ingredient**

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | ID |
| menu_id | BIGINT (FK) | 메뉴 ID |
| ingredient_id | BIGINT (FK) | 재료 ID |
| amount_per_person | DECIMAL | 1인분 기준 양 |
| unit | VARCHAR | 단위 (예: g, ml, 개) |

인원 수 × `amount_per_person`으로 필요한 재료 양을 계산한다.

**recipe**

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 레시피 ID |
| menu_id | BIGINT (FK) | 메뉴 ID |
| step_order | INT | 순서 |
| description | TEXT | 조리 설명 |
| tip | TEXT | 팁 (선택) |

### API 설계

Base URL: `/api/v1` | 응답 형식: JSON | 인증: 없음 (v1 기준)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/menus` | 전체 메뉴 목록 조회 |
| GET | `/api/v1/menus/{menuId}` | 메뉴 단건 상세 조회 |
| GET | `/api/v1/menus/{menuId}/ingredients?people={people}` | 인원 수 기반 재료 목록 (`people` 기본값: 2) |
| GET | `/api/v1/menus/{menuId}/recipe` | 단계별 레시피 조회 |

**공통 에러 응답 형식**
```json
{ "status": 404, "message": "해당 메뉴를 찾을 수 없습니다.", "timestamp": "2026-05-02T12:00:00" }
```

에러 코드: `400` 잘못된 요청 / `404` 리소스 없음 / `500` 서버 오류
