# ktlint + Spotless + Compose Lint(nlopez) 도입 설계

## 배경
`CLAUDE.md` 6절의 린트 명령어가 TODO 상태이며, 저장소에 코드 포맷/린트 자동화가 없다. ktlint를 Spotless로 실행하고, Compose 전용 룰셋으로 `io.nlopez.compose.rules:ktlint`(mrmans0n/nlopez fork)를 추가한다.

## 완료 기준
- `./gradlew spotlessCheck` / `./gradlew spotlessApply` 태스크가 루트에서 동작한다.
- ktlint official 스타일 + nlopez Compose 룰셋이 `.kt` 파일에 적용된다.
- `check`/CI 빌드에는 연결하지 않는다(사용자 결정: 미포함). git pre-commit 훅도 설치하지 않는다.
- 기존 코드베이스 전체가 `spotlessApply`로 1회 재포맷되어 `spotlessCheck`를 통과한다.
- `./gradlew test`가 재포맷 이후에도 통과한다.
- `CLAUDE.md`의 린트 명령어 TODO가 실제 명령으로 채워진다.

## 적용 범위 및 방식
- Spotless Gradle 플러그인은 **루트 프로젝트에만** 적용한다. `target("**/*.kt")` 글롭으로 전체 서브모듈의 `.kt` 파일을 커버하므로 서브모듈별 적용이 불필요하다.
- `**/build/**`, `**/generated/**`는 포맷 대상에서 제외한다.
- `kotlinGradle { target("**/*.gradle.kts") }` 블록으로 buildscript도 함께 포맷한다.

## 버전 (Maven Central 확인 완료, 2026-07-15 기준 최신 안정)
- `com.diffplug.spotless` Gradle 플러그인: `8.8.0`
- ktlint 엔진: `1.8.0` (official 코드 스타일 — `gradle.properties`의 기존 `kotlin.code.style=official`과 일치)
- Compose 룰셋: `io.nlopez.compose.rules:ktlint:0.6.2`

## 파일 변경 계획
- [ ] `gradle/libs.versions.toml`: `spotless`/`ktlintCli`/`composeRules` 버전 항목, `spotless` 플러그인 alias, `nlopez-compose-rules-ktlint` 라이브러리 alias 추가
- [ ] `build.gradle.kts` (루트): `alias(libs.plugins.spotless)` 적용 + `spotless { kotlin { ... }; kotlinGradle { ... } }` 설정
- [ ] `.editorconfig` (신규, 루트): `root = true`, `[*.{kt,kts}]`에 4-space indent, `ktlint_code_style = official`, **`ktlint_function_naming_ignore_when_annotated_with = Composable`**(Composable 함수 PascalCase 네이밍이 표준 함수명 규칙과 충돌하는 것을 방지하는 필수 예외)
- [ ] `CLAUDE.md`: 6절 TODO 라인을 `./gradlew spotlessCheck` / `./gradlew spotlessApply`로 교체
- [ ] 전체 소스에 `./gradlew spotlessApply` 1회 실행하여 기존 코드 재포맷

## 테스트 계획
1. `./gradlew spotlessCheck` — 적용 직후 기존 코드 위반으로 실패 확인
2. `./gradlew spotlessApply` — 전체 재포맷
3. `./gradlew spotlessCheck` — 통과 확인
4. `./gradlew test` — 포맷팅으로 인한 회귀 없는지 확인 (컴파일/테스트 통과)

## 범위 제외
- `check`/CI 빌드 게이팅 (사용자 결정: 미포함)
- git pre-commit 훅 설치 (사용자 결정: 미설치)
- Detekt 등 별도 정적분석 도구 도입은 이번 범위 아님
