# 규칙: Data 레이어 (`core:data`, `core:data-remote`, `core:data-local`)

> 출처: `.coderabbit.yaml` path_instructions (data).
> 심각도([P1]/[P2]/[P3]) 정의: `.claude/rules/50-review-convention.md` 참조.

## RepositoryImpl — `**/data/**/*RepositoryImpl.kt`

- 성공/실패는 `runCatching`으로 처리하고 `Result` 형태로 반환한다. try-catch 누락이나 예외 누수는 **P1**.
- DataSource가 반환한 `~Data` 접미사 클래스를 **Mapper 또는 확장 함수**로 Domain Model로 변환한다.
- 같은 관심사라면 내부에서 다중 API 호출/합성 후 하나의 Domain Model 반환을 허용한다.
- 캐싱 시 Domain 변환 **이전의 Raw `~Data` 클래스**를 DataSource에 전달한다(**P2**).

## DataSource — `**/data/**/*DataSource*.kt`

- 성공/실패를 try-catch 하지 않고 에러를 **그대로 throw**한다. 예외를 삼키면 **P1**.
- Domain Model을 반환한다.
- Remote DataSource 함수는 **REST 엔드포인트와 1:1 대응**. 엔드포인트가 같고 METHOD가 다르면 함수를 분리한다.
- 함수 접두사는 동사이며 HTTP METHOD와 연관된 단어여야 한다(**P2**).

## DTO — `**/data/**/*Request.kt`, `**/data/**/*Response.kt`

- 도메인 로직과 무관하게 **Raw 서버 데이터 형태**를 유지한다.
- 네이밍은 `~Request`, `~Response`를 사용하고 재사용하지 않는다. 위반 시 **P2**.
- API 스펙은 Swagger 문서를 기준으로 작성한다 (링크는 `CLAUDE.md` 참조).

## Entity — `**/data/**/*Entity.kt`

- 로컬 DB 저장/쿼리 속도를 위해 **Flatten 형태**로 작성한다.
- 도메인 로직과 무관하게 Raw 데이터를 유지한다.
