# 뚝딱밥 🍳

요리가 어렵게 느껴지는 사람들을 위해 **장보기부터 레시피까지 한 번에** 안내하는 서비스입니다.
식사 인원을 설정하면 메뉴를 추천하고, 인원 수에 맞춰 필요한 재료와 장보기 정보, 단계별 레시피를 제공합니다.

`ttukttak-bap`은 `frontend`와 `backend`로 구성된 모노레포입니다.

## 주요 기능

- **메뉴 추천**: 카테고리·냉장고 보유 재료 기반 메뉴 추천
- **인원 기반 재료 계산**: `1인분 기준량 × 인원 수`로 필요한 재료 양 계산 + 장보기 정보 안내
- **단계별 레시피**: 순서·팁 포함 조리 안내
- **카카오 로그인**: OAuth 로그인 + JWT(access/refresh) 인증
- **개인화**: 즐겨찾기, 최근 본 메뉴, 내 냉장고 관리 및 보유 재료 기반 추천
- **관리자**: 메뉴/재료/레시피 CRUD

## 기술 스택

| 영역 | 스택 |
|------|------|
| 프론트엔드 | Next.js 16 (App Router) · React 19 · TypeScript 5 · Tailwind CSS 4 |
| 백엔드 | Spring Boot 3.5 · Kotlin 1.9 · Spring Data JPA · Spring Security · JWT |
| 데이터 | MySQL · Redis (refresh token 저장) |
| 빌드/런타임 | Gradle (Kotlin DSL) · JDK 17 |

## 프로젝트 구조

```
ttukttak-bap/
├── frontend/   # Next.js 앱 (src/app 기반 라우팅)
└── backend/    # Spring Boot API 서버 (com.ttukttakbap.backend)
```

## 시작하기

### 사전 요구사항

- JDK 17, Node.js 20+, MySQL, Redis
- 카카오 개발자 콘솔 앱 (REST API 키)

### 백엔드 (`/backend`)

```bash
cd backend
cp .env.example .env          # DB_PASSWORD, JWT_SECRET, KAKAO_CLIENT_ID/SECRET 채우기
set -a && source .env && set +a
./gradlew bootRun             # http://localhost:8080
```

기타 명령:

```bash
./gradlew build   # 컴파일 및 패키징
./gradlew test    # 전체 테스트
```

### 프론트엔드 (`/frontend`)

```bash
cd frontend
cp .env.local.example .env.local   # NEXT_PUBLIC_KAKAO_CLIENT_ID 채우기
npm install
npm run dev                        # http://localhost:3000
```

> 카카오 콘솔의 Redirect URI 에 `http://localhost:3000/login/kakao/callback` 을 등록해야 로그인이 동작합니다.

## API 개요

Base URL: `/api/v1` · 응답 형식: JSON · 인증: 카카오 로그인 후 `Authorization: Bearer <accessToken>`

**공개 (인증 불필요)**

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/menus` | 전체 메뉴 목록 |
| GET | `/menus/recommend` | 메뉴 추천 (카테고리·냉장고 재료 기반) |
| GET | `/menus/{menuId}` | 메뉴 상세 |
| GET | `/menus/{menuId}/ingredients?people={n}` | 인원 수 기반 재료 목록 |
| GET | `/menus/{menuId}/recipe` | 단계별 레시피 |
| GET | `/categories` | 메뉴 카테고리 목록 |
| GET | `/ingredients` | 재료 목록 (냉장고 담기용) |
| POST | `/auth/login/{provider}` | 소셜 로그인 (code ↔ JWT 교환) |
| POST | `/auth/refresh` | access token 재발급 |

**인증 필요 (`/me/*`)**

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/auth/me` | 내 정보 |
| POST | `/auth/logout` | 로그아웃 |
| GET · POST · DELETE | `/me/favorites` · `/me/favorites/{menuId}` | 즐겨찾기 조회/추가/삭제 |
| GET · POST | `/me/history` · `/me/history/{menuId}` | 최근 본 메뉴 조회/기록 |
| GET · POST · DELETE | `/me/fridge` · `/me/fridge/{ingredientId}` | 내 냉장고 조회/추가/삭제 |

**관리자 (`/admin/*`)**: 메뉴·재료·레시피·메뉴재료 CRUD

**공통 에러 응답**

```json
{ "status": 404, "message": "해당 메뉴를 찾을 수 없습니다.", "timestamp": "2026-05-02T12:00:00" }
```
