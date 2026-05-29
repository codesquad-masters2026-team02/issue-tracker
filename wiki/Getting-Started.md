# Getting Started

로컬 개발 환경 세팅 가이드입니다.

## 사전 요구 사항

| 도구 | 버전 | 비고 |
|------|------|------|
| Node.js | >= 22.18.0 | |
| pnpm | 9.12.0 | `corepack enable && corepack prepare pnpm@9.12.0 --activate` |
| Java | 21 | JDK (Eclipse Temurin 권장) |
| Docker | 최신 | 배포 테스트 시 필요 |

## 프로젝트 클론

```bash
git clone https://github.com/codesquad-masters2026-team02/issue-tracker.git
cd issue-tracker
```

## 프론트엔드 실행

```bash
# 의존성 설치 (루트에서)
pnpm install

# 개발 서버 실행
cd apps/web-frontend
pnpm dev
```

`http://localhost:5173`에서 접근할 수 있습니다.

### 환경 변수

`apps/web-frontend/.env` 파일을 생성합니다:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GITHUB_CLIENT_ID=<GitHub OAuth App Client ID>
VITE_GITHUB_REDIRECT_URI=http://localhost:5173/oauth/github/callback
```

## 백엔드 실행

```bash
cd apps/spring-api

# Gradle Wrapper로 실행 (local 프로필이 기본)
./gradlew bootRun
```

`http://localhost:8080`에서 API 서버가 시작됩니다.

- **로컬 프로필**: H2 인메모리 DB를 사용하므로 별도 DB 설치 불필요
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### 프로덕션 프로필로 실행 시

MySQL이 필요하며, 아래 환경 변수를 설정해야 합니다:

```env
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<MySQL 호스트>
DB_USERNAME=<DB 사용자명>
DB_PASSWORD=<DB 비밀번호>
JWT_SECRET_KEY=<JWT 시크릿 키>
CORS_ALLOWED_ORIGINS=<허용할 Origin>
AWS_ACCESS_KEY_ID=<AWS 액세스 키>
AWS_SECRET_ACCESS_KEY=<AWS 시크릿 키>
GITHUB_CLIENT_ID=<GitHub OAuth Client ID>
GITHUB_CLIENT_SECRET=<GitHub OAuth Client Secret>
```

## API Spec 생성

백엔드 서버가 실행 중인 상태에서:

```bash
# 루트 디렉토리에서
pnpm api:spec
```

이 명령은 다음을 수행합니다:
1. Spring Boot 앱에서 OpenAPI 스펙을 추출 → `packages/api-spec/openapi.yaml`
2. orval로 TypeScript 타입 + React Query 훅 생성 → `packages/api-spec/src/generated/`

## Docker로 백엔드 실행

```bash
cd apps/spring-api
docker compose up --build
```

> **참고**: `docker-compose.yml`은 `SPRING_PROFILES_ACTIVE=prod`만 설정되어 있으므로, 나머지 환경 변수는 별도로 주입해야 합니다.

## 자주 쓰는 명령어

| 명령어 | 설명 |
|--------|------|
| `pnpm install` | 전체 의존성 설치 |
| `pnpm api:spec` | OpenAPI 스펙에서 타입 생성 |
| `cd apps/web-frontend && pnpm dev` | 프론트엔드 개발 서버 |
| `cd apps/spring-api && ./gradlew bootRun` | 백엔드 개발 서버 |
| `cd apps/spring-api && ./gradlew test` | 백엔드 테스트 |
| `cd apps/spring-api && ./gradlew bootJar` | 백엔드 JAR 빌드 |
