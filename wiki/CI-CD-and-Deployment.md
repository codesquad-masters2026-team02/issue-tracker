# CI/CD & Deployment

## 개요

| 구분 | 트리거 | 빌드 | 배포 대상 |
|------|--------|------|-----------|
| Backend | `develop` push + `apps/spring-api/**` 변경 | Docker 이미지 (ARM64) | EC2 (Docker) |
| Frontend | `develop` push + `apps/web-frontend/**` 변경 | Vite 정적 빌드 | S3 + CloudFront |

두 워크플로우 모두 `workflow_dispatch`를 지원하여 수동 실행이 가능합니다.

## Backend 배포 파이프라인

**파일**: `.github/workflows/backend_deploy.yml`

```
develop push (apps/spring-api/** 변경)
    │
    ▼
GitHub Actions (ubuntu-24.04-arm)
    │
    ├─ Checkout
    ├─ Docker Buildx 설정
    ├─ Docker Hub 로그인
    ├─ Docker 이미지 빌드 & 푸시 (linux/arm64)
    │   └─ 이미지: {DOCKER_USERNAME}/isssue_tracker:latest
    │
    ▼
SSH로 원격 서버 접속
    ├─ docker pull (최신 이미지)
    ├─ 기존 컨테이너 중지 & 제거
    └─ docker run (환경변수 주입)
        └─ 127.0.0.1:8080 → Nginx 리버스 프록시
```

### Dockerfile (`apps/spring-api/Dockerfile.api`)

- **빌드 스테이지**: `eclipse-temurin:21-jdk-alpine`
  - Gradle 의존성 캐싱 → 소스 복사 → `bootJar` 빌드
- **런타임 스테이지**: `eclipse-temurin:21-jre-alpine`
  - 비-root 사용자 (`spring:spring`)로 실행
  - 포트 8080 노출

### 배포 시 주입되는 환경변수

| 변수 | 출처 |
|------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` 고정 |
| `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD` | GitHub Secrets |
| `JWT_SECRET_KEY` | GitHub Secrets |
| `CORS_ALLOWED_ORIGINS` | GitHub Secrets |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` | GitHub Secrets |
| `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` | GitHub Secrets |

## Frontend 배포 파이프라인

**파일**: `.github/workflows/deploy-frontend.yml`

```
develop push (apps/web-frontend/** 변경)
    │
    ▼
GitHub Actions (ubuntu-latest)
    │
    ├─ Checkout
    ├─ pnpm 9.12.0 + Node 22.18.0 설정
    ├─ pnpm install --frozen-lockfile
    ├─ pnpm --filter @issue-tracker/web-frontend build
    │
    ▼
AWS OIDC 인증 (임시 자격 증명)
    │
    ├─ S3 sync (해시된 자산: immutable 캐시, 1년)
    │   └─ index.html, *.map 제외
    ├─ S3 cp index.html (no-cache)
    ├─ CloudFront 캐시 무효화 (/ 및 /index.html만)
    │
    ▼
GitHub Deployment 상태 기록
```

### 캐시 전략

| 리소스 | Cache-Control | 이유 |
|--------|---------------|------|
| JS/CSS/이미지 (해시 파일명) | `public, max-age=31536000, immutable` | 파일명에 해시 포함 → 영구 캐시 |
| `index.html` | `no-cache, no-store, must-revalidate` | 항상 최신 버전 로드 |

### 빌드 시 주입되는 환경변수

| 변수 | 출처 |
|------|------|
| `VITE_API_BASE_URL` | GitHub Secrets |
| `VITE_GITHUB_CLIENT_ID` | GitHub Variables |
| `VITE_GITHUB_REDIRECT_URI` | GitHub Variables |

### 동시성 제어

프론트엔드 워크플로우는 `concurrency` 설정으로 같은 브랜치에서 중복 배포를 방지합니다:
- 동일 그룹의 실행 중인 워크플로우가 있으면 취소 후 새 워크플로우 실행

## 인프라 구성

```
┌─────────────────────────────────────────────────────┐
│                    AWS Cloud                         │
│                                                      │
│  ┌────────────┐    ┌────────────┐                    │
│  │ CloudFront │───▶│  S3 Bucket │  ← 프론트엔드     │
│  └────────────┘    └────────────┘                    │
│                                                      │
│  ┌────────────────────────────────┐                  │
│  │          EC2 (ARM64)           │                  │
│  │  ┌───────┐    ┌─────────────┐ │  ┌────────────┐  │
│  │  │ Nginx │───▶│ Spring Boot │─┼─▶│   MySQL    │  │
│  │  │  :80  │    │ (Docker)    │ │  └────────────┘  │
│  │  └───────┘    │   :8080     │ │                  │
│  │               └─────────────┘ │  ┌────────────┐  │
│  └────────────────────────────────┘  │  S3 Bucket │  │
│                                      │ (첨부파일)  │  │
│                                      └────────────┘  │
└─────────────────────────────────────────────────────┘
```
