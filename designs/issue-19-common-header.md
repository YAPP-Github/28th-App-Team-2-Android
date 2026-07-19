# 설계 문서 — #19 [Feat] 공통 Header component 구현

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 전 **사람이 검토·수정하는 게이트**다.
> 자유롭게 수정해도 되고, 수정 후 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #19 (https://github.com/YAPP-Github/28th-App-Team-2-Android/issues/19)
- **작성**: 설계 에이전트 (Opus) / 2026-07-19
- **상태**: `사람-검토중`
- **관련 모듈**: `core:designsystem`

## 1. 문제 정의 / 목표
공통 Header 컴포넌트를 `core:designsystem`에 추가한다. Figma `Header` 컴포넌트셋은 두 변형을 가진다.
- **Header_main** (높이 60): 좌측에 `Title` + (선택) `subtext`, 우측에 알림(Bell) 아이콘.
- **Header_sub** (높이 48): 좌측 뒤로가기(chevron), 중앙 타이틀, 우측 닫기(X). *(진행바는 이번 범위에서 제외)*

- 완료 기준:
  - [ ] `TodakunMainHeader`, `TodakunSubHeader` 두 개의 Stateless Composable 제공.
  - [ ] 색/타이포는 `core:designsystem` 토큰(`TodakunTheme.colors/typography`)만 사용(하드코딩 금지, rule 40).
  - [ ] 필요한 아이콘 3종을 Figma SVG에서 추출해 `ic_` 접두 벡터 drawable로 추가(rule 40).
  - [ ] 각 Composable에 `@Preview` 제공, 모듈이 컴파일된다.
  - [ ] 콜백 동작/조건부 렌더링에 대한 Compose UI 테스트 작성.

## 2. 범위
- 포함: 두 Header Composable, 아이콘 3종(벡터 drawable), Preview, UI 테스트, designsystem 모듈 의존성 보강.
- 제외(Non-goals):
  - Header_sub의 **진행바(progress bar)** — 사용자 결정으로 제외(온보딩 진행바는 `feat/15-progressbar` 등 별도 컴포넌트 영역).
  - 실제 화면/네비게이션 연결(feature 모듈), ViewModel, 도메인/데이터 레이어. (본 이슈는 순수 UI 컴포넌트)

## 3. 참고 스펙
- Figma 컴포넌트셋: `Header` (node `1284:15268`)
  - Header_main: `Row`(가로), itemSpacing 12, padding L/R 20·T/B 16, `SPACE_BETWEEN`, `CENTER`. Title `700/22`(#000000), subtext `400/14`(#9A9A9A=gray500), Bell 24x24(#C4C4C4≈gray400).
  - Header_sub: 높이 48. Title `600/16`(#000000). back(chevron) 20x20(#131313), close(X) 20x20(#131313).
- 추출한 아이콘 노드: chevron `1284:15199`, close(delete) `1284:15228`, bell `1284:14981` (SVG 확보 완료).

## 4. 설계 및 실행 계획

### 4.1 API (사용자 결정: 두 개의 별도 Composable)
```kotlin
@Composable
fun TodakunMainHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    onBellClick: () -> Unit = {},
)

@Composable
fun TodakunSubHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null, // null이면 닫기 버튼 미표시
)
```
- **근거**: 두 변형은 좌/우 요소·타이포·높이가 모두 달라 단일 함수의 variant enum으로 묶으면 서로 무의미한 파라미터(subtext vs onBack/onClose)가 노출된다. 별도 함수가 타입 안전·호출 명확성에서 우수(사용자 채택).
- **Stateless**: 상태를 갖지 않고 콜백만 노출(rule 30 — 컴포넌트는 상태 없이, 로직은 상위에서). 미리보기·테스트 용이.
- `subtext == null`이면 서브텍스트 미표시. `onCloseClick == null`이면 닫기 버튼 자리 미표시(뒤로가기만 필요한 화면 대응).

### 4.2 레이아웃
- **Main**: `Row(fillMaxWidth, height 60dp, padding(horizontal=20, vertical=16), SpaceBetween, CenterVertically)`.
  - 좌측: `Row(itemSpacing 12, CenterVertically)` → Title(`typography.heading4Bold`, `colors.black`) + subtext(`typography.body3Regular`, `colors.gray500`).
  - 우측: `ic_bell` `Icon(size 24, tint = colors.gray400)`, `Modifier.clickable{ onBellClick() }`(min 40dp 터치영역).
- **Sub**: `Box(fillMaxWidth, height 48dp, padding(horizontal=20))`.
  - back: `ic_chevron_left` `Icon(size 20, tint = colors.gray975)` at `CenterStart`, clickable.
  - title: `typography.body2SemiBold`, `colors.black`, `Align.Center`, `TextAlign.Center`, `maxLines=1`.
  - close: `onCloseClick != null`일 때 `ic_close` `Icon(size 20, tint = colors.gray975)` at `CenterEnd`, clickable.
  - Box 오버레이 배치로 좌우 버튼 폭과 무관하게 타이틀이 항상 중앙 정렬.

### 4.3 아이콘 (사용자 결정: SVG→벡터 drawable, rule 40 `ic_` 접두)
- drawable의 `fillColor`는 원본 색을 유지하되, 실제 색은 Compose `Icon(tint=…)`이 테마 토큰으로 덮어써 렌더 색을 제어(토큰 사용은 Compose 측에서 보장, 하드코딩 아님).
- Bell은 fill 경로 + stroke 경로(1.5, round) 두 개 → 벡터 drawable의 `strokeColor/strokeWidth/strokeLineCap`로 표현. `Icon` tint가 전체 색을 균일 적용.

### 4.4 designsystem 모듈 의존성 보강
현재 `build.gradle.kts`에 Preview/UI 테스트 의존성이 없음 → 추가:
- `implementation(libs.androidx.compose.ui)` — painterResource/Icon 사용 명시화.
- `implementation(libs.androidx.compose.ui.tooling.preview)` — `@Preview`.
- `debugImplementation(libs.androidx.compose.ui.tooling)` — Preview 렌더.
- `androidTestImplementation(libs.androidx.compose.ui.test.junit4)`, `debugImplementation(libs.androidx.compose.ui.test.manifest)` — Compose UI 테스트.

### 4.5 트레이드오프
- **진행바 제외**: Figma Header_sub에는 진행바가 있으나, 진행바는 온보딩 플로우 상태에 종속적이라 Header에 강결합하면 재사용성이 떨어진다. 별도 컴포넌트로 두고 화면에서 조합하는 편이 유연(사용자 채택).
- **아이콘 tint 방식**: drawable에 색을 박지 않고 Compose tint로 토큰 적용 → rule 40(토큰 사용) 충족하면서 drawable은 단일 소스로 재사용 가능.
- **Box 오버레이 vs Row(SPACE_BETWEEN)** (Sub): 타이틀 중앙 고정이 요구되므로 Box 오버레이 채택(Row는 좌우 폭이 다르면 타이틀이 치우침).

## 5. 데이터 흐름
순수 Stateless UI 컴포넌트. `호출부(feature 화면) → Composable(title/subtext 전달) → 사용자 탭 → 콜백(onBackClick/onCloseClick/onBellClick) → 호출부에서 처리`. 도메인/데이터 흐름 없음.

## 6. 파일 변경 계획 (구현 체크리스트)
- [ ] `core/designsystem/src/main/res/drawable/ic_bell.xml` — Bell 벡터(24x24, fill+stroke).
- [ ] `core/designsystem/src/main/res/drawable/ic_chevron_left.xml` — 뒤로가기 벡터(20x20).
- [ ] `core/designsystem/src/main/res/drawable/ic_close.xml` — 닫기(X) 벡터(20x20).
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/header/TodakunMainHeader.kt` — Main Header + Preview.
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/header/TodakunSubHeader.kt` — Sub Header + Preview.
- [ ] `core/designsystem/build.gradle.kts` — 4.4의 의존성 추가.
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/header/HeaderTest.kt` — UI 테스트.

## 8. 리스크 / 미해결 질문 (사람 확인 필요)
- [ ] Header 배경색: Main/Sub 컴포넌트 배경이 Figma상 흰색(#FFFFFF). 투명으로 두고 화면 배경을 따르게 할지, 흰색을 명시할지 — **기본값: 배경 미지정(투명)** 으로 두되 필요 시 흰색 지정. 확인 필요.
-> 흰색으로
- [ ] Sub Header 좌우 패딩 20dp 가정(Figma 아이콘 프레임 기준 추정). 실제 스펙과 상이하면 조정.
-> 20dp로 진행
- [ ] Bell/아이콘 tint 색: 각 gray400/gray975로 매핑(Figma #C4C4C4/#131313 근사). 디자이너 확인 시 조정
-> Bell:gray975 아이콘 gray925
