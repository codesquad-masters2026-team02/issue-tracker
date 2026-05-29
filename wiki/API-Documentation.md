# API Documentation

## 개요

- **Base URL**: `http://localhost:8080` (로컬) / 프로덕션 도메인
- **Swagger UI**: `{Base URL}/swagger-ui.html`
- **OpenAPI Spec**: `{Base URL}/v3/api-docs`
- **공통 응답 형식**: `ApiResponse<T>` 래퍼

## 인증 (Authentication)

### JWT 기반 인증

- **Access Token**: 요청 헤더 `Authorization: Bearer {token}` (유효 기간: 5분)
- **Refresh Token**: `HttpOnly` 쿠키 (유효 기간: 14일)
- 인증이 필요한 API에 토큰 없이 요청하면 `401 Unauthorized` 반환

### 인증 흐름

```
[일반 로그인]
POST /api/users/signin (username, password)
    → 200: { accessToken } + Set-Cookie: refreshToken

[GitHub OAuth]
1. 프론트엔드에서 GitHub 로그인 페이지로 리다이렉트
2. GitHub에서 callback URL로 code 전달
3. POST /api/auth/github { code }
    → GitHub API로 code ↔ access_token 교환
    → 사용자 조회/생성
    → JWT 발급: { accessToken } + Set-Cookie: refreshToken

[토큰 갱신]
POST /api/users/refresh (Cookie: refreshToken)
    → 200: { accessToken } + Set-Cookie: refreshToken (새로 발급)

[로그아웃]
POST /api/users/logout (Cookie: refreshToken)
    → refreshToken DB에서 삭제 + 쿠키 제거
```

### 프론트엔드 인터셉터

Axios 인터셉터가 자동으로:
1. 모든 요청에 `Authorization: Bearer {accessToken}` 헤더 추가
2. 401 응답 시 `/api/users/refresh`로 토큰 갱신 후 원래 요청 재시도

## API 엔드포인트

### User

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/users/signup` | 회원가입 | - |
| POST | `/api/users/signin` | 로그인 | - |
| POST | `/api/users/logout` | 로그아웃 | O |
| POST | `/api/users/refresh` | 토큰 갱신 | Cookie |
| GET | `/api/users/me` | 내 정보 조회 | O |
| PUT | `/api/users/edit` | 프로필 수정 | O |
| GET | `/api/users` | 전체 사용자 목록 | O |

### Auth (OAuth)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/github` | GitHub OAuth 코드 교환 | - |

### Issue

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/issues` | 이슈 목록 (필터링, 페이징) | O |
| POST | `/api/issues` | 이슈 생성 | O |
| GET | `/api/issues/{id}` | 이슈 상세 조회 | O |
| PATCH | `/api/issues/{id}/title` | 이슈 제목 수정 | O |
| PATCH | `/api/issues/{id}/status` | 이슈 상태 변경 (OPEN/CLOSED) | O |
| PATCH | `/api/issues/status` | 이슈 일괄 상태 변경 | O |
| POST | `/api/issues/{id}/assignees` | 담당자 추가 | O |
| POST | `/api/issues/{id}/assignees/remove` | 담당자 제거 | O |
| POST | `/api/issues/{id}/labels` | 라벨 추가 | O |
| POST | `/api/issues/{id}/labels/remove` | 라벨 제거 | O |
| POST | `/api/issues/{id}/milestones` | 마일스톤 연결 | O |
| POST | `/api/issues/{id}/milestones/remove` | 마일스톤 해제 | O |

#### 이슈 필터링 파라미터

`GET /api/issues`에서 사용 가능한 쿼리 파라미터:

| 파라미터 | 설명 |
|----------|------|
| `status` | `OPEN` / `CLOSED` |
| `assignee` | 담당자 ID |
| `label` | 라벨 ID |
| `milestone` | 마일스톤 ID |
| `author` | 작성자 ID |
| `page` | 페이지 번호 (0부터 시작, 20개씩) |

### Comment

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/issues/{id}/comments` | 이슈의 댓글 목록 | O |
| POST | `/api/issues/{id}/comments` | 댓글 작성 | O |
| DELETE | `/api/comments/{id}` | 댓글 삭제 | O |

### Label

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/labels` | 라벨 목록 | O |
| POST | `/api/labels` | 라벨 생성 | O |
| PUT | `/api/labels/{id}` | 라벨 수정 | O |
| DELETE | `/api/labels/{id}` | 라벨 삭제 | O |

### Milestone

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/milestones` | 마일스톤 목록 | O |
| POST | `/api/milestones` | 마일스톤 생성 | O |
| PUT | `/api/milestones/{id}` | 마일스톤 수정 | O |
| PATCH | `/api/milestones/{id}` | 마일스톤 상태 변경 | O |
| DELETE | `/api/milestones/{id}` | 마일스톤 삭제 (소프트) | O |

### Attachment

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/attachments/presign` | S3 Presigned URL 발급 | O |
| GET | `/api/attachments/{id}/url` | 첨부파일 다운로드 URL 조회 | O |

#### 파일 첨부 흐름

```
1. POST /api/attachments/presign
   → { id, presignedUrl } (status: PENDING)

2. 클라이언트가 presignedUrl로 S3에 직접 PUT 업로드

3. 이슈/댓글 생성 시 attachment ID 포함
   → status: PENDING → COMMITTED
```
