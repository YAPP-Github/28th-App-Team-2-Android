# 설계 문서 — #138 전체 화면 라우팅

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #138 [Feat] 전체 화면 라우팅
- **작성**: 사용자와 직접 논의하여 확정 (오케스트레이터 Claude) / 2026-08-18
- **상태**: 승인됨(구현 가능) — 2026-08-18

## 1. 범위

- 포함:
  - Navigation3 기반 전체 화면 라우팅 그래프 구성 (`core:navigation`에 라우트 키 정의 + `app`에서 조립)
  - Root(Scaffold + SnackbarHost + 하단 탭 + NavDisplay) 구성
  - `PushNotificationEventFlow` 구독 → 인앱 알림 상태 보유 (실제 배너 UI는 `// TODO` 스텁, 사용자가 별도 구현)
  - 딥링크 파싱 및 3가지 진입 경로(콜드 스타트 / 앱 실행 중 알림 탭(`onNewIntent`) / 포그라운드 인앱 알림 탭) 처리
  - `TodakunFirebaseMessagingService` 보강 — 백그라운드(프로세스 생존)에서도 딥링크를 담은 알림 생성
  - 일부 feature `XxxRoute`의 네비게이션 콜백 시그니처 보강 (`NotificationRoute` 클릭 이동)
- 제외 (TODO로 남김, 이번 이슈 범위 아님):
  - `feature:mypage`, `feature:saju-contents` 실제 화면 구현 및 `settings.gradle.kts` include
  - 인앱 알림 배너의 실제 디자인/애니메이션 (사용자가 별도 구현 예정)
  - 킬드(프로세스 완전 종료) 상태 딥링크의 완전한 보장 (리스크 4-1 참고, 백엔드 payload 구조에 달림)

## 2. 설계 및 실행 계획

### 2-1. 화면/탭 구조 (사용자 확인 완료)

하단 탭 4개 — `core:designsystem`의 `TodakunBottomNavigation` / `TodakunNavItem` 기준으로 이미 4개 항목(FORTUNE_TELLING, TODAK_CHAT, LUCKY_ACTION, MY)이 정의돼 있고, `TodakunNavItem`에는 `// TODO Navigation Route 클래스를 프로퍼티에 추가` 주석까지 남아 있어 이 이슈에서 연결하는 것이 맞다.

| 탭 | 화면 | 비고 |
|---|---|---|
| FORTUNE_TELLING | Home | `HomeRoute` |
| TODAK_CHAT | Chat | `ChatRoute(conversationId = null)` |
| LUCKY_ACTION | LuckAction | `LuckActionRoute` |
| MY | MyPage | **미구현 — TODO 스텁 화면** |

탭 외 스택 화면(현재 화면 위에 push): `Login`(시작 겸 스플래시), `Onboarding`, `Terms`, `FortuneReport(fortuneId)`, `ChatHistory`, `Notification`. (상대방 사주 화면은 라우트 자체를 만들지 않고 TODO 주석만 남긴다.)

### 2-2. 모듈 책임 분리 (`.claude/rules/00-architecture.md` 준수)

- `core:navigation`: 라우트 키(`NavKey`)와 딥링크 문자열 파서만 둔다. feature를 참조하지 않는 순수 데이터 계층 — `androidx.navigation3:navigation3-runtime`(NavKey 인터페이스 제공)만 의존성으로 추가.
- `app`: 이미 모든 feature + `core:navigation` + nav3/orbit 의존성이 `app/build.gradle.kts`에 선언돼 있음(확인 완료). 실제 `entryProvider`(NavKey → Composable 매핑)와 Root Composable은 여기 둔다.
- 근거: feature 모듈끼리 직접 참조 금지(P1)이므로 여러 feature의 화면을 한 곳에서 잇는 코드는 모든 feature에 의존 가능한 `app`에만 위치할 수 있다. `core:navigation`이 feature를 참조하면 역방향 위반이 되므로 "키만 아는 얇은 계층 + app이 조립"하는 구조만 규칙을 어기지 않는다.

### 2-3. NavKey 설계 (`core:navigation`)

```kotlin
sealed interface TodakunRoute : NavKey {
    @Serializable data object Login : TodakunRoute
    @Serializable data class Onboarding(val onboardingToken: String) : TodakunRoute
    @Serializable data object Terms : TodakunRoute
    @Serializable data object Home : TodakunRoute
    @Serializable data class Chat(val conversationId: String? = null) : TodakunRoute
    @Serializable data object ChatHistory : TodakunRoute
    @Serializable data object LuckAction : TodakunRoute
    @Serializable data object MyPage : TodakunRoute // TODO(#후속이슈): feature:mypage 구현 후 실제 화면 연결
    @Serializable data class FortuneReport(val fortuneId: String) : TodakunRoute
    @Serializable data object Notification : TodakunRoute
    // TODO(#후속이슈): 상대방 사주 라우트 — feature:saju-contents 구현 후 추가
}
```

`OnboardingToken`은 domain의 value class라 NavKey 직렬화 대상으로 바로 못 쓴다 → `Onboarding(val onboardingToken: String)`으로 원시값을 받고, `app`에서 `OnboardingToken(value)`로 감싸 `OnboardingRoute`에 전달한다.

### 2-4. 딥링크 파서 (`core:navigation`)

서버 스펙(사용자 확인):

| NotificationType | deepLink 예시 | 매핑 |
|---|---|---|
| FORTUNE | `todakun://fortune/today` | `FortuneReport(fortuneId = "today")` |
| LUCKY_ACTION | `todakun://lucky-action` | `LuckAction` |
| AI_COMPLETE | `todakun://chat/conversations/{conversationId}` | `Chat(conversationId)` |
| NOTICE | 고정 형식 없음(관리자 지정 값 또는 null) | 위 패턴에 매칭되면 재사용, 아니면 파싱 실패(`null`) |

```kotlin
fun parseDeepLink(uri: String): TodakunRoute?
```

매칭 실패 시 `null`을 반환하고, 호출부(Root)가 정책을 정한다 — 알림함 항목 클릭이면 `Notification`으로 폴백, 포그라운드 인앱 배너 탭이면 아무 것도 하지 않는다(이동할 곳이 없으므로).

### 2-5. Root 구성 (`app` 모듈)

- `MainActivity`: `enableEdgeToEdge()` 유지, `setContent { TodakunTheme { RootScreen() } }`. `AndroidManifest.xml`에서 `android:launchMode="singleTask"` 지정(현재 미지정=standard) — 딥링크 재실행 시 인스턴스 중복 방지.
- 딥링크 Intent 처리: `onCreate`의 최초 `intent`와 `onNewIntent(intent)` 양쪽에서 `intent?.data?.toString()`을 뽑아 Activity 스코프 `MutableSharedFlow<String>`으로 흘려보낸다.
- `RootScreen`(신규, `app` 모듈 `navigation` 패키지):
  - `val backStack = rememberNavBackStack(TodakunRoute.Login)`
  - `Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, bottomBar = { if (backStack.last() is 탭라우트) TodakunBottomNavigation(...) })`
  - `NavDisplay(backStack, entryProvider = entryProvider { ... })`에서 각 `TodakunRoute` → 해당 feature `XxxRoute` 매핑. `MyPage`는 `// TODO(#후속이슈): feature:mypage 연결` 주석과 최소 Placeholder(`Box` + 안내 텍스트)만 연결.
  - 탭 전환: 탭 클릭 시 `backStack`을 `[선택한 탭라우트]` 하나로 리셋한다(단순화). **탭별 독립 백스택은 이번 범위 밖 — 필요해지면 별도 이슈로.**
  - 딥링크/알림 클릭: 파싱된 `TodakunRoute`를 `backStack.add(route)`로 현재 스택 위에 push(사용자 확정: 뒤로가기 시 이전 화면으로 복귀).
  - `PushNotificationEventFlow.events`를 `LaunchedEffect(Unit)`에서 collect → `var inAppEvent by remember { mutableStateOf<PushNotificationEvent?>(null) }`에 저장 → `PushNotificationBanner(event = inAppEvent, onDismiss, onClick = { event.deepLink?.let(::parseDeepLink)?.let(backStack::add) })` 호출.

### 2-6. 인앱 알림 배너 — TODO 스텁

`PushNotificationBanner.kt`(신규, `app` 모듈)는 사용자가 카톡 스타일(위→아래 드롭다운)로 직접 구현할 예정이므로 이번 이슈에서는 시그니처와 `// TODO` 본문만 만든다:

```kotlin
// TODO: 카카오톡 스타일 상단 드롭다운 배너 UI 구현 (담당자가 별도 작업)
@Composable
fun PushNotificationBanner(
    event: PushNotificationEvent?,
    onDismiss: () -> Unit,
    onClick: (PushNotificationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
}
```

Root의 구독/상태관리는 이번 이슈에서 완성하고, 화면 표시만 비워둔다.

### 2-7. Feature Route 시그니처 보강

- `NotificationViewModel`: `NotificationSideEffect`에 `NavigateToDeepLink(val deepLink: String)` 추가, `onNotificationClick`에서 클릭된 `Notification.deepLink`(non-null)로 `postSideEffect`. `NotificationRoute`에 `onNavigateToDeepLink: (String) -> Unit` 파라미터 추가.
- 나머지(`HomeRoute`, `FortuneReportRoute`, `ChatRoute`, `HistoryRoute`, `OnboardingRoute`, `TermsRoute`, `LoginRoute`, `LuckActionRoute`)는 이미 필요한 콜백을 다 갖추고 있어 시그니처 변경이 필요 없다 — `app`의 `entryProvider`에서 `TodakunRoute` 값으로 콜백 람다만 연결한다.

### 2-8. 로그인 / 온보딩 / 시작 분기

- 시작 라우트: `TodakunRoute.Login` (`LoginRoute` 자체가 스플래시 + 인증 확인 + 로그인 UI를 캡슐화하고 있음, 코드 확인 완료).
- `onAuthPass()`(이미 인증됨) → `backStack`을 `[Home]`으로 교체.
- `onAuthSuccess(result: LoginResult)`:
  - `result.newMember == true` → `backStack`을 `[Terms]`로 교체, `result.onboardingToken`은 Root 상태로 들고 있다가 `Onboarding`으로 이동 시 전달.
  - `result.newMember == false` → `[Home]`으로 교체.
- `OnboardingRoute.onFinish()` → `[Home]`으로 교체.

### 2-9. `TodakunFirebaseMessagingService` 보강 (백그라운드 딥링크)

현재는 포그라운드일 때만 `PushNotificationEventFlow`로 emit하고, 백그라운드/킬드에서는 아무 것도 하지 않아 FCM이 자체적으로 기본 알림을 띄운다(딥링크 없는 launcher intent). 이번 이슈에서:

- 포그라운드가 아닐 때도 `onMessageReceived`에서 직접 `NotificationCompat.Builder`로 알림을 생성한다.
- `PendingIntent`의 target intent에 `PushNotificationEvent.DEEP_LINK_KEY` extra로 `deepLink` 문자열을 실어 `MainActivity`로 전달한다.
- `MainActivity.onCreate`/`onNewIntent`에서 이 extra를 우선 확인하고, 없으면 `intent.data`(외부 공유 링크)를 확인한다.
- **단, 이 로직은 앱 프로세스가 살아있어야 `onMessageReceived`가 호출된다.** 프로세스가 완전히 종료된 상태에서 FCM payload에 `notification` 블록이 있으면 OS가 앱 코드 호출 없이 자체 표시하므로 이 로직 자체가 실행되지 않는다 — 리스크 4-1 참고.

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/navigation/
├── build.gradle.kts                                          # nav3-runtime 의존성 추가
└── src/main/java/com/kikidan/navigation/
    ├── TodakunRoute.kt                                        # 신규 — NavKey sealed interface
    └── DeepLinkParser.kt                                      # 신규 — 딥링크 문자열 → TodakunRoute

app/
├── src/main/AndroidManifest.xml                                # todakun:// intent-filter, singleTask
└── src/main/java/com/kikidan/todakun/
    ├── MainActivity.kt                                         # 딥링크 인텐트 처리, RootScreen 호출
    └── navigation/
        ├── RootScreen.kt                                       # 신규 — Scaffold+SnackbarHost+BottomNav+NavDisplay
        └── PushNotificationBanner.kt                           # 신규 — TODO 스텁

feature/notification/src/main/java/com/kikidan/notification/
├── model/NotificationSideEffect.kt                             # NavigateToDeepLink 추가
├── NotificationViewModel.kt                                    # 클릭 시 딥링크 SideEffect 발행
├── NotificationRoute.kt                                        # onNavigateToDeepLink 콜백 추가
└── fcm/TodakunFirebaseMessagingService.kt                       # 백그라운드 알림 직접 생성 + 딥링크 PendingIntent
```

- [ ] `core/navigation/build.gradle.kts` — `androidx.navigation3:navigation3-runtime` 의존성 추가
- [ ] `core/navigation/.../TodakunRoute.kt` — NavKey sealed interface 신규 작성
- [ ] `core/navigation/.../DeepLinkParser.kt` — 딥링크 파서 신규 작성 (+ 유닛 테스트)
- [ ] `app/src/main/AndroidManifest.xml` — intent-filter, launchMode 수정
- [ ] `app/.../MainActivity.kt` — 딥링크 인텐트 스트림 + RootScreen 연결
- [ ] `app/.../navigation/RootScreen.kt` — Root 조립 (신규)
- [ ] `app/.../navigation/PushNotificationBanner.kt` — TODO 스텁 (신규)
- [ ] `feature/notification/.../model/NotificationSideEffect.kt` — SideEffect 추가
- [ ] `feature/notification/.../NotificationViewModel.kt` — SideEffect 발행 로직
- [ ] `feature/notification/.../NotificationRoute.kt` — 콜백 파라미터 추가
- [ ] `feature/notification/.../fcm/TodakunFirebaseMessagingService.kt` — 백그라운드 알림 생성 로직 추가 (+ 유닛 테스트 가능한 범위는 테스트)

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **킬드(프로세스 완전 종료) 상태의 FCM 알림 탭 딥링크는, 서버 payload에 `notification` 블록이 포함되는 한 클라이언트 코드로 가로챌 수 없다.** OS가 앱 코드를 거치지 않고 자체 표시하기 때문이다. 완전히 지원하려면 백엔드가 data-only payload로 전환해야 한다 — Swagger/백엔드 확인 필요. 이번 구현은 포그라운드 + 백그라운드(프로세스 생존)까지만 확실히 커버한다.
- [ ] `todakun://fortune/today`의 `"today"`가 `FortuneReportRoute.fortuneId`로 그대로 유효한 서버 값인지 확인 필요(다른 화면에서 쓰는 `fortuneId`가 UUID/날짜 형태일 가능성).
- [ ] 탭별 독립 백스택을 지원하지 않는 것(탭 전환 시 해당 탭 내부 이동 기록 초기화)이 UX상 허용되는지 확인 필요.
- [ ] `PushNotificationBanner`는 스텁만 만들어두므로, 실제 배너가 완성되기 전까지는 포그라운드 인앱 알림이 사용자에게 시각적으로 보이지 않는다(수신·상태 보유 로직 자체는 정상 동작). 담당자가 이어서 구현.
