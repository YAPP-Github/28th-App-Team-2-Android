# 규칙: Presentation 레이어 (MVI + Orbit)

> 출처: `.coderabbit.yaml` path_instructions (presentation, ViewModel).
> 심각도([P1]/[P2]/[P3]) 정의: `.claude/rules/50-review-convention.md` 참조.

## Presentation — `**/presentation/**/*.kt`
- Orbit 사용 시 `ContainerHost` 위임은 `by(Delegate)` 형태를 권장한다.
- ViewModel 또는 상태 처리에서 직접 도메인 로직을 수행하지 말고 **UseCase를 통한다**(**P2**).

## ViewModel — `**/*ViewModel.kt`
- 상태 보존이 필요한 값은 `SavedStateHandle` 사용을 검토한다.
- 비즈니스 로직은 UseCase에 위임하고 ViewModel은 **상태 관리에 집중**한다.

## 참고 라이브러리

- MVI: Orbit (`orbit-core`, `orbit-viewmodel`, `orbit-compose`), 테스트는 `orbit-test`.
- 네비게이션: Navigation 3 (`androidx.navigation3.*`).
