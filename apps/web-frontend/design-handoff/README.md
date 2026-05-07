# Issue Tracker — 프론트엔드 개발 가이드

> AI가 프론트엔드를 개발할 때 참고하는 문서입니다.
> 디자인 토큰, 페이지 구조, 컴포넌트 스펙, 인터랙션 규칙을 담고 있습니다.

---

## 1. 기술 스택

| 항목 | 내용 |
|------|------|
| 프레임워크 | React |
| 언어 | JavaScript (또는 TypeScript) |
| 스타일 | CSS Variables (`tokens.css` 기반) |
| 폰트 | Pretendard (CDN) |
| 아이콘 | `/icons` 폴더의 SVG 파일 사용 |
| 반응형 | 데스크탑 전용 (1280px 기준) |

---

## 2. 파일 구조

```
📁 프로젝트 루트
├── README.md              ← 이 파일
├── tokens.css             ← 디자인 토큰 (색상, 폰트, 간격 등)
├── 📁 icons/              ← SVG 아이콘 파일들 (camelCase)
│   ├── alertCircle.svg
│   ├── archive.svg
│   ├── calendar.svg
│   ├── checkBoxActive.svg
│   ├── checkBoxDisable.svg
│   ├── checkBoxInitial.svg
│   ├── checkOffCircle.svg
│   ├── checkOnCircle.svg
│   ├── chevronDown.svg
│   ├── edit.svg
│   ├── label.svg
│   ├── milestone.svg
│   ├── paperclip.svg
│   ├── plus.svg
│   ├── refreshCcw.svg
│   ├── search.svg
│   ├── smile.svg
│   ├── trash.svg
│   ├── userImageLarge.svg
│   ├── userImageSmall.svg
│   └── xSquare.svg
└── 📁 screens/            ← 화면 디자인 PNG
    ├── 01-login.png
    ├── 02-issue-list.png
    ├── 03-issue-detail.png
    ├── 04-issue-create.png
    ├── 05-label.png
    └── 06-milestone.png
```

---

## 3. 페이지 목록 & 라우팅

| 경로 | 페이지 | 설명 |
|------|--------|------|
| `/login` | 로그인 | GitHub OAuth 또는 아이디/비밀번호 로그인 |
| `/` | 이슈 목록 | 이슈 필터, 검색, 정렬 |
| `/issues/new` | 이슈 작성 | 새 이슈 생성 폼 |
| `/issues/:id` | 이슈 상세 | 이슈 내용 + 코멘트 목록 |
| `/labels` | 레이블 관리 | 레이블 목록, 추가/편집/삭제 |
| `/milestones` | 마일스톤 관리 | 마일스톤 목록, 추가/편집/삭제 |

---

## 4. 공통 레이아웃

### 헤더
- 좌측: "Issue Tracker" 로고 (이탤릭 bold, 클릭 시 `/` 이동)
- 우측: 로그인한 유저 아바타 (원형 이미지)
- 로그인 페이지에서는 헤더 없음

### 페이지 배경
- 배경색: `var(--color-bg-page)` = `#F7F7FC`
- 콘텐츠 최대 너비: `1280px`, 가운데 정렬

---

## 5. 페이지별 상세 스펙

### 5-1. 로그인 페이지 (`/login`)

**레이아웃:** 화면 중앙 정렬, 세로 스택

**구성 요소:**
- 로고 텍스트 "Issue Tracker" (이탤릭)
- "GitHub 계정으로 로그인" 버튼 (outline 스타일, 파란 텍스트)
- "or" 구분 텍스트
- 아이디 인풋
- 비밀번호 인풋
- "아이디로 로그인" 버튼 (primary, 폼 미입력 시 disabled)
- "회원가입" 텍스트 링크

---

### 5-2. 이슈 목록 페이지 (`/`)

**상단 바:**
- 좌측: `필터` 드롭다운 버튼 + 검색 인풋 (`is:issue is:open` 기본값)
- 우측: `레이블(N)` 버튼, `마일스톤(N)` 버튼, `+ 이슈 작성` 버튼 (primary)

**필터 드롭다운 항목:**
- 열린 이슈 (기본 선택)
- 내가 작성한 이슈
- 나에게 할당된 이슈
- 내가 댓글을 남긴 이슈
- 닫힌 이슈

**이슈 테이블 헤더:**
- 좌측: 전체 선택 체크박스, `열린 이슈(N)` / `닫힌 이슈(N)` 탭
- 우측: `담당자` / `레이블` / `마일스톤` / `작성자` 드롭다운 필터

**각 드롭다운 필터 항목:**
- 담당자 필터: "담당자가 없는 이슈" + 유저 목록 (아바타 + 이름)
- 레이블 필터: "레이블이 없는 이슈" + 레이블 목록 (색상 원 + 이름)
- 마일스톤 필터: "마일스톤이 없는 이슈" + 마일스톤 목록
- 작성자 필터: 유저 목록 (아바타 + 이름)

**이슈 행 구성:**
- 체크박스
- 상태 아이콘 (열림: 파란 원 i, 닫힘: 회색)
- 이슈 제목 (bold, 파란 링크)
- 레이블 뱃지들 (있을 경우)
- 하단: `#번호`, 작성자 정보, 타임스탬프, 마일스톤
- 우측: 담당자 아바타

**이슈 선택 시 (체크박스 클릭):**
- 헤더가 "N개 이슈 선택" + `상태 수정` 드롭다운으로 변경
- 전체 선택 체크박스는 indeterminate(-) 상태
- 상태 수정 드롭다운: "선택한 이슈 열기" / "선택한 이슈 닫기"

**빈 상태:**
- 이슈가 없을 때: "등록된 이슈가 없습니다." 텍스트 표시
- 검색 결과 없을 때: "검색과 일치하는 결과가 없습니다." 표시

**필터 적용 시:**
- 검색바 아래에 "× 현재의 검색 필터 및 정렬 지우기" 텍스트 버튼 표시

---

### 5-3. 이슈 작성 페이지 (`/issues/new`)

**레이아웃:** 좌측 메인(아바타 + 폼) + 우측 사이드바

**좌측 폼:**
- 현재 유저 아바타
- 제목 인풋 (한 줄)
- 코멘트 텍스트에어리어 (placeholder: "코멘트를 입력하세요", 우측 하단 resize 핸들)
- 하단: `📎 파일 첨부하기` 링크 (점선 구분선 위)

**우측 사이드바:**
- `담당자` 섹션 (+ 버튼으로 추가, 유저 목록 드롭다운)
- `레이블` 섹션 (+ 버튼으로 추가, 레이블 목록 드롭다운)
- `마일스톤` 섹션 (+ 버튼으로 추가, 마일스톤 목록 드롭다운)

**하단 버튼 바:**
- `× 작성 취소` (텍스트 버튼, 좌측)
- `완료` 버튼 (primary, 우측 — 제목 없을 시 disabled)

---

### 5-4. 이슈 상세 페이지 (`/issues/:id`)

**헤더 영역:**
- 이슈 제목 + `#번호` (파란색)
- 우측: `제목 편집` 버튼 (outline), `이슈 닫기` / `이슈 열기` 버튼 (outline)
- 제목 편집 모드: 인풋으로 전환 + `편집 취소` / `편집 완료` 버튼

**이슈 상태 표시:**
- `열린 이슈` 뱃지 (파란 배경) 또는 `닫힌 이슈` 뱃지 (회색 배경)
- "이 이슈가 N분 전에 username님에 의해 열렸습니다" + "코멘트 N개"

**레이아웃:** 좌측 콘텐츠 + 우측 사이드바

**코멘트 카드:**
- 유저 아바타 + 이름 + 타임스탬프
- `작성자` 뱃지 (본인 코멘트일 때만)
- `편집` 버튼, `반응` 버튼
- 본문 내용 (마크다운 렌더링)
- 편집 모드: 텍스트에어리어로 전환 + 글자수 표시 + `편집 취소` / `편집 완료`

**새 코멘트 입력 영역:**
- 텍스트에어리어 + 파일 첨부하기
- `+ 코멘트 작성` 버튼 (primary, 내용 없을 시 disabled)

**우측 사이드바:**
- 담당자 목록 (+ 버튼으로 변경 가능)
- 레이블 목록 (+ 버튼으로 변경 가능)
- 마일스톤 (진행바 + 이름, + 버튼으로 변경 가능)
- `🗑 이슈 삭제` 버튼 (빨간 텍스트)

---

### 5-5. 레이블 관리 페이지 (`/labels`)

**상단 탭:**
- `레이블(N)` 탭 (현재 선택)
- `마일스톤(N)` 탭
- 우측: `+ 레이블 추가` / `+ 마일스톤 추가` 버튼

**레이블 추가/편집 폼 (인라인 펼쳐짐):**
- 좌측: 레이블 미리보기 박스
- 우측: 이름 인풋, 설명(선택) 인풋, 배경색상 입력 + 새로고침 버튼 + 텍스트색상 드롭다운
- `× 취소` / `+ 완료` 버튼

**레이블 목록 행:**
- 레이블 뱃지 (실제 색상 적용)
- 설명 텍스트
- 우측: `편집` 버튼, `삭제` 버튼 (빨간색)

---

### 5-6. 마일스톤 관리 페이지 (`/milestones`)

**마일스톤 추가/편집 폼 (인라인 펼쳐짐):**
- 이름 인풋 + 완료일(선택) 인풋 (YYYY. MM. DD 형식)
- 설명(선택) 인풋
- `× 취소` / `+ 완료` 버튼

**마일스톤 목록 행:**
- 마일스톤 아이콘 + 이름
- 완료일 (캘린더 아이콘 + 날짜)
- 설명 텍스트
- 우측: 진행 바 (파란색) + 퍼센트, `열린 이슈 N` `닫힌 이슈 N`
- `닫기` / `편집` / `삭제` 버튼

---

## 6. 컴포넌트 스펙

### 버튼

| 종류 | 설명 | 배경 | 텍스트 | 보더 |
|------|------|------|--------|------|
| Primary | 주요 액션 | `--color-primary-500` | white | 없음 |
| Outline | 보조 액션 | transparent | `--color-primary-500` | `--color-primary-500` |
| Ghost | 텍스트 전용 | transparent | `--color-text-secondary` | 없음 |
| Danger | 삭제 액션 | transparent | `--color-error` | 없음 |
| Disabled | 비활성 | `--color-interactive-primary-disabled` | white | 없음 |

- 기본 높이: `40px`
- border-radius: `var(--radius-full)` (pill 형태)
- 글자 크기: `14px`, weight: `500`

### 인풋 / 텍스트에어리어

- 높이: `40px` (인풋), 자유 높이 (텍스트에어리어)
- border-radius: `8px`
- 배경: `var(--color-bg-input)` = `#EFF0F6`
- 보더: 기본 없음 → focus 시 `var(--color-primary-500)` 1px
- placeholder 색상: `var(--color-text-placeholder)` = `#A0A3BD`
- error 상태: 보더 `var(--color-error)`, 하단 에러 텍스트 표시

### 레이블 뱃지

- 높이: `24px`, 좌우 패딩: `8px`
- border-radius: `var(--radius-full)`
- 글자 크기: `12px`
- 색상: 배경/텍스트 커스텀 (레이블 설정값 사용)

### 드롭다운 패널

- 배경: `white`
- border-radius: `8px`
- 그림자: `var(--shadow-dropdown)`
- 최소 너비: `200px`
- 항목 높이: `40px`, 좌우 패딩: `16px`

### 체크박스

- 크기: `16px × 16px`
- border-radius: `4px`
- 미선택: 보더만 표시
- 선택: `--color-primary-500` 배경 + 흰 체크
- Indeterminate: `--color-primary-500` 배경 + 흰 minus(-)

### 진행 바 (Progress)

- 높이: `4px`
- border-radius: `9999px`
- 배경 트랙: `var(--color-gray-200)`
- 진행 색상: `var(--color-primary-500)`

---

## 7. 인터랙션 규칙

- **드롭다운**: 트리거 클릭 시 열림, 외부 클릭 시 닫힘
- **필터 선택**: 라디오 방식 (단일 선택), 선택된 항목에 체크 아이콘
- **이슈 체크박스**: 다중 선택 가능, 하나라도 선택 시 헤더 변경
- **버튼 disabled**: 조건 미충족 시 opacity 낮추고 클릭 불가
- **폼 편집 모드**: 인라인으로 전환, 취소 시 원래 상태로 복원
- **코멘트 글자수**: 텍스트에어리어 우측 하단에 "띄어쓰기 포함 N자" 실시간 표시

---

## 8. 디자인 토큰 사용법

```css
/* tokens.css를 최상위에서 import */
@import './tokens.css';

/* 사용 예시 */
.button-primary {
  height: var(--button-height-md);
  background-color: var(--color-interactive-primary);
  border-radius: var(--radius-button);
  font-size: var(--text-button-size);
  color: var(--color-text-on-primary);
}

.input {
  height: var(--input-height);
  background-color: var(--color-bg-input);
  border-radius: var(--radius-input);
  padding: 0 var(--input-padding-x);
  color: var(--color-text-primary);
}
```

---

## 9. 아이콘 사용법

> 모든 SVG는 `/icons` 폴더에 **camelCase**로 저장되어 있습니다. 파일명에 `icon-` 접두사는 사용하지 않습니다.

```jsx
// React에서 SVG 인라인 import (색상 변경 가능)
import { ReactComponent as SearchIcon } from './icons/search.svg';

// 또는 img 태그 (색상 변경 불가)
<img src="./icons/search.svg" alt="검색" />
```

### 아이콘 매핑표

| 파일명 | 용도 | 사용 위치 |
|--------|------|-----------|
| `search.svg` | 검색 | 이슈 목록 검색 인풋 |
| `alertCircle.svg` | 정보/경고 표시 | 인풋 에러 메시지 등 |
| `checkOnCircle.svg` | **열린 이슈** 상태 아이콘 | 이슈 목록/상세, 열린 이슈 뱃지 |
| `checkOffCircle.svg` | **닫힌 이슈** 상태 아이콘 | 이슈 목록/상세, 닫힌 이슈 뱃지 |
| `archive.svg` | 닫힌 이슈 탭 / 보관 | 이슈 목록 탭 |
| `xSquare.svg` | 닫기 / 취소 / 삭제(X) | 작성 취소, 필터 지우기, 모달 닫기 |
| `plus.svg` | 추가 | 이슈/레이블/마일스톤 추가, 사이드바 + 버튼 |
| `refreshCcw.svg` | 새로고침 / 색상 랜덤 | 레이블 색상 랜덤 생성 |
| `edit.svg` | 편집 | 제목 편집, 코멘트 편집, 레이블/마일스톤 편집 |
| `trash.svg` | 삭제 | 이슈 삭제, 레이블/마일스톤 삭제 |
| `smile.svg` | 반응(이모지) | 코멘트 반응 버튼 |
| `calendar.svg` | 날짜 | 마일스톤 완료일 표시 |
| `label.svg` | 레이블 | 레이블 탭/필터 버튼, 레이블 뱃지 영역 |
| `milestone.svg` | 마일스톤 | 마일스톤 탭/필터 버튼 |
| `paperclip.svg` | 파일 첨부 | 이슈/코멘트 작성 첨부 |
| `chevronDown.svg` | 드롭다운 화살표 | 모든 드롭다운 트리거, 정렬, 필터 |
| `checkBoxInitial.svg` | 체크박스 — 미선택 | 이슈 목록 체크박스 |
| `checkBoxActive.svg` | 체크박스 — 선택됨 | 이슈 목록 체크박스 |
| `checkBoxDisable.svg` | 체크박스 — 비활성 | 이슈 목록 체크박스 |
| `userImageLarge.svg` | 유저 아바타 기본 (큰 사이즈) | 이슈 작성/상세, 코멘트 카드 |
| `userImageSmall.svg` | 유저 아바타 기본 (작은 사이즈) | 이슈 목록 행, 사이드바 |

### ⚠️ 누락 아이콘 (필요 시 추가)

현재 폴더에 없지만 화면에서 사용될 가능성이 있는 아이콘:
- **필터 슬라이더 아이콘** — 이슈 목록 상단 "필터" 드롭다운 트리거 (현재는 텍스트만)
- **GitHub 로고** — 로그인 페이지 "GitHub 계정으로 로그인" 버튼
- **체크 (✓)** — 드롭다운 라디오 선택 표시용 단일 아이콘 (현재는 `checkOnCircle`로 대체 가능)

---

## 10. 주의사항

- 모든 색상, 간격, 폰트 크기는 반드시 `tokens.css`의 CSS 변수를 사용할 것
- 하드코딩된 색상값(`#007AFF` 등) 직접 사용 금지
- 버튼은 모두 pill 형태(`border-radius: 9999px`)
- 인풋/카드는 `border-radius: 8px`
- 페이지 배경은 항상 `#F7F7FC` (흰색 아님)
- 다크모드는 `data-theme="dark"` 속성으로 전환
