# 설계 문서 — #14 [Feat] Divider, Popover, Checkbox, Badge, Chip 구현

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 전 **사람이 검토·수정하는 게이트**다.
> 자유롭게 수정해도 되고, 수정 후 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #14 [Feat] Divider, Popover, Checkbox, Badge, Chip 구현
- **작성**: 설계 에이전트 (Opus) / 2026-07-16
- **상태**: `승인됨(구현 가능)`
- **관련 모듈**: `core:designsystem` (단일 모듈, 순수 Compose UI 컴포넌트)

---

## 1. 문제 정의 / 목표

`core:designsystem`에 재사용 가능한 5개 원자(atom) 컴포넌트(Divider, Popover, Checkbox, Badge, Chip)를 Figma 스펙에 맞춰 구현한다. 모든 색/타이포/치수는 `core:designsystem`의 테마 토큰(`TodakunTheme.colors`, `TodakunTheme.typography`)에 대응시키고 하드코딩하지 않는다(`.claude/rules/40`). domain/data/presentation 레이어는 관련 없다.

기존 형제 브랜치(`feat/13-textfield`, `feat/10-wheel-picker`) 설계 원칙을 승계한다: **Figma variant를 그대로 옮기지 않고, `checked`/`type` 같은 상태·속성 파라미터로부터 시각 상태를 파생**시킨다.

### 완료 기준 (Acceptance Criteria)

**Divider**
- [x] `TodakunDivider`가 1px(구분선, `gray100`)와 10px(섹션 구분, `gray25`) 두 시각을 지원한다.
- [x] 색/두께가 토큰에서 파생되며 하드코딩이 없다. 기본은 `fillMaxWidth`.

**Popover**
- [x] `TodakunPopover`가 슬롯(`content: @Composable`)을 받는 범용 서페이스 컨테이너다.
- [x] 배경 `white`, radius 12, 내부 padding 8, 그림자(black 8%, blur 10, offset 0,0 근사)를 렌더한다.
- [x] 내부 필터 아이템 목록(지역 평수 등)은 구현 범위에 **포함하지 않는다**.

**Checkbox**
- [x] `TodakunCheckbox(checked, onCheckedChange, ...)`가 `checked` 값으로부터 On/Off 시각을 파생한다.
- [x] On: 20x20, radius 6, `primary600` 배경 + 흰색 체크 아이콘. Off: `white` 배경 + `gray300` 1px 테두리.
- [x] 클릭 시 `onCheckedChange(!checked)`가 정확히 1회 호출된다. `enabled=false`면 클릭 무시.

**Badge**
- [x] `TodakunBadge(text, type, ...)`가 6종 색상 타입(green/yellow/pink/purple/blue/gray)을 지원한다.
- [x] 각 타입의 배경/텍스트 색이 아래 표 토큰과 일치. radius 6, padding (h6, v3), 텍스트 `caption2SemiBold`.

**Chip**
- [x] `TodakunChip(text, ...)`가 pill(radius 100), `primary700` 배경, 흰색 `body3Medium` 텍스트를 렌더한다.
- [x] padding (h10, v5). (미선택 상태는 8절 미해결 질문 참조.)

**공통**
- [x] 각 컴포넌트에 `@Preview` 함수가 존재하고 `TodakunTheme`로 감싼다.
- [x] `./gradlew :core:designsystem:test` 및 관련 androidTest가 통과한다.

---

## 2. 범위

- **포함**: 위 5개 컴포넌트 Composable, Preview, 상태별 렌더링 테스트, Checkbox 체크 아이콘 벡터 드로어블 1개.
- **제외(Non-goals)**:
  - Popover 내부의 필터 아이템/Cell/Scroll Bar 등 구체 콘텐츠(예시일 뿐 이슈 범위 아님).
  - Popover의 앵커링/위치 계산·팝업 트리거 로직(순수 서페이스 컨테이너만 제공).
  - Chip의 미선택(unselected)·비활성 상태(디자인 미확인).
  - domain/data/presentation 레이어 변경.

---

## 3. 참고 스펙 (Figma `bLZr7Nh53PmRHuEjX7gNco`)

> 색상은 `theme/Color.kt` 토큰명으로 매칭. `~`는 근사치. 최상위 COMPONENT_SET의 stroke `#9747FF`는 Figma 자동 아웃라인이므로 **무시**.

### 3.1 Divider
| 컴포넌트 | node-id | size | fill(토큰) |
|---|---|---|---|
| divider_1px | 1318:13019 | 393×1 | `#F1F1F1` → `gray100` |
| divider_10px | 1318:13011 | 393×10 | `#FAFAFA` → `gray25` |

두께와 색이 **함께 변한다**(1px=gray100, 10px=gray25). 별개의 두 COMPONENT.

### 3.2 Popover (node 1417:22826)
- 컨테이너: `layout=HORIZONTAL`, spacing 4, padding 8(전방향), radius 12, fill `#FFFFFF` → `white`.
- effect: `DROP_SHADOW blur=10, color #000000 a=0.08` → `black` 8%, offset (0,0).
- 내부(범위 외): Item 텍스트 15sp/Pretendard Medium 등.

### 3.3 Checkbox (COMPONENT_SET 385:4002, `Status`=On/Off)
| variant | size | radius | fill | stroke |
|---|---|---|---|---|
| On | 20×20 | 6 | `#7F73EA` → `primary600` | — |
| Off | 20×20 | 6 | `#FFFFFF` → `white` | `#DCDCDC` → `gray300`, 1px |
- On 내부: `check_line` 16×16, 흰색(`#FFFFFF` → `white`) 체크 vector.

### 3.4 Badge (COMPONENT_SET 390:1612, `Property 1`=6종)
- 공통: `layout=HORIZONTAL`, padding (L6,T3,R6,B3), radius 6, 텍스트 Pretendard **weight 600, size 11, lineHeight 14** → `caption2SemiBold`(11sp/14.3sp SemiBold). letterSpacing ≈ -0.0011(무시 수준).

| type | 배경 fill(토큰) | 텍스트 fill(토큰) | 예시 텍스트 |
|---|---|---|---|
| yellow | `#FFF2DE` → `orange100` | `#A85F00` → `orange800` | 건강 |
| green | `#D5F1EE` → `teal100` | `#127065` → `teal800` | 직장 |
| pink | `#FEEAFB` → `pink100` | `#87507C` → `pink800` | 연애 |
| purple | `#E8E2FC` → `primary100` | `#4545B8` → `primary800` | 관계 |
| blue | `#E7ECFF` → `sky100` | `#4658A8` → `sky800` | 금전 |
| gray | `#EFF2F8` → `coolGray100` | `#8F9AAD` → `coolGray500` | category |

### 3.5 Chip (COMPONENT 1460:19914, `chip2`)
- size 45×30(hug), `layout=HORIZONTAL`, padding (L10,T5,R10,B5), counterAlign CENTER, radius 100, fill `#5757D7` → `primary700`.
- 텍스트 `#FFFFFF` → `white`, Pretendard **weight 500, size 14, lineHeight 20**, align CENTER → `body3Medium`(14sp/18.2sp Medium). lineHeight가 20 vs 18.2로 미세 차이(무시 수준). letterSpacing ≈ -0.0014.
- **단일 variant만 존재**(미선택 상태 미확인 → 8절).

---

## 4. 설계 및 실행 계획

### 4.1 공통 원칙
- 파일 위치: `core/designsystem/src/main/java/com/kikidan/designsystem/component/Todakun<Name>.kt` (플랫 `component/` — `feat/13-textfield`의 `TodakunTextField.kt`/`TodakunSelectField.kt` 관례를 따름. 사람 검토 결정).
- 색/타이포는 `val colors = TodakunTheme.colors` / `TodakunTheme.typography`로만 접근. 하드코딩 금지(`rules/40`).
- 리컴포지션 안정성(`rules/40` P2): 파라미터는 `String`/`Boolean`/`enum` 등 stable 타입 우선. 콜백은 `TodakunCheckbox`처럼 필요한 곳만 노출.
- 모든 컴포넌트에 `modifier: Modifier = Modifier`를 첫 옵션 파라미터로 둔다.

### 4.2 컴포넌트별 API 설계

**Divider** — 두께·색이 함께 변하므로 의미 단위 enum으로 파생(추천안):
```kotlin
enum class TodakunDividerType { Line, Section }   // Line=1px/gray100, Section=10px/gray25

@Composable
fun TodakunDivider(
    modifier: Modifier = Modifier,
    type: TodakunDividerType = TodakunDividerType.Line,
)
```
- 근거: 1px/10px가 색까지 동반 변경되므로 "두께만 받는 raw 파라미터"보다 의미 있는 타입 파생이 오용을 줄인다. (대안: `TodakunDivider(thickness, color)` 저수준 — 유연하나 토큰 조합 강제 불가. → 8절에서 사람 선택.)

**Popover** — 슬롯 기반 서페이스:
```kotlin
@Composable
fun TodakunPopover(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```
- `white` 배경 + radius 12 `clip` + padding 8 + 그림자. 그림자는 `Modifier.shadow(elevation, RoundedCornerShape(12.dp), ambientColor/spotColor = colors.black.copy(alpha=0.08f))`로 근사(offset 0,0/blur 10 정확 재현은 제약 → 8절).

**Checkbox** — `checked`에서 On/Off 파생:
```kotlin
@Composable
fun TodakunCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```
- 20dp Box, radius 6. `checked` → `primary600` 배경 + `ic_check_line`(흰색) 표시. `!checked` → `white` 배경 + `gray300` 1dp border. `Modifier.clickable(enabled)`에서 `onCheckedChange(!checked)`. 접근성: `Modifier.toggleable`/`Role.Checkbox` 검토.

**Badge** — `type` enum으로 색 파생:
```kotlin
enum class TodakunBadgeType { Green, Yellow, Pink, Purple, Blue, Gray }

@Composable
fun TodakunBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: TodakunBadgeType = TodakunBadgeType.Gray,
)
```
- 내부 `when(type)`로 (배경, 텍스트) 토큰 페어 결정(3.4 표). radius 6, padding (h6,v3), `caption2SemiBold`, hug 크기.

**Chip** — 확정된 selected 시각만 구현하되 확장 가능한 시그니처:
```kotlin
@Composable
fun TodakunChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
)
```
- pill(radius 100), `primary700` 배경, `white` `body3Medium`, padding (h10,v5). `onClick`이 있으면 `clickable`. (미선택 상태 추가 시 `selected: Boolean` 확장 여지 — 지금은 추측 구현 금지, 8절.)

### 4.3 필요한 리소스
- `ic_check_line.xml` (벡터 드로어블): Checkbox On의 흰색 체크 마크. `ic_` 접두사 = SVG/벡터(`rules/40` 준수). tint를 `white`로 적용. Figma `check_line` 노드 export 필요(구현자가 Figma에서 SVG 추출).

---

## 5. 데이터 흐름

해당 없음 — 순수 무상태(stateless) 프리젠테이션 컴포넌트. 상태는 호출부가 hoisting한다(Checkbox `checked`, Chip `onClick`). UseCase/Repository/DataSource 경유 없음.

---

## 6. 파일 변경 계획 (구현 체크리스트)

- [x] `core/designsystem/src/main/java/com/kikidan/designsystem/component/TodakunDivider.kt` — `TodakunDivider` + `TodakunDividerType` + Preview
- [x] `core/designsystem/src/main/java/com/kikidan/designsystem/component/TodakunPopover.kt` — 슬롯 서페이스 컨테이너 + Preview
- [x] `core/designsystem/src/main/java/com/kikidan/designsystem/component/TodakunCheckbox.kt` — checked 파생 체크박스 + Preview(On/Off)
- [x] `core/designsystem/src/main/java/com/kikidan/designsystem/component/TodakunBadge.kt` — `TodakunBadge` + `TodakunBadgeType`(6종) + Preview
- [x] `core/designsystem/src/main/java/com/kikidan/designsystem/component/TodakunChip.kt` — pill chip + Preview
- [x] `core/designsystem/src/main/res/drawable/ic_check_line.xml` — Checkbox 체크 벡터(흰색 tint)
- [x] (테스트) `core/designsystem/src/androidTest/java/com/kikidan/designsystem/component/TodakunCheckboxTest.kt`
- [x] (테스트) `.../component/TodakunBadgeTest.kt`
- [x] (테스트) `.../component/TodakunChipTest.kt`
- [x] (테스트) `.../component/TodakunDividerTest.kt`

> 아키텍처 확인(`rules/00`): 모두 `core:designsystem` 내부. feature/domain/data 미참조. 레이어 위반 없음.

---

## 7. 테스트 계획

기존 `feat/10-wheel-picker`의 androidTest 패턴 승계: `createComposeRule` + `TodakunTheme` 래핑 + 한국어 백틱 테스트명 + `onNodeWith*`/`assert*`.

- Compose UI(androidTest) 대상:
  - [x] **Checkbox**: (a) `checked=false`에서 클릭 시 `onCheckedChange(true)`가 정확히 1회, (b) `enabled=false`면 클릭해도 콜백 미호출, (c) On/Off 상태별 렌더(체크 아이콘 표시/미표시) — `testTag` 또는 `Role.Checkbox` 기반 `assertIsOn/Off`.
  - [x] **Badge**: 각 `type`에 대해 전달한 `text`가 표시되는지(`onNodeWithText`). (색 검증은 스냅샷 부재로 렌더 존재 여부까지.)
  - [x] **Chip**: `text` 표시 및 `onClick` 제공 시 클릭 콜백 1회 호출.
  - [x] **Divider**: `Line`/`Section` 각각 `testTag`로 렌더/높이 존재 확인(정밀 픽셀 검증은 생략).
- Preview: 각 파일에 상태/타입을 망라한 `@Preview`(예: Badge 6종, Checkbox On·Off, Divider 2종) — 시각 회귀의 1차 방어선.
- 유닛 테스트(`src/test`): 순수 UI라 로직 유닛 테스트 대상은 없음. 검증은 androidTest 중심.
- 명령어: `./gradlew :core:designsystem:test` (유닛), Compose 테스트는 `:core:designsystem:connectedDebugAndroidTest`(에뮬레이터 필요).

---

## 8. 리스크 / 미해결 질문 (사람 확인 필요)

### 사람 검토 완료 (결정됨)
- [x] **[Chip 미선택 상태]** → **selected 시각만 구현**. unselected/비활성 상태는 Figma 미확인 상태이므로 이번 범위에서 추측 구현하지 않는다. 필요 시 별도 확인 후 `selected: Boolean` 파라미터로 확장.
- [x] **[Divider 파라미터화]** → **의미 단위 `TodakunDividerType.{Line, Section}` enum** 채택.
- [x] **[패키지 관례]** → **플랫 `component/`** 채택 (`feat/13-textfield` 관례를 따름). 4.1/6절 파일 경로에 반영 완료.

### 구현 단계에서 판단 (블로킹 아님)
- [x] **[Popover 그림자 재현도]** Figma는 blur 10 / offset (0,0) / black 8%. Compose `Modifier.shadow`는 elevation 기반이라 offset 0 그림자를 정확히 재현하기 어려움 → `Modifier.shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), ambientColor = spotColor = colors.black.copy(alpha = 0.08f))`로 근사 구현했다. 완전히 동일하지는 않으나(offset 0 그림자를 elevation 기반 API로 정확히 재현하는 것은 제약), 설계 문서에 명시된 허용 오차 범위. 시각 차이가 크면 이후 커스텀 draw로 개선 검토.
- [x] **[Checkbox 체크 아이콘 소스]** Figma API로 벡터(`Subtract`, 13×10, fill white) SVG를 직접 export하여 `designs/assets/issue-14-divider-popover-checkbox-badge-chip/ic_check_line.xml`(Android VectorDrawable로 변환 완료)에 준비해 두었다. 구현 시 이 파일을 `core/designsystem/src/main/res/drawable/ic_check_line.xml`로 그대로 복사한다.
- [x] **[Popover 스코프]** 앵커 위치/팝업 트리거는 범위 제외(순수 컨테이너). 실제 팝업 동작이 필요하면 별도 이슈로 분리. `TodakunPopover`는 슬롯(`content: @Composable ColumnScope.() -> Unit`)만 받는 서페이스 컨테이너로 구현했고, 앵커링/트리거 로직은 포함하지 않았다.
