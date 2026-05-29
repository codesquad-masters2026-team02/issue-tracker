# Architecture

## 시스템 아키텍처

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────┐
│   Browser   │────▶│  CloudFront CDN  │────▶│  S3 (Static) │
│  (React SPA)│     └──────────────────┘     └──────────────┘
│             │
│             │     ┌──────────────────┐     ┌──────────────┐
│             │────▶│   Nginx (EC2)    │────▶│  Spring Boot │
└─────────────┘     │  Reverse Proxy   │     │   :8080      │
                    └──────────────────┘     └──────┬───────┘
                                                   │
                                            ┌──────▼───────┐
                                            │    MySQL      │
                                            └──────────────┘
                                            ┌──────────────┐
                                            │   AWS S3     │
                                            │ (첨부파일)    │
                                            └──────────────┘
```

### 요청 흐름

1. 사용자가 브라우저에서 SPA에 접근 (CloudFront + S3에서 정적 파일 서빙)
2. SPA가 API 요청을 백엔드 서버로 전송
3. Nginx가 리버스 프록시로 `127.0.0.1:8080`의 Spring Boot 컨테이너에 전달
4. Spring Boot가 MySQL과 통신하여 데이터 처리
5. 파일 첨부 시 Presigned URL을 발급하여 클라이언트가 S3에 직접 업로드

## 모노레포 구조

```
issue-tracker/
├── apps/
│   ├── spring-api/          # Spring Boot 백엔드
│   └── web-frontend/        # React 프론트엔드
├── packages/
│   └── api-spec/            # OpenAPI 코드 생성기
├── pnpm-workspace.yaml
├── package.json
└── pnpm-lock.yaml
```

pnpm workspace로 관리되며, `packages/api-spec`은 백엔드의 OpenAPI 스펙에서 TypeScript 타입과 React Query 훅을 자동 생성하는 패키지입니다.

### API Spec 생성 흐름

```
Spring Boot (Swagger)
    │
    ▼  ./gradlew generateOpenApiDocs
openapi.yaml (packages/api-spec/)
    │
    ▼  orval (코드 생성)
TypeScript Types + React Query Hooks
    │
    ▼  workspace dependency
web-frontend에서 import
```

실행 명령: `pnpm api:spec`

## 기술 스택

### Backend

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 | 언어 |
| Spring Boot | 3.5.14 | 웹 프레임워크 |
| Spring Data JDBC | - | 데이터 접근 |
| Flyway | - | DB 마이그레이션 |
| JJWT | 0.12.6 | JWT 인증 |
| bcrypt | 0.10.2 | 비밀번호 해싱 |
| SpringDoc OpenAPI | 2.7.0 | API 문서 자동 생성 |
| AWS SDK v2 (S3) | 2.25.0 | 파일 업로드 |
| Lombok | - | 보일러플레이트 제거 |
| H2 | - | 로컬 개발용 인메모리 DB |
| MySQL | - | 프로덕션 DB |

### Frontend

| 기술 | 버전 | 용도 |
|------|------|------|
| React | 18.3 | UI 라이브러리 |
| TypeScript | 5.6 | 타입 안전성 |
| Vite | 5.4 | 빌드 도구 |
| React Router DOM | 6.27 | 라우팅 |
| TanStack React Query | 5.59 | 서버 상태 관리 |
| Axios | 1.7 | HTTP 클라이언트 |
| react-markdown | 10.1 | 마크다운 렌더링 |

### Infra

| 기술 | 용도 |
|------|------|
| Docker | 백엔드 컨테이너화 |
| GitHub Actions | CI/CD |
| AWS S3 + CloudFront | 프론트엔드 호스팅 |
| AWS EC2 (ARM64) | 백엔드 호스팅 |
| Nginx | 리버스 프록시 |
| AWS S3 | 첨부파일 저장소 |

## 주요 도메인 모듈 (Backend)

```
com.codesquad.issueTracker/
├── issue/           # 이슈 CRUD, 필터링, 페이징
├── comment/         # 댓글 CRUD
├── label/           # 라벨 CRUD
├── milestone/       # 마일스톤 CRUD
├── user/            # 회원가입, 로그인, 프로필
├── attachment/      # 파일 첨부 (S3 Presigned URL)
├── auth/            # GitHub OAuth
├── common/          # CORS, 인터셉터, 예외처리, 공통 응답
└── security/        # JWT, 비밀번호 헬퍼
```

## 프론트엔드 라우팅 구조

| 경로 | 페이지 | 인증 |
|------|--------|------|
| `/login` | 로그인 페이지 | 불필요 |
| `/oauth/github/callback` | GitHub OAuth 콜백 | 불필요 |
| `/` | 이슈 목록 | 필요 |
| `/issues/new` | 이슈 생성 | 필요 |
| `/issues/:id` | 이슈 상세 | 필요 |
| `/labels` | 라벨 관리 | 필요 |
| `/milestones` | 마일스톤 관리 | 필요 |
