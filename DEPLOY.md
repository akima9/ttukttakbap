# 배포 가이드 — Oracle Cloud 평생무료 VM ($0)

뚝딱밥을 **Oracle Cloud Always Free VM 한 대**에 Docker Compose로 전부 올리는 절차입니다.
프론트·백엔드·MySQL·Redis·Caddy(자동 HTTPS)를 한 서버에서 돌리며, 월 비용 $0입니다.

## 구조
```
브라우저 ──HTTPS──> Caddy(443) ─┬─ /api/*  → backend:8080
                                └─ /*      → frontend:3000
backend ──> mysql / redis        (전부 compose 내부망, Caddy만 외부 공개)
```
- 브라우저는 `https://<도메인>/api/v1/...`(같은 출처)로 호출 → **CORS 불필요 + HTTPS 확보**(재료 목록 공유 기능 동작).
- Caddy가 Let's Encrypt 인증서를 자동 발급/갱신합니다.

## 준비물
- Oracle Cloud 계정(가입 시 카드 인증, 과금은 없음 — Always Free 범위만 사용)
- 카카오 개발자 계정
- 이 저장소

---

## 1. Oracle Cloud VM 생성
1. [Oracle Cloud](https://cloud.oracle.com) 가입 → 콘솔 → **Compute → Instances → Create instance**.
2. **Image**: Ubuntu 22.04. **Shape**: `VM.Standard.A1.Flex`(ARM Ampere, Always Free) — OCPU 2~4, RAM 6~24GB 권장.
   - ARM 재고가 없으면 `VM.Standard.E2.1.Micro`(AMD, 1GB) 2대 중 1대로 폴백(단 메모리 부족 대비 필요 — 아래 트러블슈팅 참고).
3. **SSH key**: 로컬 공개키 등록(없으면 `ssh-keygen`으로 생성).
4. 생성 후 **Public IP** 기록.
5. **인그레스 포트 개방** — VM이 속한 서브넷의 **Security List → Add Ingress Rules**:
   - Source `0.0.0.0/0`, IP Protocol TCP, Destination Port **80**
   - Source `0.0.0.0/0`, IP Protocol TCP, Destination Port **443**

## 2. 무료 도메인 (DuckDNS)
카카오 로그인·HTTPS에는 호스트네임이 필요합니다(공인 IP만으로는 인증서 발급 불가).
1. [duckdns.org](https://www.duckdns.org) 로그인 → 서브도메인 생성(예: `ttukttakbap`).
2. **current ip** 칸에 VM Public IP 입력 → update. 이제 `ttukttakbap.duckdns.org` → VM IP.
3. (선택) IP가 바뀔 일은 없지만, 갱신 크론을 걸어두면 안전합니다.

## 3. VM 초기 설정
SSH 접속: `ssh ubuntu@<VM_PUBLIC_IP>`

```bash
# OS 방화벽(Oracle Ubuntu는 iptables가 기본 차단) — 80/443 허용
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save

# Docker 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker   # 또는 재로그인

# 저장소 클론
git clone <이 저장소 URL> ttukttak-bap
cd ttukttak-bap
```

## 4. 카카오 콘솔 등록
[Kakao Developers](https://developers.kakao.com) → 내 애플리케이션 → 앱 생성 후:
1. **플랫폼 → Web** → 사이트 도메인 `https://ttukttakbap.duckdns.org` 등록.
2. **카카오 로그인 → 활성화 ON**.
3. **카카오 로그인 → Redirect URI**: `https://ttukttakbap.duckdns.org/login/kakao/callback` 등록.
   (프론트가 `window.location.origin` 기반으로 콜백을 만들어 자동 일치합니다.)
4. **동의항목**: 닉네임(`profile_nickname`) 필수 동의.
5. **앱 키 → REST API 키** 복사 → 아래 `.env`의 `KAKAO_CLIENT_ID`.
   - (선택) 보안 → Client Secret 사용 시 `KAKAO_CLIENT_SECRET`에도 입력.

## 5. 환경변수 작성 & 배포
```bash
cp .env.example .env
nano .env   # 아래 값 채우기
```

`.env` 핵심 값:
| 키 | 값 |
|----|----|
| `DOMAIN` | `ttukttakbap.duckdns.org` (스킴 없이) |
| `DB_PASSWORD` / `MYSQL_ROOT_PASSWORD` | `openssl rand -base64 24` 로 생성 |
| `JWT_SECRET` | `openssl rand -base64 48` (32바이트 이상) |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | 관리자 로그인용(꼭 변경) |
| `CORS_ALLOWED_ORIGINS` | `https://ttukttakbap.duckdns.org` |

> ⚠️ `KAKAO_CLIENT_ID`는 프론트 이미지에 **빌드 타임에 구워집니다**(`NEXT_PUBLIC_KAKAO_CLIENT_ID`). 반드시 빌드 전에 채워두고, 나중에 바꾸면 프론트 이미지를 다시 빌드해야 합니다.

빌드 & 기동:
```bash
docker compose up -d --build
```
Caddy가 자동으로 TLS 인증서를 발급합니다(수십 초). 진행 확인:
```bash
docker compose ps
docker compose logs -f backend   # "Started BackendApplication" 뜨면 기동 완료
```

## 6. 시드 데이터 넣기 (최초 1회)
백엔드가 처음 기동하면서 Hibernate가 테이블을 생성합니다(`ddl-auto=update`). **테이블이 만들어진 뒤** 시드를 import 하세요:
```bash
# backend/db/dummy-data.sql = 메뉴/재료/레시피 시드(INSERT)
docker compose exec -T mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ttukttakbap' < backend/db/dummy-data.sql
```
> 시드 없이 시작하고 싶으면 이 단계를 건너뛰고 `https://<도메인>/admin/login`(위 ADMIN 계정)에서 메뉴를 직접 등록해도 됩니다.

## 7. 검증
```bash
curl -s https://ttukttakbap.duckdns.org/api/v1/menus | head
```
브라우저에서 `https://ttukttakbap.duckdns.org` 접속:
- [ ] 홈 → 메뉴 목록 → 상세(재료) → 레시피 렌더
- [ ] 카카오 로그인 → 헤더 아바타(닉네임) 표시
- [ ] 즐겨찾기 토글, `/fridge` 냉장고 담기, `/my` 조회
- [ ] 재료 페이지 "목록 공유"가 공유 시트를 띄움(HTTPS라 동작)
- [ ] `/admin/login` HTTP Basic 로그인 → 콘텐츠 관리

---

## 운영
### 재배포
```bash
git pull
docker compose up -d --build
```
- 프론트 관련 `NEXT_PUBLIC_*`(카카오 키 등)를 바꿨다면 프론트만 재빌드: `docker compose up -d --build frontend`

### 로그 / 재시작
```bash
docker compose logs -f backend        # 로그
docker compose restart backend        # 재시작(로그인은 Redis라 유지됨)
```

### DB 백업
```bash
docker compose exec -T mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" ttukttakbap' > backup-$(date +%F).sql
```
MySQL 데이터는 `mysql_data` 볼륨에 영속됩니다. VM 스냅샷도 주기적으로 떠두면 안전합니다.

---

## 트러블슈팅
| 증상 | 원인 / 해결 |
|------|-------------|
| HTTPS 인증서 발급 실패 | 80/443이 막힘 → Oracle Security List **및** OS iptables 둘 다 확인. DNS 전파 전이면 잠시 대기 |
| 사이트 502 | 백엔드가 아직 기동 중이거나 MySQL 헬스체크 대기 → `docker compose logs backend`, `docker compose ps` 확인 |
| 카카오 로그인 KOE006 | Redirect URI 불일치 → 카카오 콘솔에 `https://<도메인>/login/kakao/callback` 정확히 등록됐는지 |
| 로그인 시 카카오 키 오류 | `KAKAO_CLIENT_ID` 미설정 상태로 프론트가 빌드됨 → `.env` 채운 뒤 `docker compose up -d --build frontend` |
| 재시작하면 로그아웃됨 | `REFRESH_TOKEN_STORE=redis`인지 확인(기본 `.env.example`에 설정됨) |
| ARM VM 재고 없음(AMD 1GB로 폴백) | `backend` 서비스에 `JAVA_TOOL_OPTIONS=-Xmx256m` 환경변수 추가로 메모리 절감. MySQL도 `--innodb-buffer-pool-size=64M` 옵션 고려 |

## 후속 과제
- CI/CD(GitHub Actions로 push 시 자동 배포)
- Flyway 스키마 마이그레이션(현재 `ddl-auto=update`)
- 구글/네이버 로그인 추가
