# Database

## 개요

- **로컬**: H2 인메모리 DB (MySQL 호환 모드)
- **프로덕션**: MySQL
- **마이그레이션**: Flyway (`apps/spring-api/src/main/resources/db/migration/`)

## ERD

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   users      │       │   issues     │       │  milestones  │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │◀──┐   │ id (PK)      │──────▶│ id (PK)      │
│ username     │   │   │ author_id(FK)│       │ name         │
│ password     │   │   │ title        │       │ due_date     │
│ refresh_token│   │   │ status       │       │ description  │
│ oauth_provider│  │   │ created_at   │       │ status       │
│ oauth_id     │   │   │ milestone_id │       │ is_deleted   │
│ profile_image│   │   └──────┬───────┘       └──────────────┘
└──────────────┘   │          │
       ▲           │          │
       │           │          │
┌──────┴───────┐   │   ┌──────▼───────┐       ┌──────────────┐
│ issue_users  │   │   │ issue_labels │       │   labels     │
├──────────────┤   │   ├──────────────┤       ├──────────────┤
│ issue_id(PK) │   │   │ issue_id(PK) │       │ id (PK)      │
│ user_id (PK) │───┘   │ label_id(PK) │──────▶│ name         │
└──────────────┘       └──────────────┘       │ description  │
                                              │ bg_color     │
       ┌──────────────┐                       │ text_color   │
       │  comments    │                       └──────────────┘
       ├──────────────┤
       │ id (PK)      │       ┌──────────────┐
       │ content      │       │ attachments  │
       │ type         │       ├──────────────┤
       │ attachment_key│      │ id (PK, UUID)│
       │ created_at   │      │ s3_key       │
       │ updated_at   │      │ filename     │
       │ user_id (FK) │      │ content_type │
       │ issue_id(FK) │      │ size_bytes   │
       └──────────────┘      │ uploader_id  │
                              │ comment_id   │
                              │ status       │
                              │ created_at   │
                              │ committed_at │
                              └──────────────┘
```

## 테이블 명세

### users

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | |
| username | VARCHAR(64) | 사용자명 |
| password | VARCHAR(128) | bcrypt 해시 |
| refresh_token | TEXT | JWT 리프레시 토큰 |
| oauth_provider | VARCHAR(50) | OAuth 제공자 (e.g. `github`) |
| oauth_id | VARCHAR(255) | OAuth 제공자의 사용자 ID |
| profile_image_url | VARCHAR(512) | 프로필 이미지 URL |

### issues

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | |
| author_id | BIGINT (FK) | 작성자 |
| title | VARCHAR(255) | 이슈 제목 |
| status | VARCHAR(50) | `OPEN` / `CLOSED` |
| created_at | DATETIME | 생성 시각 |
| milestone_id | BIGINT (FK → milestones) | 연결된 마일스톤 |

### milestones

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | |
| name | VARCHAR(100) | 마일스톤 이름 |
| due_date | DATE | 마감일 |
| description | TEXT | 설명 |
| status | VARCHAR(50) | `OPEN` / `CLOSED` |
| is_deleted | BOOLEAN | 소프트 삭제 플래그 |

### labels

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | |
| name | VARCHAR(50) | 라벨 이름 |
| description | VARCHAR(100) | 설명 |
| background_color | CHAR(7) | 배경색 (e.g. `#FF0000`) |
| text_color | VARCHAR(7) | 글자색 |

### comments

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | |
| content | TEXT | 댓글 내용 (마크다운) |
| type | VARCHAR(16) | `ISSUE_BODY` (본문) / `DISCUSSION` (댓글) |
| attachment_key | VARCHAR(255) | 첨부파일 키 |
| created_at | DATETIME | 생성 시각 |
| updated_at | DATETIME | 수정 시각 |
| user_id | BIGINT (FK → users) | 작성자 |
| issue_id | BIGINT (FK → issues) | 소속 이슈 |

### issue_labels (다대다 조인 테이블)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| issue_id | BIGINT (PK, FK → issues) | |
| label_id | BIGINT (PK, FK → labels) | |

- `label_id`에 인덱스 (`idx_label_id`)

### issue_users (다대다 조인 테이블 - 담당자)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| issue_id | BIGINT (PK, FK → issues) | |
| user_id | BIGINT (PK, FK → users) | |

### attachments

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | VARCHAR(36) (PK) | UUID |
| s3_key | TEXT | S3 객체 키 |
| filename | TEXT | 원본 파일명 |
| content_type | TEXT | MIME 타입 |
| size_bytes | BIGINT | 파일 크기 |
| uploader_id | BIGINT (FK → users) | 업로더 |
| comment_id | BIGINT (FK → comments) | 연결된 댓글 (nullable) |
| status | VARCHAR(64) | `PENDING` / `COMMITTED` |
| created_at | TIMESTAMP | Presigned URL 발급 시각 |
| committed_at | TIMESTAMP | 커밋(사용 확정) 시각 |

## 마이그레이션

마이그레이션 파일은 `apps/spring-api/src/main/resources/db/migration/` 디렉토리에 위치합니다.

| 파일 | 설명 |
|------|------|
| `V1__init_schema.sql` | 초기 스키마 (전체 테이블 생성) |

새 마이그레이션 추가 시 `V{N}__{description}.sql` 형식으로 파일을 생성합니다.
