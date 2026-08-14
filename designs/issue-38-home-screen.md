# 설계 문서 — #38 [Feat] 홈 화면 구성

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #38 (https://github.com/YAPP-Github/28th-App-Team-2-Android/issues/38)
- **작성**: 설계 에이전트 (Opus) / 2026-08-14
- **브랜치**: `feat/38-home-screen` (base: `origin/develop`)
- **상태**: 승인됨(구현 가능)
- **사람 결정 반영 (2026-08-14)**: Q1~Q12 전부 결정 완료 (Q13은 §1-2 스텁 표로 기결정). 상세는 각 절 및 §5 참고. 요약:
  - Q1: 설계안 그대로 (`LuckActionScoreResponse.id` = `luckActionId` 가정, WU-1 착수 시 dev 서버로 검증)
  - Q2: 가설 B — `title` = 글래스 카드 라벨, 인사 카피는 `feature:home`에 하드코딩
  - Q3: 그라데이션 컷오프/시작색 제안값 확정
  - Q4: 글래스 이펙트 — Figma 실측 파라미터(Light -45°/80%, Refraction 80, Depth 20, Dispersion 50, Frost 15, Splay 0) 기준 Compose 근사 전략 확정 (§2-8)
  - Q5: 팔레트 밖 색상 2건 기존 토큰 근사
  - Q6: `MainActivity` → `HomeRoute` 직접 호출
  - Q7: `feature:home` 전용 헤더 신규 작성 (공용 컴포넌트 미변경)
  - Q8: 이번 이슈는 "성취운"만 추가, 기존 "직장운" 유지
  - Q9: 바텀시트 서브텍스트 생략
  - Q10: 닫기 아이콘 상단 X만 구현
  - Q11: 점수 폰트 Pretendard로 대체
  - Q12: 배경/캐릭터 정적 이미지 export

## 참조 소스

| 항목 | 값 |
|---|---|
| Figma 홈 화면 | `bLZr7Nh53PmRHuEjX7gNco` node `2944-30491` |
| Figma 오늘의 점수 카드 | node `2944-30685` |
| Figma 상세 운세 바텀시트 | node `1001-11717`(금전 90) / `1001-11778`(건강 21) / `1001-11599`(관계 45) / `1001-11839`(성취 72) / `1001-11660`(연애 84) |
| Swagger | `https://api-dev.todakun.com/v3/api-docs` (2026-08-14 조회) |

---

## 1. 범위

### 1-1. 포함

- `feature:home` 모듈 신규 생성 및 홈 화면 전체 UI 구현
- 홈 화면 데이터 연동에 필요한 domain/data 레이어 확장
  (오늘의 총점 + 코멘트, 카테고리별 `luckActionId`, 행운 액션 상세)
- "상세 운세" 카테고리 칩 탭 → 상세 운세 바텀시트 (높이 80% 상한 + 내부 스크롤)
- `MainActivity`에 홈 화면 진입 배선 (현재 템플릿 `Greeting` 대체)
- 위 범위의 ViewModel / UseCase / Mapper 유닛 테스트

### 1-2. 제외 (스텁 처리 — 사람이 확정한 방침)

`feature:saju-contents` 모듈(상대방 궁합 / 택일 운세 / 연도별 운세)은 develop에 **없다**.
`feat/108-yearly-fortune`, `feat/115-date-fortune-ui` 브랜치에만 있고 아직 머지 전이다.
따라서 아래는 **화면에 보이되 동작하지 않는 스텁**으로 구현하고, 108/115 머지 후 별도 이슈에서 실제 라우트에 연결한다.

| 스텁 대상 | 스텁 방식 |
|---|---|
| 사주 풀이 3개 항목 (상대랑 궁합 / 택일 운세 / 연도별 운세) | 클릭 리스너 미연결 + `// TODO(#38): feature:saju-contents 머지 후 연결` 주석 |
| "운세 리포트" 버튼 (글래스 카드 우측) | 동일 (대상 화면 미정) |
| 헤더 벨 아이콘 | 동일 (알림 화면 미구현) |
| 하단 네비게이션 4탭 | `selectedItem = FORTUNE_TELLING` 고정, `onItemSelect` no-op + TODO |
| "운세 기반 행운 액션" 배너 | `HomeRoute(onNavigateToLuckAction: () -> Unit)` 콜백으로 노출만 하고, `MainActivity`에서 no-op 전달 |

> **NavHost가 이 저장소에 아직 존재하지 않는다.** `MainActivity`는 여전히 안드로이드 템플릿의 `Greeting`이고, `core:navigation`은 빈 모듈이며, `ChatRoute`/`LuckActionRoute`도 아무 데서도 호출되지 않는다. 이 이슈에서 Navigation 3 배선을 새로 짜는 것은 범위 초과이므로, `MainActivity`가 `HomeRoute`를 직접 호출하도록만 한다(§4-Q6).

### 1-3. 작업 단위 분할 및 실행 의존성

```
WU-1 (domain + data 확장)
  │
  ▼
WU-2 (feature:home 모듈 골격 + 상단 영역)
  │
  ├──────────────┬──────────────┐
  ▼              ▼              │
WU-3 (본문 섹션)  WU-4 (바텀시트)  │
  └──────────────┴──────────────┘
                 │
                 ▼
            WU-5 (app 배선 + 테스트 마감)
```

| ID | 작업 단위 | 파일 수 | 선행 |
|---|---|---|---|
| **WU-1** | domain 모델/Repository/UseCase + data DTO·Mapper·DataSource·RepositoryImpl 확장 | 12 | — |
| **WU-2** | `feature:home` 모듈 생성, Route/ViewModel/State/SideEffect, 헤더·배경·인사 카피·오늘의 점수 글래스 카드 | 8 | WU-1 |
| **WU-3** | 상세 운세 가로 스크롤 리스트, 사주 풀이 섹션(스텁), 행운 액션 배너, 하단 네비 배치 | 5 | WU-2 |
| **WU-4** | 상세 운세 바텀시트 (반원 게이지 + 점수대 그라데이션 + 80% 높이/스크롤) | 4 | WU-2 |
| **WU-5** | `MainActivity` 배선, 유닛 테스트, 문자열 리소스 정리 | 6 | WU-3, WU-4 |

WU-3와 WU-4는 서로 독립이므로 병렬 진행 가능하다.

---

## 2. 설계 및 실행 계획

### 2-1. 확인된 사실 (설계의 근거)

**Swagger 실측 (2026-08-14)**

```
GET /api/v1/daily-fortunes/today
  → TodayFortuneResponse { id, fortuneDate, score, title, luckActionScores[] }
GET /api/v1/daily-fortunes/{dailyFortuneId}
  → DailyFortuneResponse { id, fortuneDate, score, title, content, luckyItems[], cautionaryItems[], luckActionScores[] }
GET /api/v1/luck-actions/{luckActionId}
  → LuckActionResponse { id, fortuneCategory, score, title, content, achieved }
LuckActionScoreResponse { id, fortuneCategory, score }
```

**Figma 실측 — 오늘의 점수 카드 (node 2944-30685)**

| 속성 | 값 | 대응 토큰 |
|---|---|---|
| 컨테이너 | 337×69, radius 16, fill `#FFFFFF` @ 10% | `TodakunColor.whiteOpacity10` |
| 패딩 | 좌우 16, 상하 14 | — |
| "오늘의 점수" | 10sp / Regular / `#FFFFFF` @ 60% / lh 13 | `whiteOpacity60` + 신규 타이포 필요(§4-Q4) |
| "72점" | 16sp / SemiBold / `#FFFFFF` / lh 24 | `body2SemiBold`, `white` |
| "∙ 흐름 좋은 날" | 14sp / Medium / `#C1B4F6` / lh 20 | `body3Medium`, `primary300` |
| "운세 리포트" 버튼 | 100×28, radius 99, stroke `#FFFFFF` 1px, fill 없음, 14sp Medium white + chevron 16dp | `body3Medium`, `white` |

**Figma 실측 — 상세 운세 바텀시트 (node 1001-11717)**

```
Overlay  : #000000 @ 40%                          → TodakunColor.blackOpacity30/40 (§4-Q5)
Sheet    : 393×656 / 852 = 77%  (80% 요건 충족)
├ Top(96) : X 아이콘 20dp(우측 24) + 제목 20sp Bold #171717 + 서브텍스트 14sp Regular #828387
├ Body(434): 게이지 200×114 (아크 170×85, 점수 28sp #5757D7)
│           행운 액션 박스 353×116, fill #FAFAFA, radius 12, padding 20
│             ├ "오늘의 행운 액션" 14sp Medium #7F73EA center
│             └ 액션 제목       16sp SemiBold #171717 center (2줄)
│           본문 353×100, 14sp Regular #171717 (4문장)
├ CTA(92) : 353×52, radius 12, fill #7F73EA, 16sp SemiBold white
└ HomeIndicator(34)
```

토큰 대응: `#171717`=`gray975`, `#FAFAFA`=`gray25`, `#7F73EA`=`primary600`, `#5757D7`=`primary700`.
`#828387`은 팔레트에 없음 → `gray600(#8A8A8A)` 근사 (§4-Q5).

**Figma 실측 — 홈 화면 텍스트 노드 31개 (node 2944-30491)**

- 상세 운세 카테고리는 **5개 전부** 존재 (관계운 45 / 연애운 74 / 성취운 38 / 건강운 38 / 금전운 38), 각 카드 93dp → **가로 스크롤 `LazyRow`** 확정
- 인사 카피: "흘렸던 땀방울이 달콤한 결실로 돌아오는 하루예요" (193dp × 2줄)
- 사주 풀이 항목: 제목 24dp + 부제 20dp
  - 상대랑 궁합 / "그 사람과 나, 잘 맞을까요?"
  - 택일 운세 / "좋은 날을 골라드려요!"
  - 연도별 운세 / "올해의 큰 흐름 보기"
- 배너: "운세 기반 행운 액션" + "5개 행운 액션으로 하루 시작하기!"

### 2-2. Domain 레이어 확장

**문제**: `TodayFortuneResponse.toFortuneScores()`가 `luckActionScores`만 도메인으로 옮기고 총점(`score`)·코멘트(`title`)·카테고리별 `id`를 전부 버린다. 홈 글래스 카드와 바텀시트가 이 정보를 필요로 한다.

**선택한 설계**

1. **`FortuneScore`에 `luckActionId` 필드 추가** — 서버가 카테고리 점수마다 이미 `id`를 내려주고 있고, 바텀시트가 `GET /luck-actions/{id}`를 호출하려면 이 id가 필요하다. `DailyFortuneResponse.luckActionScores`도 같은 스키마이므로 매핑이 균일하다.
   - 대안(카테고리로 `/luck-actions/today` 결과와 매칭)은 네트워크 호출이 하나 더 늘고, 과거 날짜에는 쓸 수 없어 기각.
   - ⚠️ `LuckActionScoreResponse.id`가 실제 `luckActionId`인지 Swagger 문서상 명시가 없다 → §4-Q1에서 확인 필요.

2. **`TodayFortune` 신규 모델 + `getTodayFortuneScores()` → `getTodayFortune()` 시그니처 변경**
   ```kotlin
   data class TodayFortune(
       val id: String,
       val date: LocalDate,
       val totalScore: Int,
       val scoreLabel: String,     // TodayFortuneResponse.title, 예: "흐름 좋은 날" — 글래스 카드 라벨 (Q2 가설 B 확정)
       val scores: List<FortuneScore>,
   )
   ```
   인사 카피("흘렸던 땀방울이...")는 API에 대응 필드가 없으므로 `feature:home`의 `strings.xml`에 고정 문구로 둔다(§2-8, §5-Q2 참고).
   - 메서드를 하나 더 추가(`getTodayFortune()`을 별도로)하면 같은 엔드포인트를 두 경로에서 중복 호출하게 되므로, 기존 메서드를 확장하는 쪽을 택했다.
   - 기존 유일 호출부 `GetLuckActionPageUseCase.invokeToday()`는 `.scores`만 꺼내 쓰면 되므로 파급이 1줄이다.

3. **`LuckActionDetail` 신규 모델 + `LuckActionRepository.getLuckActionDetail(id)`**
   ```kotlin
   data class LuckActionDetail(
       val id: String,
       val category: FortuneCategory,
       val score: Int,
       val title: String,
       val content: String,
       val achieved: Boolean,
   )
   ```
   - 기존 `LuckAction`에 `score`/`content`를 얹지 않은 이유: `TodayLuckActionResponse`·`LuckActionSummaryResponse`에는 `content`가 없어 빈 문자열 기본값을 강제하게 되고, 이는 "DTO를 그대로 노출하지 않는다"(rules/10)의 취지에 어긋난다.

4. **UseCase 2종 신규**
   - `GetHomeFortuneUseCase` — `FortuneRepository.getTodayFortune()` 위임 (동사 시작, rules/10 준수)
   - `GetLuckActionDetailUseCase` — `LuckActionRepository.getLuckActionDetail(id)` 위임

   두 UseCase 모두 Repository 단순 위임이지만, rules/30이 "ViewModel은 UseCase를 통한다"를 P2로 규정하므로 유지한다.

**반환 타입**: 전부 `Result<T>` (rules/10 준수). 신규 모델 필드는 전부 `val`.

### 2-3. Data 레이어 확장

- `TodayFortuneResponse.toDomain(): TodayFortune` — 기존 `toFortuneScores()` 대체
- `LuckActionScoreResponse.toDomain()` — `luckActionId = id` 추가
- `LuckActionResponse.toDetail(): LuckActionDetail` 신규 (기존 `toDomain(): LuckAction`은 유지)
- `RemoteFortuneDataSource.getTodayFortuneScores()` → `getTodayFortune(): TodayFortune`
- `RemoteLuckActionDataSource.getLuckAction(id): LuckActionDetail` 신규
  - 엔드포인트 `GET api/v1/luck-actions/{luckActionId}` 와 1:1 대응 (rules/20)
  - DataSource는 try-catch 없이 그대로 throw, `RepositoryImpl`에서 `runCatchingCancellable` 래핑 (rules/20)

### 2-4. `feature:home` 모듈 설계

`feature:chat` / `feature:luck-action`과 동일한 패키지 컨벤션을 따른다.

```
feature/home/
  build.gradle.kts            plugins { alias(libs.plugins.todakun.feature) }
                              namespace = "com.kikidan.home"
                              deps: androidx.compose.material3, core.ktx,
                                    kotlinx.coroutines.core, (test) coroutines.test
  src/main/java/com/kikidan/home/
    HomeRoute.kt
    HomeViewModel.kt
    model/HomeState.kt
    model/HomeSideEffect.kt
    screen/HomeScreen.kt
    component/*.kt
```

`todakun.feature` 컨벤션 플러그인이 domain / navigation / designsystem / hilt / orbit(core·viewmodel·compose·test) 를 이미 주입하므로 build 파일은 chat/luck-action과 동일한 4줄이면 충분하다.

**의존성 방향** (rules/00): `feature:home → core:domain / core:designsystem` 만 사용. `feature:luck-action`, `feature:chat`을 **직접 참조하지 않는다**(P1). 화면 이동은 전부 `HomeRoute`의 콜백 파라미터로 뽑아 `app`이 연결한다.

**MVI 상태 설계** (rules/30, `LuckActionUiState` 패턴 답습)

```kotlin
sealed interface HomeState {
    data object Loading : HomeState
    data class Success(
        val totalScore: Int,
        val scoreLabel: String,                                // TodayFortune.scoreLabel, 글래스 카드 표시용
        val greeting: String,                                  // strings.xml 고정 문구 (Q2 가설 B 확정, API 필드 없음)
        val categories: PersistentList<CategoryScoreUiModel>,  // 5개
        val detail: DetailSheetUiState? = null,                // null이면 시트 닫힘
    ) : HomeState
    data object Failure : HomeState
}

data class CategoryScoreUiModel(
    val luckActionId: String,
    val category: FortuneCategory,
    val score: Int,
)

sealed interface DetailSheetUiState {
    data class Loading(val category: FortuneCategory) : DetailSheetUiState
    data class Success(
        val category: FortuneCategory,
        val score: Int,
        val actionTitle: String,
        val content: String,
    ) : DetailSheetUiState
}

sealed interface HomeSideEffect {
    data class Error(val e: Throwable) : HomeSideEffect
}
```

`ContainerHost<HomeState, HomeSideEffect>` + `container<...>(HomeState.Loading)`, 에러는 `postSideEffect` → `HomeRoute`가 `snackbarHostState`로 노출. `LuckActionRoute`/`ChatRoute`와 동일한 형태다.

바텀시트 상태를 `HomeState.Success.detail`에 두는 이유: 시트 내용이 네트워크 호출 결과(로딩/성공)를 가지므로 Compose 로컬 상태보다 ViewModel이 소유하는 편이 회전·프로세스 복원에 안전하고, rules/30의 "ViewModel은 상태 관리에 집중"과도 맞는다.

### 2-5. 점수대 → 그라데이션 색상 시스템

**확인된 데이터** (점수 텍스트 fill 기준. 아크 vector의 raw gradient는 Figma API가 노출하지 않아 조회 시 timeout — 3회 재시도 실패)

| 점수 | 카테고리 | 텍스트 색 | 팔레트 토큰 |
|---|---|---|---|
| 21 | 건강운 | `#EA4343` | `TodakunColor.red500` |
| 45 | 관계운 | `#179082` | `TodakunColor.teal700` |
| 72 / 84 / 90 | 성취·연애·금전 | `#5757D7` | `TodakunColor.primary700` |

Figma published style `icon_gra` = `#9C8AF6`(0%) → `#5757D7`(100%) = `primary500 → primary700`.

**제안 설계** (전부 기존 토큰 조합 — 하드코딩 없음, rules/40 준수)

```kotlin
// feature/home/component/FortuneScoreGradient.kt
internal enum class FortuneScoreBand(val start: Color, val end: Color) {
    LOW(TodakunColor.red300,     TodakunColor.red500),      // #F78B8B → #EA4343
    MID(TodakunColor.teal500,    TodakunColor.teal700),     // #20CBB7 → #179082
    HIGH(TodakunColor.primary500, TodakunColor.primary700), // #9C8AF6 → #5757D7  == icon_gra
    ;
    companion object {
        fun of(score: Int) = when {
            score < 40 -> LOW
            score < 60 -> MID
            else -> HIGH
        }
    }
}
```

- 끝 색(`end`)은 **실측 확정값**이다.
- 시작 색(`start`)은 확정된 purple 램프(500→700, 2단계 차이)를 red/teal에 유추 적용한 값이며 **미확정**이다 (§4-Q3).
- 컷오프 `<40 / <60 / ≥60`는 실측 구간(21=red, 45=teal, 72=purple)과 모순되지 않는 최소 가정이며 **미확정**이다 (§4-Q3).
- 이 매핑은 바텀시트 게이지와 홈 "상세 운세" 카드 점수 텍스트 두 곳에서 쓰이므로 `feature:home` 내부 단일 파일로 둔다. 소비자가 하나뿐이므로 `core:designsystem`으로 올리지 않는다 (다른 feature가 쓰기 시작하면 그때 승격).

### 2-6. 반원 게이지 구현

Figma 아크는 170×85 반원, 좌→우 진행, 끝에 cap. Compose `Canvas` + `drawArc`로 구현한다.

```
Canvas(Modifier.size(170.dp, 85.dp)) {
    drawArc(color = TodakunColor.gray100, startAngle = 180f, sweepAngle = 180f,
            useCenter = false, style = Stroke(width, cap = StrokeCap.Round))   // 트랙
    drawArc(brush = Brush.horizontalGradient(listOf(band.start, band.end)),
            startAngle = 180f, sweepAngle = 180f * score / 100f,
            useCenter = false, style = Stroke(width, cap = StrokeCap.Round))   // 진행
}
```

`Brush.sweepGradient`가 아크 곡률에는 더 정확하지만 시작 각도 정렬이 까다롭고, 반원 구간에서는 `horizontalGradient`와 육안 차이가 거의 없으므로 후자를 택한다. 스트로크 두께는 Figma의 `Ellipse 19` 폭(≈18.6dp)에서 유추 → 구현 시 `export_node_as_image`로 재확인 권장.

### 2-7. 바텀시트 — 80% 높이 상한 + 내부 스크롤

요건: **시트 높이가 화면의 80%를 넘지 않고, 넘으면 시트 내부가 스크롤된다.**
(Figma 원안은 656/852 = 77%로 이미 상한 이내지만, 본문 길이가 가변이므로 상한이 필요하다.)

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FortuneDetailBottomSheet(...) {
    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TodakunColor.white,
        dragHandle = null,
    ) {
        Column(Modifier.heightIn(max = screenHeight * MAX_SHEET_HEIGHT_RATIO)) {
            SheetHeader(...)                                   // 고정
            Column(
                Modifier
                    .weight(1f, fill = false)                  // 짧으면 wrap, 길면 잘림 → 스크롤
                    .verticalScroll(rememberScrollState()),
            ) {
                ScoreGauge(...); LuckActionBox(...); ContentText(...)
            }
            CtaButton(...)                                     // 고정
        }
    }
}
```

- `weight(1f, fill = false)`가 핵심이다. 내용이 짧으면 시트가 내용 높이만큼만 뜨고, 길어지면 남은 공간까지만 차지한 뒤 내부가 스크롤된다. 별도의 높이 측정/분기 없이 한 줄로 요건을 만족한다.
- 헤더(X + 제목)와 CTA를 스크롤 밖에 고정한 이유: Figma에서 CTA가 시트 바닥에 붙어 있고, 긴 본문에서 CTA가 밀려나면 "토닥이에게 더 물어보기" 진입이 막힌다.
- `LocalConfiguration.current.screenHeightDp` 대신 `LocalWindowInfo.containerSize`를 쓴다 (Compose BOM 2026.02.01 기준 전자는 deprecated).
- Compose BOM 2026.02.01에서 `ModalBottomSheet`는 `androidx.compose.material3` 소속 → `feature:home`의 `build.gradle.kts`에 `implementation(libs.androidx.compose.material3)` 필요 (chat/luck-action과 동일).
- `WheelPickerBottomSheet`는 `internal` + 휠피커 전용 레이아웃이라 재사용 불가. 다만 dropShadow/radius/`navigationBarsPadding` 처리 방식은 참고한다.

### 2-8. 홈 화면 레이아웃 골격

```
Box(fillMaxSize)
└ Column
  ├ HomeHeader                                   ← feature:home 전용 신규 (Q7, 공용 TodakunMainHeader 미변경)
  ├ Column(verticalScroll)                       ← 페이지 전체 스크롤
  │ ├ Box  다크 네이비 그라데이션 + 배경 이펙트 + 캐릭터 + 인사 카피(하드코딩, Q2)
  │ │ └ HomeTodayScoreCard   (글래스 카드, §2-8-1 글래스 이펙트)
  │ └ Column(white, radius top 24)               ← 화이트 라운드 카드
  │   ├ "상세 운세" + LazyRow(5 categories)
  │   ├ "사주 풀이" + 3 items (스텁)
  │   └ 행운 액션 그라데이션 배너
  └ TodakunBottomNavigation(selectedItem = FORTUNE_TELLING, onItemSelect = {})
```

#### 2-8-1. 글래스모피즘 구현 (Q4 → 리뷰 후 haze로 대체, 2026-08-14)

사람이 Figma에서 직접 확인한 글래스 이펙트 파라미터: **Light -45° / 80%, Refraction 80, Depth 20, Dispersion 50, Frost 15, Splay 0**.

**1차 구현(리뷰에서 P3 지적)**: `Build.VERSION.SDK_INT >= 31`일 때만 `Modifier.blur` 적용, API 26~30에서는 블러 없이 반투명 배경만 남아 Frost 효과가 아예 빠지는 문제가 있었다.

**최종 결정**: [`chrisbanes/haze`](https://github.com/chrisbanes/haze) 라이브러리를 신규 의존성으로 도입한다. RenderEffect(API 31+) 없이 자체 블러 셰이더를 구현해 `minSdk 26`부터 전 버전에서 동일하게 동작하고, `HazeTint`로 틴트 색(≈Refraction/Frost 근사), `HazeStyle`의 `noiseFactor`로 질감을 조절할 수 있어 1차 구현보다 실제 Figma 글래스 이펙트에 더 가깝다.

- **의존성**: `libs.versions.toml`에 `haze = "dev.chrisbanes.haze:haze"` 추가 (버전은 구현 시점 최신 stable 확인). 현재 유일한 소비자가 `feature:home`이므로 해당 모듈의 `build.gradle.kts`에만 추가한다 — Q7과 동일한 논리로, 다른 화면에서도 글래스 카드가 필요해지면 그때 `core:designsystem`으로 승격 검토.
- **적용 구조**: 배경(다크 그라데이션 + 캐릭터)이 그려지는 컨테이너에 `Modifier.hazeSource(state = hazeState)`, `HomeTodayScoreCard`에 `Modifier.hazeEffect(state = hazeState, style = ...)`를 적용한다(정확한 API 명은 haze 버전에 따라 `haze()`/`hazeChild()`일 수 있으니 구현 시점 문서 확인). `blur`/`edgeTreatment`/`Build.VERSION` 분기 코드는 제거한다.
- **Refraction/Dispersion/Splay**: haze도 이 파라미터들을 완전히 재현하진 않는다(색 분산·굴절 왜곡은 여전히 셰이더 영역). 그래도 blur 근사보다 결과가 낫고 API 커버리지가 넓어지므로 이 갭은 감수한다.
  `ponytail: haze로도 dispersion/refraction 완전 재현은 안 됨 — 픽셀 완전 일치 필요해지면 AGSL RuntimeShader(API 33+) 도입`

**전체 스크롤 vs LazyColumn**: 홈은 섹션 수가 고정(4개)이고 카테고리 리스트만 5개이므로 `Column + verticalScroll`로 충분하다. `LazyColumn` 안에 `LazyRow`를 중첩하면 스크롤 충돌 처리가 붙으므로 피한다.

**리컴포지션** (rules/40 P2): 리스트는 `PersistentList` (`kotlinx-collections-immutable`, 컨벤션 플러그인이 이미 주입), UI 모델은 `data class`, 콜백은 `viewModel::method` 참조로 전달해 람다 재생성을 피한다.

### 2-9. 리소스

- 신규 drawable 필요: `ic_chevron_small_right` (운세 리포트 버튼) — 기존에 `ic_chevron_small_bottom`만 있음
- 배경/캐릭터 이미지: `img_home_background`, `img_home_character` (Figma export 필요, `img_` 접두사 = png/webp — rules/40)
- 문자열은 전부 `core/designsystem/src/main/res/values/strings.xml`에 `home_` 접두사로 추가 (기존 `luck_action_*`, `chat_*` 컨벤션 따름)
- ⚠️ 기존 문자열에서 `ACHIEVEMENT`가 "직장운"인데 Figma 홈/바텀시트는 "성취운"이다 (§4-Q8)

---

## 3. 파일 변경 계획 (구현 체크리스트)

```
28th-App-Team-2-Android/
├── settings.gradle.kts                                   [M] include(":feature:home")
├── app/
│   ├── build.gradle.kts                                  [M] implementation(projects.feature.home)
│   └── src/main/java/com/kikidan/todakun/
│       └── MainActivity.kt                               [M] Greeting → HomeRoute + Scaffold(snackbarHost)
├── core/domain/src/
│   ├── main/java/com/kikidan/domain/
│   │   ├── model/fortune/TodayFortune.kt                  [+]
│   │   ├── model/fortune/LuckActionDetail.kt              [+]
│   │   ├── model/fortune/FortuneScore.kt                  [M] + luckActionId
│   │   ├── repository/FortuneRepository.kt                [M] getTodayFortuneScores → getTodayFortune
│   │   ├── repository/LuckActionRepository.kt             [M] + getLuckActionDetail
│   │   ├── usecase/GetHomeFortuneUseCase.kt               [+]
│   │   ├── usecase/GetLuckActionDetailUseCase.kt          [+]
│   │   └── usecase/GetLuckActionPageUseCase.kt            [M] .scores 로 어댑트
│   └── test/java/com/kikidan/domain/
│       ├── fake/FakeFortuneRepository.kt                  [M]
│       ├── fake/FakeLuckActionRepository.kt               [M]
│       └── usecase/GetLuckActionDetailUseCaseTest.kt      [+]
├── core/data/src/
│   ├── main/java/com/kikidan/data/
│   │   ├── datasource/RemoteFortuneDataSource.kt          [M]
│   │   ├── datasource/RemoteLuckActionDataSource.kt       [M] + getLuckAction(id)
│   │   ├── repository/FortuneRepositoryImpl.kt            [M]
│   │   └── repository/LuckActionRepositoryImpl.kt         [M]
│   └── test/java/com/kikidan/data/
│       ├── fake/FakeRemoteFortuneDataSource.kt            [M]
│       ├── fake/FakeRemoteLuckActionDataSource.kt         [M]
│       └── repository/LuckActionRepositoryImplTest.kt     [M] 상세 조회 케이스 추가
├── core/data-remote/src/
│   ├── main/java/com/kikidan/data_remote/
│   │   ├── dto/fortune/TodayFortuneResponse.kt            [M] toFortuneScores → toDomain
│   │   ├── dto/fortune/DailyFortuneResponse.kt            [M] 매퍼 시그니처 정렬
│   │   ├── dto/fortune/LuckActionScoreResponse.kt         [M] + luckActionId
│   │   ├── dto/fortune/LuckActionResponse.kt              [M] + toDetail()
│   │   ├── datasource/RemoteFortuneDataSourceImpl.kt      [M]
│   │   └── datasource/RemoteLuckActionDataSourceImpl.kt   [M] GET luck-actions/{id}
│   └── test/java/com/kikidan/data_remote/
│       ├── dto/fortune/FortuneMapperTest.kt               [M]
│       └── datasource/RemoteLuckActionDataSourceImplTest.kt [M]
├── core/designsystem/src/main/res/
│   ├── values/strings.xml                                 [M] home_* 문자열
│   ├── drawable/ic_chevron_small_right.xml                [+]
│   ├── drawable/img_home_background.webp                  [+] Figma export
│   └── drawable/img_home_character.png                    [+] Figma export
└── feature/home/                                          [+] 신규 모듈
    ├── build.gradle.kts                                   [+]
    ├── consumer-rules.pro / proguard-rules.pro / .gitignore [+]
    └── src/main/
        ├── AndroidManifest.xml                            [+]
        └── java/com/kikidan/home/
            ├── HomeRoute.kt                               [+]
            ├── HomeViewModel.kt                           [+]
            ├── model/HomeState.kt                         [+]
            ├── model/HomeSideEffect.kt                    [+]
            ├── screen/HomeScreen.kt                       [+]
            └── component/
                ├── HomeTodayScoreCard.kt                  [+] 글래스 카드
                ├── HomeCategoryScoreRow.kt                [+] 상세 운세 LazyRow
                ├── HomeSajuSection.kt                     [+] 사주 풀이 3항목 (스텁)
                ├── HomeLuckActionBanner.kt                [+]
                ├── FortuneDetailBottomSheet.kt            [+] 80% + 스크롤
                ├── FortuneScoreGauge.kt                   [+] 반원 게이지
                └── FortuneScoreGradient.kt                [+] 점수대 → 색상 밴드
        └── test/java/com/kikidan/home/
            ├── FakeFortuneRepository.kt                   [+]
            ├── FakeLuckActionRepository.kt                [+]
            └── HomeViewModelTest.kt                       [+]
```

### 체크리스트

**WU-1 — domain / data 확장**
- [ ] `core/domain/.../model/fortune/TodayFortune.kt` — 신규 모델 (전 필드 `val`)
- [ ] `core/domain/.../model/fortune/LuckActionDetail.kt` — 신규 모델
- [ ] `core/domain/.../model/fortune/FortuneScore.kt` — `luckActionId: String` 추가
- [ ] `core/domain/.../repository/FortuneRepository.kt` — `getTodayFortune(): Result<TodayFortune>`
- [ ] `core/domain/.../repository/LuckActionRepository.kt` — `getLuckActionDetail(id): Result<LuckActionDetail>`
- [ ] `core/domain/.../usecase/GetHomeFortuneUseCase.kt`, `GetLuckActionDetailUseCase.kt`
- [ ] `core/domain/.../usecase/GetLuckActionPageUseCase.kt` — `getTodayFortune().map { it.scores }`
- [ ] `core/data-remote/.../dto/fortune/*.kt` — 매퍼 4건 수정/추가
- [ ] `core/data/.../datasource/*.kt`, `repository/*.kt` — 시그니처 반영, `runCatchingCancellable` 유지
- [ ] `core/data-remote/.../RemoteLuckActionDataSourceImpl.kt` — `GET api/v1/luck-actions/{id}` 추가
- [ ] 기존 Fake 4종 시그니처 동기화
- [ ] `./gradlew :core:domain:test :core:data:test :core:data-remote:test` 통과

**WU-2 — feature:home 골격 + 상단 영역**
- [ ] `settings.gradle.kts` include, `feature/home/build.gradle.kts` (chat 모듈 형태 복제)
- [ ] `HomeState.kt` / `HomeSideEffect.kt`
- [ ] `HomeViewModel.kt` — `ContainerHost` 위임, `load()` / `openDetail(luckActionId)` / `closeDetail()`
- [ ] `HomeRoute.kt` — `collectAsState` + `collectSideEffect`, 네비 콜백 파라미터 노출
- [ ] `HomeScreen.kt` — 배경/캐릭터/인사/헤더 배치
- [ ] `HomeTodayScoreCard.kt` — §2-1 실측 스펙대로
- [ ] drawable / strings 추가

**WU-3 — 본문 섹션**
- [ ] `HomeCategoryScoreRow.kt` — `LazyRow`, 5개 카테고리, 칩 클릭 → `openDetail`
- [ ] `HomeSajuSection.kt` — 3항목 표시 + 클릭 미연결 TODO
- [ ] `HomeLuckActionBanner.kt` — 그라데이션 배너, `onNavigateToLuckAction` 콜백
- [ ] `HomeScreen.kt`에 섹션 + `TodakunBottomNavigation` 조립

**WU-4 — 상세 운세 바텀시트**
- [ ] `FortuneScoreGradient.kt` — 밴드 3종
- [ ] `FortuneScoreGauge.kt` — `drawArc` 반원 게이지 + 중앙 점수
- [ ] `FortuneDetailBottomSheet.kt` — 헤더/CTA 고정 + 본문 스크롤 + 80% 상한
- [ ] `HomeViewModel`에 상세 로딩 인텐트 연결

**WU-5 — 배선 + 테스트**
- [ ] `MainActivity.kt` — `Greeting` 제거, `HomeRoute` + `Scaffold(snackbarHost = ...)`
- [ ] `app/build.gradle.kts` — `projects.feature.home`
- [ ] `HomeViewModelTest.kt` (§4 테스트 계획)
- [ ] `./gradlew :feature:home:test :core:domain:test :core:data:test :core:data-remote:test` 통과
- [ ] `./gradlew ktlintCheck` 통과
- [ ] `./gradlew assembleDebug` 통과

---

## 4. 테스트 계획

### 4-1. 유닛 테스트 — `feature:home` (`orbit-test`, 컨벤션 플러그인이 이미 주입)

`HomeViewModelTest`

| # | 케이스 | 기대 |
|---|---|---|
| 1 | `load()` 성공 | `HomeState.Success(totalScore, comment, categories.size == 5)` |
| 2 | `load()` 카테고리 정렬 | `FortuneCategory.ordinal` 순서 유지 (`LuckActionViewModel`과 동일 규칙) |
| 3 | `load()` 실패 | `HomeSideEffect.Error` 발행 + `HomeState.Failure` |
| 4 | `openDetail(id)` 성공 | `detail`이 `Loading` → `Success(score, actionTitle, content)` 순으로 전이 |
| 5 | `openDetail(id)` 실패 | `HomeSideEffect.Error` 발행, `detail == null`로 복귀 (시트가 빈 채 열려 있지 않을 것) |
| 6 | `closeDetail()` | `detail == null`, 나머지 Success 필드 불변 |
| 7 | Success 상태에서 `openDetail` 재호출 | 기존 총점/카테고리 상태가 초기화되지 않음 |

Fake는 `feature/luck-action/src/test/.../FakeFortuneRepository.kt` 패턴을 복제한다.

### 4-2. 유닛 테스트 — domain

`GetLuckActionDetailUseCaseTest`
- 성공 시 `LuckActionDetail` 그대로 전달
- 실패 시 `Result.failure` 전파

`GetLuckActionPageUseCaseTest` (기존 파일 수정)
- `getTodayFortune()` 변경 후에도 오늘 날짜 경로가 `scores + actions`를 합쳐 반환하는지 회귀 확인

### 4-3. 유닛 테스트 — data

`FortuneMapperTest` (기존 수정)
- `TodayFortuneResponse.toDomain()`이 `score`/`title`/`fortuneDate`/`luckActionScores`를 **전부** 옮기는지 (현재 유실 버그의 회귀 방지)
- `LuckActionScoreResponse.toDomain()`의 `luckActionId` 매핑
- `LuckActionResponse.toDetail()`의 `content`/`score` 매핑

`RemoteLuckActionDataSourceImplTest` (기존 수정)
- `GET api/v1/luck-actions/{id}` 경로가 정확한지 (MockEngine)
- 에러 응답 시 예외를 삼키지 않고 그대로 throw 하는지 (rules/20 P1)

`LuckActionRepositoryImplTest` (기존 수정)
- `getLuckActionDetail` 성공/실패가 `Result`로 감싸지는지

### 4-4. Compose UI 테스트

이번 이슈에서는 작성하지 않는다. `core:designsystem`만 `androidTest`를 운용 중이고 feature 모듈에는 UI 테스트 관례가 없다. 대신 모든 신규 컴포넌트에 `@Preview`를 붙여 육안 검토가 가능하게 한다.

### 4-5. 수동 검증

- [ ] 바텀시트가 **긴 본문**(6문장 이상)에서 화면 80%를 넘지 않고 내부만 스크롤되는지
- [ ] 바텀시트가 **짧은 본문**(1문장)에서 불필요하게 늘어나지 않는지
- [ ] 21 / 45 / 72점 각각에서 게이지 색이 red / teal / purple로 갈리는지
- [ ] 상세 운세 5개 카테고리가 가로 스크롤되는지
- [ ] `./gradlew ktlintCheck` — Compose lint 컨벤션 (rules/40)

---

## 5. 리스크 / 미해결 질문 (사람 확인 필요)

> **Q1, Q2는 사람이 결정함 (2026-08-14, 상단 참고).** 아래 항목은 결정 반영 + 나머지는 구현하며 근사치로 진행 가능하되 검토 시 확정이 필요하다.

- [x] **Q1 (해결) — `LuckActionScoreResponse.id` = `luckActionId`로 가정하고 설계안 그대로 진행.**
  `GET /api/v1/luck-actions/{luckActionId}`로 점수·액션 제목·본문을 한 번에 받는다. WU-1 구현 착수 시 dev 서버에 실제 호출해 최종 검증할 것 (id가 다르면 대안: `GET /luck-actions/today` + `fortuneCategory` 매칭으로 전환, 이 경우 `FortuneScore.luckActionId` 추가는 불필요해짐).

- [x] **Q2 (해결) — 가설 B 확정.** `TodayFortuneResponse.title` = 글래스 카드 라벨("흐름 좋은 날" 등, `TodayFortune.scoreLabel`)이고, 인사 카피("흘렸던 땀방울이...")는 API에 대응 필드가 없어 `feature:home`의 `strings.xml`에 고정 문구로 하드코딩한다(§2-2, §2-8, `HomeState.Success.greeting`).
  → 라벨("흐름 좋은 날")과 아래 Q3의 그라데이션 색상 밴드는 **서로 독립**이다 — 라벨은 서버가 내려주는 문자열 그대로 쓰고, 게이지 색은 클라이언트가 점수로 계산한다. 둘의 컷오프가 우연히 다를 수 있음을 인지하고 진행한다.

- [x] **Q3 (결정) — 점수대 컷오프와 red/teal 그라데이션 시작 색 제안값 확정.**
  `<40 LOW(red300→red500) / <60 MID(teal500→teal700) / ≥60 HIGH(primary500→primary700, icon_gra와 동일)`. §2-5 코드 그대로 구현.

- [x] **Q4 (결정) — 글래스 이펙트: Figma 실측 파라미터 확보, Compose 근사 전략 확정.**
  Light -45°/80%, Refraction 80, Depth 20, Dispersion 50, Frost 15, Splay 0. Refraction/Dispersion/Splay는 AGSL 없이 재현 불가 → blur(Frost) + 대각선 스페큘러 하이라이트(Light)로 근사. 구현 상세는 §2-8-1.
  "오늘의 점수" 10sp 라벨과 `TodakunTypography` 최소 스케일(`caption3`) 불일치 여부는 WU-2 구현 중 실측 확인.

- [x] **Q5 (결정) — 팔레트 밖 색상 2건, 기존 토큰으로 근사.**
  서브텍스트 `#828387` → `gray600(#8A8A8A)`. 딤 오버레이 `#000000@40%` → `blackOpacity30` 사용 (신규 토큰 추가 안 함).

- [x] **Q6 (결정) — `MainActivity`가 `HomeRoute` 직접 호출.**
  NavHost 신규 구축은 별도 이슈로 분리. 이 저장소에 NavHost가 전혀 없다는 사실(`MainActivity`=템플릿 `Greeting`, `core:navigation` 빈 모듈)은 그대로 유효하며, 홈 화면 진입만 최소 배선한다.

- [x] **Q7 (결정) — `feature:home` 전용 헤더 신규 작성.**
  공용 `TodakunMainHeader`(텍스트 타이틀 전용)는 건드리지 않는다. 패턴이 반복되면 그때 승격 검토.

- [x] **Q8 (결정) — 이번 이슈는 "성취운"만 추가, 기존 "직장운"은 유지.**
  `luck_action_score_achievement`("직장운")는 변경하지 않는다. 표기 통일은 범위 밖 — 필요 시 별도 이슈.

- [x] **Q9 (결정) — 바텀시트 서브텍스트 생략.**
  `SubText` 프레임(Lorem ipsum, API 필드 없음)은 구현하지 않는다.

- [x] **Q10 (결정) — 닫기 아이콘은 상단 X 하나만 구현.**
  제목 우측의 28dp `ic/BottomPopup`(1001:11733) 중복 노드는 무시한다.

- [x] **Q11 (결정) — 점수 텍스트 폰트 Pretendard로 대체.**
  Figma의 `Noto Sans KR DemiLight(350)` 대신 `TodakunTypography.heading3Regular` 계열(28sp) 사용. 폰트 파일 추가 없음.

- [x] **Q12 (결정) — 배경/캐릭터/반짝임 전부 정적 이미지로 export.**
  `img_home_background`, `img_home_character` PNG/WebP export. 애니메이션 없음 (Figma에 모션 스펙 없음).

- [x] **Q13 (결정) — 스텁 범위는 §1-2 표 그대로.**
  사주 풀이 3항목 / 운세 리포트 / 벨 / 하단 네비 4탭 / 행운 액션 배너 이동이 no-op 상태로 머지된다(사람이 develop 기준 진행 + 스텁 처리로 확정). PR 본문에 명시할 것 (WU-5 체크리스트에 반영 권장).
