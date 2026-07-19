# 설계 문서 — #18 BottomNavigation 구현

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 전 **사람이 검토·수정하는 게이트**다.
> 자유롭게 수정해도 되고, 수정 후 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: [#18 [Feat] BottomNavigation 구현](https://github.com/YAPP-Github/28th-App-Team-2-Android/issues/18)
- **작성**: 설계 (Opus) / 2026-07-19
- **상태**: `승인됨(구현 가능)` (§8 질문 4건 사람 확정 반영)
- **관련 모듈**: `core:designsystem`

---

## 1. 문제 정의 / 목표

앱 하단에 고정되는 재사용 가능한 **BottomNavigation 컴포넌트**를 디자인 시스템(`core:designsystem`)에 구현한다.
4개 탭(운세 · 토닥이 · 행운 액션 · 마이)을 가지며, 선택된 탭은 채워진(on) 아이콘 + 진한 라벨,
비선택 탭은 외곽선(off) 아이콘 + 회색 라벨로 표시된다.

컴포넌트는 **상태를 갖지 않는(stateless) 프레젠테이셔널 컴포넌트**로, 선택 상태와 클릭 콜백을 외부에서 주입받는다.
(실제 네비게이션 라우팅 연결은 이슈 범위 밖 — `app`/`core:navigation`에서 별도 수행)

- 완료 기준(Acceptance Criteria):
  - [ ] `TodakunBottomNavigation` 컴포저블이 `core:designsystem`에 존재한다.
  - [ ] 4개 탭(운세/토닥이/행운 액션/마이)이 Figma와 동일한 순서·아이콘·라벨로 렌더링된다.
  - [ ] 선택 탭 = on 아이콘 + `caption3SemiBold` + `gray975`, 비선택 탭 = off 아이콘 + `caption3Medium` + `gray500`.
  - [ ] 탭 클릭 시 해당 탭 식별자로 `onItemSelected` 콜백이 호출된다.
  - [ ] 컨테이너 스펙 재현: 흰색 배경, 높이 56dp, 상단 좌우 라운드 24dp, 좌우 패딩 12dp, 상단 그림자.
  - [ ] 디자인 값은 `TodakunTheme` 토큰(색/타이포)을 사용하고 레이아웃 하드코딩을 최소화한다.
  - [ ] Compose UI 테스트 통과(선택 상태 표시 · 클릭 콜백 · 라벨 존재).

## 2. 범위

- **포함**:
  - 8개 아이콘 벡터 드로어블(on/off × 4탭) — Figma에서 SVG 추출 → Android VectorDrawable 변환.
  - 탭 모델(`TodakunNavItem` enum: 라벨 + on/off 아이콘 리소스).
  - `TodakunBottomNavigation` stateless 컴포저블 + `@Preview`.
  - 라벨 문자열 리소스, Compose UI 테스트, 테스트용 gradle 의존성 추가.
- **제외(Non-goals)**:
  - 실제 화면 라우팅/네비게이션 그래프 연결(Navigation 3 wiring) — 별도 이슈.
  - `MainActivity`/`Scaffold` 통합(데모 목적의 최소 연결은 선택).
  - iOS `HomeIndicator` 목업(Figma의 하단 검은 바) — 안드로이드는 시스템 제스처 바 사용, 컴포넌트에서 제외.
  - 다크 모드(현재 디자인/테마는 라이트 전용).

## 3. 참고 스펙 (Figma)

파일: `bLZr7Nh53PmRHuEjX7gNco`

- **Bottom_Navigation** (node `380:2848`) — 전체 컴포넌트
- 아이콘 컴포넌트 셋(각각 `Status=on` / `Status=off` variant):
  - `ic_navi_lucky` (`1075:14545`) — **운세** 탭
  - `ic_navi_ai` (`1075:14553`) — **토닥이** 탭
  - `ic_navi_action` (`1075:14559`) — **행운 액션** 탭
  - `ic_navi_my` (`1075:14563`) — **마이** 탭

### 추출된 스펙

| 항목 | 값 | 매핑 토큰 |
|---|---|---|
| 컨테이너 배경 | `#FFFFFF` | `TodakunTheme.colors.white` |
| 컨테이너 높이 | 56dp | (레이아웃 상수) |
| 상단 라운드 | top-left/right 24dp | (레이아웃 상수) |
| 좌우 패딩 | 12dp | (레이아웃 상수) |
| 상단 그림자 | offset (0, -4), blur 20, `rgba(0,0,0,0.06)` | Compose `shadow`/`drawBehind` |
| Contents 상단 패딩 | 4dp | (레이아웃 상수) |
| 탭 배치 | 4개 균등(weight 1f), 세로 정렬, 아이콘↔라벨 gap 4dp | — |
| 아이콘 크기 | 24×24dp | — |
| 라벨(선택) | Pretendard SemiBold 10sp / lh 13 / `#171717` | `caption3SemiBold` + `gray975` |
| 라벨(비선택) | Pretendard Medium 10sp / lh 13 / `#9A9A9A` | `caption3Medium` + `gray500` |
| 아이콘 색(선택) | `#171717` (+ 내부 흰색 컷아웃) | 벡터에 baked |
| 아이콘 색(비선택) | `#6B7486` | 벡터에 baked |

> **주의**: 비선택 시 **아이콘 색(`#6B7486`=coolGray600)** 과 **라벨 색(`#9A9A9A`=gray500)** 이 서로 다르다. 또한 on/off 아이콘은 단순 색 변경이 아니라 **서로 다른 형태(채움 vs 외곽선)** 이고 일부(`ic_navi_ai_on`)는 흰색 컷아웃을 포함하는 **다색 벡터**다. → 단일 tint로 처리 불가, on/off를 **별도 드로어블**로 둔다(§4 트레이드오프 참조).

## 4. 설계 및 실행 계획

### 4.1 위치 근거 (`.claude/rules/00-architecture.md`)
- 재사용 UI 컴포넌트이므로 `core:designsystem`에 둔다. feature 간 공유 가능, 레이어 침범 없음.
- 탭 식별 enum(`TodakunNavItem`)도 designsystem에 둔다. domain/navigation에 의존하지 않으므로 역방향 참조 없음. `app`이 enum ↔ 실제 라우트를 매핑한다.

### 4.2 공개 API
```kotlin
enum class TodakunNavItem(
    @StringRes val labelRes: Int,
    @DrawableRes val selectedIconRes: Int,
    @DrawableRes val unselectedIconRes: Int,
) {
    // enum 명은 아이콘 에셋명(ic_navi_*)에 정렬 (사람 확정: "ic_navi_lucky로 진행")
    LUCKY(R.string.bottom_nav_fortune, R.drawable.ic_navi_lucky_on, R.drawable.ic_navi_lucky_off),   // 운세
    AI(R.string.bottom_nav_todak, R.drawable.ic_navi_ai_on, R.drawable.ic_navi_ai_off),               // 토닥이
    ACTION(R.string.bottom_nav_lucky_action, R.drawable.ic_navi_action_on, R.drawable.ic_navi_action_off), // 행운 액션
    MY(R.string.bottom_nav_my, R.drawable.ic_navi_my_on, R.drawable.ic_navi_my_off),                  // 마이
}

@Composable
fun TodakunBottomNavigation(
    selectedItem: TodakunNavItem,
    onItemSelected: (TodakunNavItem) -> Unit,
    modifier: Modifier = Modifier,
    items: List<TodakunNavItem> = TodakunNavItem.entries,
)
```

### 4.3 내부 구조
- 루트: `Row`(또는 `Surface` + `Row`) — 흰 배경, 높이 56dp, 상단 라운드 24dp, 상단 그림자, 좌우 패딩 12dp, 상단 패딩 4dp.
  - 그림자: **`Modifier.dropShadow()`** 사용(사람 확정). `RoundedCornerShape(topStart=24dp, topEnd=24dp)` 셰이프에 offset (0,-4), blur(radius) 20dp, color `black` alpha 0.06 적용. (Compose BOM 2026.02.01 = `androidx.compose.ui.graphics.shadow.Shadow` + `Modifier.dropShadow(shape, shadow)` 사용 가능)
- 각 탭: `Modifier.weight(1f)` `Column`(가로 중앙, 세로 `spacedBy(4dp)`), `clickable`(리플 최소/기본) → `onItemSelected(item)`.
  - 아이콘: `Image(painter = painterResource(if selected on else off), contentDescription = null)` 24dp. (다색 baked 벡터이므로 `Icon`+tint 미사용, `Image` 사용)
  - 라벨: `Text(stringResource(item.labelRes), style = caption3SemiBold/Medium, color = gray975/gray500)`.

### 4.4 `.claude/rules/40-compose-and-resources.md` 준수
- 색/타이포는 `TodakunTheme.colors` / `TodakunTheme.typography` 토큰 사용(라벨). 레이아웃 상수(dp)는 `private val`/`object` 상수로 모아 하드코딩 산재 방지.
- 리컴포지션(P2): `onItemSelected`는 안정 람다로 hoist, `TodakunNavItem`은 enum(안정), 탭 루프에서 람다 캡처 최소화.
- 리소스 네이밍(`ic_` 접두사 = 벡터): `ic_navi_*_on.xml` / `ic_navi_*_off.xml` (VectorDrawable). ✅ 규칙 부합.

### 4.5 트레이드오프
- **on/off 별도 드로어블(baked color) vs 단일 아이콘 + tint**: on/off는 형태가 다르고 다색이라 tint 불가 → **별도 8개 드로어블**. 결과적으로 벡터 `fillColor`에 hex가 baked 되어 "색 토큰 하드코딩 금지"와 부분 상충하나, 아이콘은 Figma에서 export된 아트 에셋으로 테마 변경 대상이 아님. 라벨 등 tint 가능한 요소는 토큰을 사용하여 규칙 취지를 지킨다.
- **enum(고정 4탭) vs 완전 제네릭 item 리스트**: 디자인상 탭이 4개로 고정이라 enum이 타입 안전하고 단순. `items` 파라미터로 부분 노출/테스트 유연성은 확보.
- **테스트 = Compose 계측 테스트(androidTest)**: 순수 UI라 JVM 유닛 테스트로는 검증 가치가 낮음. 기존 프로젝트가 designsystem androidTest 패턴을 사용하므로 이를 따른다. 단, 계측 테스트는 에뮬레이터 필요 → `./gradlew test`로는 실행 안 됨(§7 참조).

## 5. 데이터 흐름

stateless 프레젠테이셔널 컴포넌트라 도메인 데이터 흐름 없음.
```
호출부(app/화면) ──selectedItem──▶ TodakunBottomNavigation
호출부(app/화면) ◀──onItemSelected(item)── (탭 클릭)
```

## 6. 파일 변경 계획 (구현 체크리스트)

**드로어블 (신규, `core/designsystem/src/main/res/drawable/`)** — SVG→VectorDrawable 변환:
- [ ] `ic_navi_lucky_on.xml`, `ic_navi_lucky_off.xml`
- [ ] `ic_navi_ai_on.xml`, `ic_navi_ai_off.xml`
- [ ] `ic_navi_action_on.xml`, `ic_navi_action_off.xml`
- [ ] `ic_navi_my_on.xml`, `ic_navi_my_off.xml`

**문자열 (신규/수정)**:
- [ ] `core/designsystem/src/main/res/values/strings.xml` — `bottom_nav_fortune`("운세"), `bottom_nav_todak`("토닥이"), `bottom_nav_lucky_action`("행운 액션"), `bottom_nav_my`("마이")

**컴포넌트 (신규)**:
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/bottomnavigation/TodakunNavItem.kt`
- [ ] `core/designsystem/src/main/java/com/kikidan/designsystem/component/bottomnavigation/TodakunBottomNavigation.kt` (+ `@Preview`)

**gradle (수정)**:
- [ ] `core/designsystem/build.gradle.kts` — androidTest용 Compose UI 테스트 의존성 추가
  ```kotlin
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.junit)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  ```
  (`libs.androidx.compose.ui.test.junit4`, `...ui.test.manifest`는 카탈로그에 존재)


> 실행: 계측 테스트라 에뮬레이터/기기 필요(`./gradlew :core:designsystem:connectedDebugAndroidTest`). 기기 없는 CI에서는
> 최소한 컴파일 검증(`./gradlew :core:designsystem:assembleDebug :core:designsystem:compileDebugAndroidTestKotlin`)으로 대체.

## 8. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **탭 enum 네이밍**: 첫 탭 라벨은 "운세"인데 Figma 아이콘 컴포넌트명은 `ic_navi_lucky`다. enum명을 `FORTUNE`으로 제안(드로어블은 Figma명 `ic_navi_lucky_*` 유지). 괜찮은지?
-> ic_navi_lucky로 진행
- [ ] **상단 그림자 재현 방식**: `Modifier.shadow`는 사방 그림자라 상단만 표현이 어렵다. `drawBehind` 커스텀 그림자로 근사 예정 — 픽셀 정확도는 시각 확인 후 조정. 허용 범위?
-> Modifier.dropShadow() 로 진행
- [ ] **컴포넌트 범위**: 이슈가 "컴포넌트 구현"이므로 라우팅 연결은 제외했다. `MainActivity` 데모 연결까지 원하면 알려달라.
-> 컴포넌트 구현만
- [ ] **다크 모드/테마 대응**: 현재 baked-color 벡터로 라이트 전용. 향후 테마 대응이 필요하면 아이콘을 tint 가능한 형태로 재설계 필요.
-> 다크모드 미대응
