# 규칙: Domain 레이어 (`core:domain`)

> 출처: `.coderabbit.yaml` path_instructions (domain).
> 심각도([P1]/[P2]/[P3]) 정의: `.claude/rules/50-review-convention.md` 참조.

## Model — `**/domain/**/model/**/*.kt`

- 모든 필드는 `val`로 선언(불변성). `var` 발견 시 **P1**.
- `data class` 또는 `sealed interface/class` 형태여야 한다.
- DTO/Entity를 그대로 노출 금지. 비즈니스 로직에 맞게 정리된 형태여야 한다(**P1**).

## UseCase — `**/domain/**/*UseCase.kt`

- 이름은 **동사로 시작**(예: `GetHomeInfoUseCase`). 위반 시 **P2**.
- "유저의 행동" 또는 "단일 기능 단위"로 구분한다.
- 여러 UseCase를 합성하는 **퍼사드 패턴 허용**.
- 서로 다른 관심사의 데이터 조합은 Repository가 아니라 **UseCase에서** 수행한다.

## Repository 인터페이스 — `**/domain/**/*Repository.kt`

- 반환 타입은 `Result<T>`, `Flow<Result<T>>`, Paging 사용 시 `Flow<PagingData<T>>` 중 하나여야 한다. 위반 시 **P1**.
- **Domain Model을 반환**하며 `~DTO/~Entity/~Data` 접미사 클래스를 노출하면 안 된다(**P1**).
- "데이터의 관심사" 단위로 분리한다(예: `UserRepository`, `StarRepository`).
