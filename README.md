# [Issue Tracker]



> 프로젝트 협업을 위한 이슈, 마일스톤 관리 웹페이지 입니다



## 팀원 소개 (Team Members)



| 이름 | 역할 | GitHub |
| --- | --- | --- |
| 가비 | Backend | __[@WOWND](https://github.com/WOWND)__ |
| 존 | Backend | __[@Jon](https://github.com/jwpark97114)__ |

---



## 협업 전략 (Collaboration Strategy)



### 1. 브랜치 구조 (Branch Strategy)

우리는 [Git Flow / GitHub Flow] 전략을 간소화하여 사용합니다.

* `main`: 배포 가능한 상태의 안정적인 코드

* `develop`: 다음 출시를 위해 개발 중인 코드 (기본 작업 브랜치)

* `feature/{issue-number}-{short-desc}`: 새로운 기능 개발 (예: `feature/12-login-api`)

* `fix/{issue-number}-{short-desc}`: 버그 수정 (예: `fix/15-db-connection`)



### 2. 분업 및 의존적 작업 관리 (Division of Labor & Dependencies)

* **API First Design:** 프론트엔드와 백엔드의 병렬 작업을 위해 명세서(Swagger/REST Docs 등)를 먼저 정의하고 Mock API를 제공하여 블로킹을 방지합니다.

* **도메인 분리:** 충돌을 최소화하기 위해 도메인(예: Issue List)을 명확히 나누어 개발을 진행합니다.

* **공통 모듈:** 전역 예외 처리나 공통 유틸리티 클래스는 의존성이 높으므로 프로젝트 초기에 짝 프로그래밍(Pair Programming)으로 함께 구축합니다.



---



## 그라운드룰 (Ground Rules)



1. **리뷰 없이는 머지하지 않기:** 모든 PR(Pull Request)은 최소 1명 이상의 Approve를 받아야 `develop`에 머지할 수 있습니다.

2. **작업 전 이슈 생성:** 코드를 작성하기 전에 반드시 Project Board에 이슈를 생성하고, 스스로를 Assign합니다.

3. **일일 스탠드업:** 매일 [시간]에 15분간 어제 한 일, 오늘 할 일, 현재 블로킹된 요소를 공유합니다.

4. **깨진 유리창 방치 금지:** 빌드 실패나 심각한 에러 로그를 발견하면 진행 중인 작업을 멈추고 즉시 팀에 공유합니다.



---



## 템플릿 (Templates)



### 커밋 메시지 컨벤션 (Commit Convention)

[Karma 스타일](http://karma-runner.github.io/6.3/dev/git-commit-msg.html)을 따릅니다. 커밋 메시지 끝에 이슈 번호를 적어 자동 링크를 만듭니다.



* `feat:` 새로운 기능 추가

* `fix:` 버그 수정

* `docs:` 문서 수정

* `refactor:` 코드 리팩토링 (기능 변화 없음)

* `test:` 테스트 코드 추가/수정

* `chore:` 빌드 업무 수정, 패키지 매니저 수정



**예시:** `feat: 로그인 API JWT 검증 로직 추가 (#12)`



### 이슈 템플릿 (Issue Template)

*이슈 생성 시 아래 양식을 복사하여 사용하세요.*



**[기능 추가 / 버그 수정] 제목**

- **Context (배경):** 이 기능/버그 수정이 왜 필요한지 설명합니다.

- **Acceptance Criteria (완료 조건):** 

  - [ ] 작업 1 (예: DB 스키마 업데이트)

  - [ ] 작업 2 (예: 단위 테스트 통과)

- **Priority (우선순위):** (High, Medium, Low)



---



## 링크 (Links)



* **Project Board:** [https://github.com/orgs/codesquad-masters2026-team02/projects/1]

* **회의록 (Notion/Wiki):** [https://www.notion.so/issue-35692fa56810805ab299e22611ba6bc2]

* **API 명세서:** [Swagger/Postman 링크 삽입]

* **디자인 산출물:** [https://www.figma.com/proto/iCaxVg5GbJ3zdixryroKAU/WEB_%EC%9D%B4%EC%8A%88%ED%8A%B8%EB%9E%98%EC%BB%A4?node-id=29901-88031&p=f&t=xIzb1nTIu4iwT5L9-0&scaling=min-zoom&content-scaling=fixed&page-id=89%3A0]
