# 규칙: Compose UI & 리소스 네이밍

> 출처: `.coderabbit.yaml` path_instructions (Compose, res/drawable).
> 심각도([P1]/[P2]/[P3]) 정의: `.claude/rules/50-review-convention.md` 참조.

## Compose / 일반 Kotlin — `**/*.kt`

- **ktlint 및 compose lint 컨벤션**을 따른다.
- Composable 함수의 불필요한 리컴포지션 유발 요소(불안정 파라미터, 람다 재생성 등)가 있으면 **P2**.
- 디자인 값(Color/Typography/Spacing)은 `core:designsystem`의 테마 토큰을 사용하고 하드코딩하지 않는다.
- UI 구현은 Figma 디자인을 기준으로 한다 (링크는 `CLAUDE.md` 참조).

## 리소스 네이밍 — `**/res/drawable/**`

- `ic_` 접두사 → SVG(벡터) 파일.
- `img_` 접두사 → 그 외(png 등) 파일.
- 규칙을 벗어난 네이밍은 **P3**.
