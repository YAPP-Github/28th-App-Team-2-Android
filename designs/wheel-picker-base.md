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
  - [ ] `WheelPicker`(휠 원시 컴포넌트)이 stateless로 구현되어 있다.
  - [ ] `WheelPicker`(Base)가 `columns: List<WheelPickerColumnState>`를 받아 1~N개 휠을 조합할 수 있다.
  - [ ] `SajuBirthTimeWheelPicker` / `TimeWheelPicker` / `BirthDateWheelPicker` 3개 wrapper가 각각 Figma 노드 383:1630 / 1417:23096 / 383:1347 을 재현한다.
  - [ ] 색상·타이포는 전부 `core:designsystem` 테마 토큰을 사용한다(하드코딩 없음).
  - [ ] Compose UI 테스트가 추가되고 통과한다.

## 2. 범위

- 포함:
  - `core:designsystem`에 새 `component/wheelpicker` 패키지 추가
  - `WheelPickerColumn`, `WheelPicker`(Base), `WheelPickerColumnState`
  - `SajuBirthTimeWheelPicker`, `TimeWheelPicker`, `BirthDateWheelPicker` wrapper + `@Preview`
  - `core:designsystem`에 Compose UI 테스트 인프라(androidTest) 배선
- 제외(Non-goals):
  - SajuBirthTimeWheelPicker의 가운데 항목 탭 → 직접 입력(TextField) 기능, 나머지 WheelPicker에는 직접 입력 추가
  - 실제 기능(feature) 화면에서의 통합(생년월일 입력 화면 등) — 이번 작업은 designsystem 컴포넌트까지만
  - 숫자 포매팅("08"), 시진 라벨 조합 등 도메인 로직 — 전부 호출부(feature) 책임
  - 접근성(TalkBack) 세부 튜닝

## 3. 참고 스펙
- TimeWheelPicker 인수 조건
  - '시'에 24 이상의 숫자가 들어오면 ➔ 23으로 치환, 
  - '분'에 60 이상의 숫자가 들어오면 ➔ 59로 치환
  - 3자리 이상 입력 시에는 앞의 2자리만 자른 후 위 규칙을 적용해 주세요. (예: 9999 입력 시 23시 59분으로 보정)
- BirthDateWheelPicekr 인수 조건
  - 생년월일 입력: 이 부분은 자동 보정이 까다로울 수 있으니(예: 2월 30일 등), 존재하지 않는 날짜 입력 후 저장 시 "올바른 날짜를 입력해 주세요."라는 토스트 메시지가 뜨도록 예외 처리

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
  WheelPickerVariants.kt — SajuBirthTimeWheelPicker/TimeWheelPicker/BirthDateWheelPicker + @Preview 3종
```

### 4.2 API

```kotlin
// WheelPicker.kt
@Immutable
data class WheelPickerColumnState(
    val items: List<String>,               // 이미 포맷된 표시용 라벨
    val selectedIndex: Int,
    val width: Dp = Dp.Unspecified,        // Unspecified면 컬럼끼리 균등 분배
)

@Composable
fun WheelPicker(
    title: String,
    onSaveClick: () -> Unit,
    columns: List<WheelPickerColumnState>,
    onWheelPickerColumnSelected: (columnIndex: Int, selectedIndex: Int) -> Unit,
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
    directInputEnabled: Boolean = false,
    maxInputDigits: Int = 2,
    onDirectInputCommitted: (rawDigits: String) -> Unit = {},
)
```

직접입력은 원본 스니펫과 동일하게 "가운데 항목 탭 → BasicTextField 전환 → 포커스 아웃/IME Done 시 커밋" 메커니즘을 재사용한다. `WheelPickerColumn`은 타이핑 중 `maxInputDigits`자리로 길이만 제한할 뿐 값 해석은 하지 않고, 커밋 시 raw 숫자 문자열을 그대로 콜백으로 올려보낸다 — 범위 클램프(23/59 등)나 존재하지 않는 날짜 판정 같은 "의미"는 상위 wrapper의 몫이다.

```kotlin
// WheelPicker.kt (Base)
@Composable
fun WheelPicker(
    title: String,
    onSaveClick: () -> Unit,
    columns: List<WheelPickerColumnState>,
    onWheelPickerColumnSelected: (columnIndex: Int, selectedIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    columnSpacing: Dp = 28.dp,
    visibleCount: Int = 5,
    directInputEnabled: Boolean = false,
    onColumnDirectInputCommitted: (columnIndex: Int, rawDigits: String) -> Unit = { _, _ -> },
)

@Immutable
data class WheelPickerColumnState(
    val items: List<String>,
    val selectedIndex: Int,
    val width: Dp = Dp.Unspecified,
    val maxInputDigits: Int = 2,   // 컬럼별 자리수 상한 (year=4, 나머지=2)
)
```

```kotlin
// WheelPickerVariants.kt
@Composable
fun SajuBirthTimeWheelPicker(
    title: String,
    onSaveClick: () -> Unit,
    column: WheelPickerColumnState,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) // 문자열 라벨 컬럼이라 직접입력 없음 — Base를 directInputEnabled=false로 호출

@Composable
fun TimeWheelPicker(
    title: String,
    hour: Int,                       // 0..23
    minute: Int,                     // 0..59
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    columnSpacing: Dp = 64.dp,
)

@Composable
fun BirthDateWheelPicker(
    title: String,
    year: Int,
    month: Int,                      // 1..12
    day: Int,                        // 1..31 (표시 범위 고정, 실존 여부는 저장 시 검증)
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onSaveClick: () -> Unit,         // 유효한 날짜일 때만 호출됨
    modifier: Modifier = Modifier,
    yearRange: IntRange = 1900..2100,
    columnSpacing: Dp = 40.dp,
)
```

`TimeWheelPicker`/`BirthDateWheelPicker`는 `WheelPickerColumnState`를 노출하지 않고 명시적 `Int` 파라미터(및 range)를 받는다 — 이 두 컴포넌트는 이미 "시/분", "년/월/일"이라는 구체적 의미를 가진 이름이므로, 내부에서 0-기반 `items` 문자열 리스트를 직접 생성하고 값↔인덱스가 1:1로 대응돼 클램프 로직을 스스로 가질 수 있다(제네릭 `WheelPicker`/`WheelPickerColumn`은 여전히 도메인을 모른다).

- `List<String>` 채택 이유: `WheelPicker`/`WheelPickerColumn`(Base·원시 컴포넌트)은 도메인(숫자 포맷, 시진 라벨 조합, 달력 규칙)을 몰라야 하며, 문자열 리스트가 가장 테스트하기 쉽고 재사용 가능한 경계다. (대안이었던 `제네릭<T> + itemLabel 람다`는 컬럼마다 다른 T를 쓸 때 타입 복잡도가 늘어 기각.) 단, `TimeWheelPicker`/`BirthDateWheelPicker`는 "시/분", "년/월/일"이라는 구체적 의미가 이름에 이미 박혀 있는 wrapper이므로 예외적으로 `Int` 파라미터를 받고 내부에서 `List<String>`으로 변환한다 — Base 원칙(도메인 무지)은 유지하되 named wrapper는 실용성을 우선.
- `columns.size`(개수)와 각 `WheelPickerColumnState.items`(데이터)가 곧 "피그마별 파라미터"이며, `WheelPicker` 하나가 이를 조합한다.
- **콜백은 컬럼별 embedded 람다가 아닌 단일 `onWheelPickerColumnSelected(columnIndex, selectedIndex)`로 통합** (사용자 피드백 반영). 이유:
  1. `BirthDateWheelPicker`처럼 컬럼 간 의존성(월 변경 시 일자 유효 범위 재계산, 윤년 2월 등)이 있을 때, 재계산 로직이 하나의 핸들러에 모이므로 여러 클로저에 흩어지지 않는다.
  2. `WheelPickerColumnState`에서 함수 프로퍼티가 빠져 순수 데이터 값이 되어 `@Immutable` 구조적 동등성이 안정적이다(클로저 참조 불일치로 인한 불필요한 리컴포지션 방지).
  - `WheelPicker`(Base)는 `columnIndex`의 의미(어느 휠인지)를 모르므로 그대로 raw index를 전달한다. `SajuBirthTimeWheelPicker`(단일 휠)는 index가 항상 0이라 의미가 없으므로 `onSelectedIndexChange: (Int) -> Unit`로 단순화. `TimeWheelPicker`/`BirthDateWheelPicker`는 각각 `columnIndex` 매핑을 문서화(예: `BirthDateWheelPicker` → 0=년/1=월/2=일)한다.
  - 호출부 예시(생년월일, 윤년/월별 일수 반영):
    ```kotlin
    onWheelPickerColumnSelected = { columnIndex, selectedIndex ->
        val newYear = if (columnIndex == 0) years[selectedIndex] else year
        val newMonth = if (columnIndex == 1) months[selectedIndex] else month
        val maxDay = daysInMonth(newYear, newMonth) // 윤년 2월 등 반영
        val newDay = if (columnIndex == 2) days[selectedIndex] else day.coerceAtMost(maxDay)
        // year/month/day 상태 갱신 → 다음 recomposition에서 day 컬럼 items/selectedIndex 재계산
    }
    ```
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
- **직접입력 (컬럼별 선택적)**: `SajuBirthTimeWheelPicker`는 문자열 라벨 컬럼이라 직접입력 없음(탭 시 스크롤 이동만). `TimeWheelPicker`/`BirthDateWheelPicker`는 원본 스니펫과 동일한 탭→`BasicTextField`→커밋 메커니즘을 유지한다.
  - 타이핑 중: `maxInputDigits`자리로 길이만 제한(원본의 `.take(maxLen)`과 동일 메커니즘) — 3자리 이상은애초에 입력 자체가 막혀 "앞 2자리만 자른 것"과 동일한 효과.
  - 커밋 시(포커스 아웃/IME Done): `TimeWheelPicker` — hour는 `min(typed, 23)`, minute는 `min(typed, 59)`로 클램프 후 해당 인덱스로 스크롤. `BirthDateWheelPicker`의 year/month/day는 클램프 없이 그대로 반영(단, month는 1..12로, day는 1..31로 표시 범위만 제한) — 실존 여부는 저장 시점에 검증.
  - `BirthDateWheelPicker`의 "저장" 탭 시: `runCatching { LocalDate.of(year, month, day) }` 로 날짜 존재 여부를 검증. 실패하면 `onSaveClick`을 호출하지 않고 `Toast.makeText(context, "올바른 날짜를 입력해 주세요.", Toast.LENGTH_SHORT).show()`로 즉시 피드백. 성공하면 `onSaveClick()` 호출. (calendar 계산은 JDK `java.time`만 사용 — 앱 도메인/UseCase 의존 없음, designsystem이 domain 모듈을 참조하지 않는다는 rule 00 원칙 유지.)

### 4.4 트레이드오프

- 아이템 슬롯 높이를 모든 distance에서 **균일(48dp)** 하게 유지하기로 함 — Figma는 중심 행(하이라이트 필, padding 12/12)과 주변 행의 실측 높이가 다르지만(28~57dp 편차), `LazyColumn` 스냅 물리를 단순하게 유지하려면 아이템 높이가 균일해야 한다. 대신 텍스트 스타일(폰트 크기)과 필 배경으로 시각적 강조를 표현해 근접하게 재현한다. 픽셀 단위 오차는 구현 후 디자인 QA에서 조정 가능.
- 3개 wrapper composable은 도메인 이름(`SajuBirthTimeWheelPicker`/`TimeWheelPicker`/`BirthDateWheelPicker`)으로 정함(사용자 결정) — 컬럼 구성(개수·spacing·width)은 designsystem이 고정하지만, 이름 자체는 실제 사용 맥락을 드러내는 편이 낫다고 판단. 단, `items`/`width` 등 데이터·포맷 로직은 여전히 호출부 책임이며 wrapper 내부에 도메인 로직은 없다.

## 5. 데이터 흐름

이 컴포넌트는 순수 Presentation(디자인 시스템) 레이어이며 MVI/UseCase와 무관하다. 호출부(추후 feature 레이어)가 다음을 책임진다:

```
[Base 경로 — WheelPicker/SajuBirthTimeWheelPicker]
호출부 상태 → List<String> 포맷(이미 완성된 라벨) → WheelPickerColumnState 구성
  → WheelPicker 렌더
  ← onWheelPickerColumnSelected(columnIndex, selectedIndex) 콜백으로 인덱스 변경 보고
  ← onSaveClick 콜백으로 저장 트리거

[TimeWheelPicker / BirthDateWheelPicker 경로 — 값 자체가 파라미터]
호출부 상태(Int: hour/minute 또는 year/month/day)
  → TimeWheelPicker/BirthDateWheelPicker에 그대로 전달
  → 내부에서 0-기반 List<String> 생성 + WheelPicker 위임(스크롤 선택 & 직접입력 모두 처리)
  ← onHourChange/onMinuteChange 또는 onYearChange/onMonthChange/onDayChange 콜백으로 새 Int 값 보고
     (스크롤이든 직접입력 커밋이든 동일한 콜백 — 내부에서 클램프까지 끝낸 최종 값)
  ← onSaveClick — BirthDateWheelPicker는 저장 탭 시 내부적으로 `LocalDate.of(year,month,day)` 유효성 검증 후
     유효할 때만 호출, 무효 시 컴포넌트 자체 Toast 표시하고 onSaveClick 미호출
```

## 6. 파일 변경 계획 (구현 체크리스트)

- [ ] `core/designsystem/build.gradle.kts` — `androidTestImplementation(libs.androidx.compose.ui.test.junit4)`, `debugImplementation(libs.androidx.compose.ui.test.manifest)`, `androidTestImplementation(libs.androidx.junit)` 등 테스트 의존성 배선
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerColumn.kt` — 단일 휠 원시 컴포넌트 신규 작성
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/WheelPicker.kt` — Base 컴포저블 + `WheelPickerColumnState` 신규 작성
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerVariants.kt` — `SajuBirthTimeWheelPicker`/`TimeWheelPicker`/`BirthDateWheelPicker` + `@Preview` 3종 신규 작성
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerColumnTest.kt` — 신규 (스크롤 선택 + 직접입력 커밋)
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/wheelpicker/WheelPickerTest.kt` — 신규
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/wheelpicker/TimeWheelPickerTest.kt` — 신규 (hour/minute 클램프 규칙)
- [ ] `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/wheelpicker/BirthDateWheelPickerTest.kt` — 신규 (무효 날짜 저장 시 Toast, onSaveClick 미호출)

## 7. 테스트 계획

- Compose UI 테스트 대상 (`core/designsystem/src/androidTest`):
  - [ ] `WheelPickerColumn`: 초기 `selectedIndex`의 항목이 중앙(하이라이트) 스타일로 표시되는가
  - [ ] `WheelPickerColumn`: 중앙이 아닌 항목을 탭하면 `onSelectedIndexChange`가 해당 인덱스로 정확히 1회 호출되는가
  - [ ] `WheelPicker`: N개 컬럼 각각을 조작하면 `onWheelPickerColumnSelected`가 올바른 `columnIndex`와 `selectedIndex`로 호출되는가(다른 컬럼과 혼동 없음)
  - [ ] `WheelPicker`: "저장" 탭 시 `onSaveClick` 호출
  - [ ] `SajuBirthTimeWheelPicker`/`TimeWheelPicker`/`BirthDateWheelPicker`: 각각 올바른 컬럼 개수로 렌더되는지 스모크 테스트
  - [ ] `WheelPickerColumn`: 직접입력 활성화 시 가운데 항목 탭 → TextField 전환, `maxInputDigits`자리 초과 입력 불가, 커밋 시 raw 문자열 콜백 호출
  - [ ] `TimeWheelPicker`: "24" 입력 시 hour 23으로 클램프, "60" 입력 시 minute 59로 클램프 (3자리 이상은 입력 자체가 2자리로 제한되어 동일 규칙 적용됨을 확인)
  - [ ] `BirthDateWheelPicker`: 존재하는 날짜(예: 2000-02-29 윤년) 저장 시 `onSaveClick` 호출, Toast 없음
  - [ ] `BirthDateWheelPicker`: 존재하지 않는 날짜(예: 2월 30일) 저장 시 `onSaveClick` 미호출 + "올바른 날짜를 입력해 주세요." Toast 노출
- 회귀 방지를 위해 `visibleCount`가 짝수일 때 등 잘못된 입력에 대한 방어(원본의 `require(visibleCount % 2 == 1)`)도 유지하고 실패 케이스를 테스트에 포함.

## 8. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] 카드 그림자(`shadow(0px 0px 10px rgba(0,0,0,0.05))`)를 Compose `Modifier.shadow`로 근사할 때 Figma와 시각적으로 얼마나 일치하는지는 구현 후 스크린샷 비교로 확인 필요.
- [ ] `WheelPicker_multi`의 분(minute) 컬럼이 Figma 목업에는 `00`, `30`만 노출되어 있으나, 이번 결정으로 `TimeWheelPicker`는 0~59 전체 분을 노출한다(직접입력으로 임의 분 입력 가능해야 하므로). Figma는 대표값만 보여준 것으로 해석.
- [ ] `BirthDateWheelPicker`의 day 휠은 항상 1~31 고정 노출(월별 일수에 맞춰 동적으로 줄이지 않음) — 존재하지 않는 조합(2월 30일 등)은 저장 시 Toast로만 안내. UX상 "애초에 못 고르게 막을지 vs 저장 시점에 안내할지"는 사람이 최종 확인.
- [ ] Toast는 `LocalContext.current` + `Toast.makeText`로 컴포넌트 내부에서 직접 발생시킨다 — Orbit `SideEffect` 경유가 아님(이 컴포넌트는 ViewModel과 무관한 순수 designsystem 컴포저블이므로). 추후 feature 통합 시 이 Toast를 Snackbar 등으로 대체하고 싶다면 API 확장(예: `onInvalidDate: (() -> Unit)? = null` 콜백으로 오버라이드)이 필요할 수 있음 — 이번 스코프에서는 기본 Toast로 확정.
