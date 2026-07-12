# 설계 문서 — Wheel Picker Base 컴포넌트

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 전 **사람이 검토·수정하는 게이트**다.
> 자유롭게 수정해도 되고, 수정 후 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: 없음 (slug 기반: `wheel-picker-base`)
- **작성**: 메인 오케스트레이터(대화형 브레인스토밍) / 2026-07-12
- **상태**: `승인됨(구현 가능)`
- **관련 모듈**: `core:designsystem`

## 1. 문제 정의 / 목표

iOS `UIPickerView` 스타일의 드럼롤 휠 피커를 3개의 Figma 프레임(1/2/3휠 변형)에 맞춰 **stateless Compose 컴포넌트**로 구현한다. 세 변형은 카드 크롬(핸들바, 타이틀+저장 버튼, 하이라이트 필)을 공유하지만 휠 개수·데이터·컬럼 간격이 다르므로, 하나의 Base picker가 컬럼 개수와 데이터를 파라미터로 받아 세 변형을 조합해야 한다.

- 완료 기준:
  - [ ] `WheelPickerColumn`(단일 휠 원시 컴포넌트)이 stateless로 구현되어 있다.
  - [ ] `WheelPicker`(Base)가 `columns: List<WheelPickerColumnState>`를 받아 1~N개 휠을 조합할 수 있다.
  - [ ] `WheelPickerSingle` / `WheelPickerDouble` / `WheelPickerTriple` 3개 wrapper가 각각 Figma 노드 383:1630 / 1417:23096 / 383:1347 을 재현한다.
  - [ ] 색상·타이포는 전부 `core:designsystem` 테마 토큰을 사용한다(하드코딩 없음).
  - [ ] Compose UI 테스트가 추가되고 통과한다.

## 2. 범위

- 포함:
  - `core:designsystem`에 새 `component/wheelpicker` 패키지 추가
  - `WheelPickerColumn`, `WheelPicker`(Base), `WheelPickerColumnState`
  - `WheelPickerSingle`, `WheelPickerDouble`, `WheelPickerTriple` wrapper + `@Preview`
  - `core:designsystem`에 Compose UI 테스트 인프라(androidTest) 배선
- 제외(Non-goals):
  - 가운데 항목 탭 → 직접 입력(TextField) 기능 (Figma에 없음, 명시적으로 제거)
  - 실제 기능(feature) 화면에서의 통합(생년월일 입력 화면 등) — 이번 작업은 designsystem 컴포넌트까지만
  - 숫자 포매팅("08"), 시진 라벨 조합 등 도메인 로직 — 전부 호출부(feature) 책임
  - 접근성(TalkBack) 세부 튜닝

## 3. 참고 스펙

- Figma:
  - `WheelPicker_single` — node-id `383:1630` — "태어난 시각 선택", 1컬럼, 시진 문자열 라벨
  - `WheelPicker_multi` — node-id `1417:23096` — "받을 시간 입력", 2컬럼(시/분), 컬럼 간격 64dp
  - `WheelPicker_multi02` — node-id `383:1347` — "생년월일 입력", 3컬럼(년/월/일), 컬럼 간격 40dp, 컬럼폭 60/40/40dp
- 참고 코드: 사용자가 제공한 `WheelNumberPicker` 프로토타입(`com.example.androidtest`, 본 저장소 소속 아님) — 스냅 스크롤·drum-roll graphicsLayer 로직의 메커니즘 레퍼런스로만 사용.

## 4. 설계 및 실행 계획

### 4.1 컴포넌트 계층

```
core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/
  WheelPickerColumn.kt   — 단일 휠 원시 컴포넌트 (internal)
  WheelPicker.kt         — Base 컴포저블 + WheelPickerColumnState
  WheelPickerVariants.kt — WheelPickerSingle/Double/Triple + @Preview 3종
```

### 4.2 API

```kotlin
// WheelPicker.kt
@Immutable
data class WheelPickerColumnState(
    val items: List<String>,               // 이미 포맷된 표시용 라벨
    val selectedIndex: Int,
    val onSelectedIndexChange: (Int) -> Unit,
    val width: Dp = Dp.Unspecified,        // Unspecified면 컬럼끼리 균등 분배
)

@Composable
fun WheelPicker(
    title: String,
    onSaveClick: () -> Unit,
    columns: List<WheelPickerColumnState>,
    modifier: Modifier = Modifier,
    columnSpacing: Dp = 28.dp,
    visibleCount: Int = 5,
)
```

```kotlin
// WheelPickerColumn.kt (internal)
@Composable
internal fun WheelPickerColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    itemHeight: Dp = 48.dp,
)
```

```kotlin
// WheelPickerVariants.kt
@Composable
fun WheelPickerSingle(
    title: String,
    onSaveClick: () -> Unit,
    column: WheelPickerColumnState,
    modifier: Modifier = Modifier,
)

@Composable
fun WheelPickerDouble(
    title: String,
    onSaveClick: () -> Unit,
    first: WheelPickerColumnState,
    second: WheelPickerColumnState,
    modifier: Modifier = Modifier,
    columnSpacing: Dp = 64.dp,
)

@Composable
fun WheelPickerTriple(
    title: String,
    onSaveClick: () -> Unit,
    first: WheelPickerColumnState,
    second: WheelPickerColumnState,
    third: WheelPickerColumnState,
    modifier: Modifier = Modifier,
    columnSpacing: Dp = 40.dp,
)
```

- `List<String>` 채택 이유: designsystem은 도메인(숫자 포맷, 시진 라벨 조합)을 몰라야 하며, 문자열 리스트가 가장 테스트하기 쉽고 재사용 가능한 경계다. (사용자 승인됨 — 대안이었던 `제네릭<T> + itemLabel 람다`는 컬럼마다 다른 T를 쓸 때 타입 복잡도가 늘어 기각.)
- `columns.size`(개수)와 각 `WheelPickerColumnState.items`(데이터)가 곧 "피그마별 파라미터"이며, `WheelPicker` 하나가 이를 조합한다.
- 3개 wrapper는 컬럼 개수/기본 spacing만 고정하고 나머지(상태, 콜백, 라벨)는 그대로 hoist — 여전히 완전 stateless.

### 4.3 시각/모션 재현

- **스크롤 물리**: `LazyColumn` + `rememberSnapFlingBehavior`, 원본 스니펫의 스냅 로직 재사용.
- **드럼롤 효과**: 스크롤 중 `graphicsLayer { rotationX; alpha; scaleX; scaleY }` 로 곡면 효과 유지(원본과 동일 메커니즘).
- **정지 상태 텍스트 스타일** — Figma는 연속 보간이 아닌 3단계 이산 스타일이므로 `abs(distanceFromCenter)` 기준으로 분기:
  | distance | 타이포 토큰 | 컬러 토큰 |
  |---|---|---|
  | 0 (중심) | `body1Medium` (18sp) | `black` |
  | 1 | `body2Regular` (16sp) | `gray700` |
  | ≥2 | `body3Regular` (14sp) | `gray400` |
- **하이라이트 필**: `WheelPicker`가 컬럼 Row 전체 뒤에 `Box`(`primary50`, `RoundedCornerShape(8.dp)`)를 한 번만 그린다(멀티 휠에서도 모든 컬럼을 가로지르는 하나의 필 — Figma와 동일하게 컬럼별이 아님).
- **카드 크롬**: white bg, `RoundedCornerShape(12.dp)`, `shadow(...)`로 Figma의 `0px 0px 10px rgba(0,0,0,0.05)` 근사, padding(top 16 / bottom 40 / horizontal 30), 섹션 간 28dp gap.
- **핸들바**: SVG 에셋 대신 단순 `Box`(rounded gray line, 42dp x 4dp 근사)로 대체 — 에셋 파이프라인 불필요, `ic_/img_` 네이밍 규칙 대상 아님.
- **색상/타이포**: 전부 `LocalTodakunColor.current` / `LocalTodakunTypography.current` 토큰 사용 (rule 40 — 하드코딩 금지).
- **직접입력 제거**: 원본의 탭→`BasicTextField` 전환 로직은 제거. 항목 탭 시 해당 인덱스로 스크롤 이동만 수행.

### 4.4 트레이드오프

- 아이템 슬롯 높이를 모든 distance에서 **균일(48dp)** 하게 유지하기로 함 — Figma는 중심 행(하이라이트 필, padding 12/12)과 주변 행의 실측 높이가 다르지만(28~57dp 편차), `LazyColumn` 스냅 물리를 단순하게 유지하려면 아이템 높이가 균일해야 한다. 대신 텍스트 스타일(폰트 크기)과 필 배경으로 시각적 강조를 표현해 근접하게 재현한다. 픽셀 단위 오차는 구현 후 디자인 QA에서 조정 가능.
- 3개 wrapper composable을 도메인 이름(예: `BirthDateWheelPicker`)이 아닌 컬럼 개수 기반 이름(`WheelPickerSingle/Double/Triple`)으로 정함 — designsystem 레벨 재사용성을 위해 도메인에 묶지 않음.

## 5. 데이터 흐름

이 컴포넌트는 순수 Presentation(디자인 시스템) 레이어이며 MVI/UseCase와 무관하다. 호출부(추후 feature 레이어)가 다음을 책임진다:

```
호출부(feature) 상태(Int/시진 등)
  → List<String> 포맷 (예: 8 -> "08", 시진 enum -> "자시 (子時): 23:30 ~ 01:29")
  → WheelPickerColumnState(items, selectedIndex, onSelectedIndexChange)
  → WheelPicker/WheelPickerSingle/Double/Triple 렌더
  ← onSelectedIndexChange(newIndex) 콜백으로 인덱스 변경 보고
  ← onSaveClick 콜백으로 저장 트리거
```

## 6. 파일 변경 계획 (구현 체크리스트)

- [ ] `core/designsystem/build.gradle.kts` — `androidTestImplementation(libs.androidx.compose.ui.test.junit4)`, `debugImplementation(libs.androidx.compose.ui.test.manifest)`, `androidTestImplementation(libs.androidx.junit)` 등 테스트 의존성 배선
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerColumn.kt` — 단일 휠 원시 컴포넌트 신규 작성
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/WheelPicker.kt` — Base 컴포저블 + `WheelPickerColumnState` 신규 작성
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerVariants.kt` — `WheelPickerSingle`/`Double`/`Triple` + `@Preview` 3종 신규 작성
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerColumnTest.kt` — 신규
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerTest.kt` — 신규

## 7. 테스트 계획

- Compose UI 테스트 대상 (`core/designsystem/src/androidTest`):
  - [ ] `WheelPickerColumn`: 초기 `selectedIndex`의 항목이 중앙(하이라이트) 스타일로 표시되는가
  - [ ] `WheelPickerColumn`: 중앙이 아닌 항목을 탭하면 `onSelectedIndexChange`가 해당 인덱스로 정확히 1회 호출되는가
  - [ ] `WheelPicker`: N개 컬럼이 각각 독립적으로 상태를 보고하는가(한 컬럼 조작이 다른 컬럼에 영향 없음)
  - [ ] `WheelPicker`: "저장" 탭 시 `onSaveClick` 호출
  - [ ] `WheelPickerSingle`/`Double`/`Triple`: 각각 올바른 컬럼 개수로 렌더되는지 스모크 테스트
- 회귀 방지를 위해 `visibleCount`가 짝수일 때 등 잘못된 입력에 대한 방어(원본의 `require(visibleCount % 2 == 1)`)도 유지하고 실패 케이스를 테스트에 포함.

## 8. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] 카드 그림자(`shadow(0px 0px 10px rgba(0,0,0,0.05))`)를 Compose `Modifier.shadow`로 근사할 때 Figma와 시각적으로 얼마나 일치하는지는 구현 후 스크린샷 비교로 확인 필요.
- [ ] `WheelPicker_multi`의 분(minute) 컬럼이 Figma 목업에는 `00`, `30`만 노출되어 있어(30분 간격 추정) — 실제 데이터 규칙(0~59 전체 vs 30분 간격)은 feature 통합 시점에 확정 필요. 이번 작업은 컴포넌트 자체만 다루므로 `items: List<String>`을 그대로 받는 구조로 어느 쪽이든 대응 가능.
