# 규칙: 아키텍처 레이어

## 모듈 구조

- **by-feature 멀티 모듈** 구조이며 `domain`을 최상위(가장 안쪽) 모듈로 둔다.
- 현재 모듈 구성:
  - `app` — 앱 진입점, DI 조립, 네비게이션 호스트
  - `core:domain` — Model / UseCase / Repository 인터페이스 (프레임워크 비의존)
  - `core:data` — RepositoryImpl, Mapper
  - `core:data-remote` — Ktor 기반 Remote DataSource, DTO
  - `core:data-local` — DataStore 기반 Local DataSource, Entity
  - `core:navigation` — Navigation 3 라우팅
  - `core:designsystem` — 디자인 시스템(Color/Theme/Component)
  - `feature:login` (그리고 이후 추가될 `feature:*`)

## 의존성 방향 (위반 시 P1)

- 허용: `feature → domain`, `data → domain`
- **feature 모듈끼리 직접 참조 금지.** 발견 시 P1.
- 역방향 참조(`domain → feature`, `domain → data`) 금지. 발견 시 P1.
- feature 간 공유가 필요하면 `domain` 또는 별도 `core:*` 모듈로 승격한다.

## DI

- **Hilt**를 사용한다. 수동 팩토리/서비스로케이터로 우회하지 않는다.
- 모듈 경계에서 필요한 바인딩은 각 모듈의 Hilt `Module`에 선언한다.

## 설계 시 체크리스트

- [ ] 새 코드가 올바른 모듈에 위치하는가? (레이어 침범 없음)
- [ ] feature 간 직접 참조가 생기지 않는가?
- [ ] domain은 안드로이드/네트워크 프레임워크에 의존하지 않는가?
