# 설계 문서 — #59 하위 G-chat: ChatScreen + Navigation 3 최소 골격

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 G-chat (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: F-chat (`designs/issue-59-F-chat-viewmodel.md`) — `ChatState`/`ChatViewModel` 필요. 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

F-chat이 만든 `ChatState`를 실제로 그리고, **이 앱 최초의 Navigation 3 골격**을 세워 화면에 도달할 수 있게 만든다.

- 포함:
  - `ChatScreen` Composable (추천 칩 / 메시지 리스트 / 생각 중 / 타이핑 텍스트 / 입력창)
  - `ThinkingIndicator` 신규 Composable (점 3개 애니메이션 — designsystem에 없음)
  - `core:navigation`에 `NavKey` 정의 (`ChatKey`)
  - `app`에 `NavDisplay` + 백스택 + MainActivity 배선
  - 화면 문구 `strings.xml`
- 제외:
  - 히스토리 화면·라우트 (G-history)
  - 액션 카드(`ChatAction`) 렌더링 — Figma 미확정, 후속 이슈
  - 바텀 네비게이션 / 딥링크 / 화면 전환 애니메이션 / 로그인 라우트 (4절 참조)
  - Compose UI 테스트 (2-6 참조)

**파일 변경 개수: 8개 (신규 5, 수정 3)** — 10개 기준 충족.

### 이 단위의 위치

전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고.

## 2. 설계 및 실행 계획

### 2-1. 규칙 준수 근거

| 규칙 | 준수 방법 |
|------|-----------|
| `rules/00`: feature 간 직접 참조 금지 (P1) | `ChatScreen`은 네비게이션을 모른다. `onNavigateBack`/`onNavigateToHistory` 람다만 받는다. 라우트 조립은 전부 `app`에서 한다 |
| `rules/00`: 역방향 참조 금지 (P1) | `core:navigation`은 `NavKey` 선언만 갖고 feature/app에 의존하지 않는다 |
| `rules/40`: 디자인 값 하드코딩 금지 | 색·타이포는 `TodakunColor` / `TodakunTypography` 토큰만 사용. 여백 dp는 Figma 실측값을 화면 로컬 `private object` 상수로 모은다 |
| `rules/40`: 불필요한 리컴포지션 (P2) | 람다는 `viewModel::onSendClick` 형태의 메서드 참조로 넘겨 매 컴포지션 재생성을 피한다. `LazyColumn`에 `key = { it.id }` 지정 |
| `rules/40`: 리소스 네이밍 | 신규 drawable 없음 (기존 `ic_*` 재사용) |
| `rules/30`: designsystem 컴포넌트 재사용 | 헤더/입력창/사용자 버블/추천 칩 4개를 **그대로** 쓴다. 신규 작성은 `ThinkingIndicator` 하나뿐 |

### 2-2. **[핵심 트레이드오프] Navigation 3 골격을 이 단위에 넣을 것인가**

현재 상태 (조사 결과): `app/build.gradle.kts`에 nav3 4종 의존성이 **이미 선언돼 있으나** 코드가 0줄이다. `core:navigation`은 `src/main`에 `AndroidManifest.xml`만 있고 `kotlinx-serialization` 플러그인만 적용돼 있다(= 직렬화 가능한 라우트 키를 담을 자리로 설계돼 있었다는 뜻). `MainActivity`는 여전히 `Greeting("Android")`를 그린다.

| 옵션 | 내용 | 장점 | 단점 | 판정 |
|------|------|------|------|------|
| **A. 별도 선행 단위 `E-nav`로 분리** | 네비게이션만 먼저 만든다 | 관심사 분리, G-chat이 작아짐 | **목적지가 0개인 `NavDisplay`는 검토도 테스트도 불가능하다.** 리뷰어가 "이게 맞는지" 판단할 근거가 없고, 첫 실제 화면을 붙이는 순간 어차피 고쳐진다. 블로킹 단위만 하나 늘어난다 | 기각 |
| **B. 네비게이션 없이 Composable만** | `ChatScreen`을 `@Preview`로만 확인 | 가장 작음 | `conversationId` 전달 경로가 검증되지 않는다. G-history의 "클릭 시 conversationId 넘겨 이동"이 통째로 검증 불가가 되어 리스크가 뒤로 밀린다 | 기각 |
| **C. 최소 골격을 이 단위에 포함 ← 채택** | 라우트 키 1개 + 백스택 + `NavDisplay` + MainActivity 배선 | ChatScreen이 **실기기에서 실제로 열린다.** `conversationId` 인자 전달 경로가 G-chat 안에서 끝까지 검증된다. G-history는 키 1개와 entry 1개만 추가하면 된다 | **채택** |

**"최소"의 경계선 (넘지 않는다)**

| 만든다 | 만들지 않는다 |
|--------|--------------|
| `ChatKey(conversationId: String?)` 1개 | 앱 전역 라우트 그래프, 로그인/온보딩/홈 라우트 |
| `SnapshotStateList` 백스택 1개 | 중첩 그래프, 멀티 백스택 |
| `entryProvider` + entry 1개 | 화면 전환 애니메이션, Predictive Back |
| `rememberViewModelStoreNavEntryDecorator` (VM 스코프) | 딥링크, `SavedState` 커스텀 직렬화 |
| — | 바텀 네비게이션 (`TodakunBottomNavigation`은 이미 있으나 붙일 화면이 없다) |

앱 전역 네비게이션 아키텍처는 화면이 3개 이상 생긴 뒤에 그 사용례를 보고 정하는 편이 낫다. 지금 정하면 추측이다. (YAGNI)

### 2-3. `conversationId`를 ViewModel에 전달하는 방법

Navigation 3에서 **키 객체 자체가 인자 홀더**다. Nav2처럼 `SavedStateHandle`에 라우트 인자가 자동 주입되지 않는다.

| 옵션 | 단점 | 판정 |
|------|------|------|
| `@AssistedInject` + `hiltViewModel(creationCallback = ...)` | Factory 인터페이스 + assisted 애노테이션 + 호출부 보일러플레이트. 이 앱 최초 Hilt-Compose 배선인데 난이도만 올린다 | 기각 |
| `SavedStateHandle`에 수동 저장 | 백스택 키와 값이 이중 저장되어 어긋날 여지 (F-chat 2-5) | 기각 |
| **`ChatScreen(conversationId)` + `LaunchedEffect(Unit) { viewModel.load(it) }` ← 채택** | 초기화가 부수효과에 의존 | VM이 nav entry에 스코프되므로 entry당 정확히 1회 실행된다. 3줄. **채택** |

### 2-4. 화면 구성

```
Column (fillMaxSize, background = TodakunColor.white)
├── TodakunChatHeader(
│       title = "토닥이",
│       freeChatUsed = quota?.used ?: 0, freeChatTotal = quota?.limit ?: 0,
│       onCloseClick = onNavigateBack,
│       onChatIconClick = viewModel::startNewConversation,
│       onNotesIconClick = onNavigateToHistory)
├── LazyColumn(weight 1f, state = listState)
│     ├── item  greeting                    (state.greeting이 비어있지 않을 때)
│     ├── items(messages, key = { it.id })
│     │     USER      → Row(Arrangement.End) { TodakunChatUserInputBubble(content) }
│     │     그 외      → Text(content, body2Regular)      ← 봇 답변은 말풍선이 아니다
│     ├── item  phase != IDLE 일 때
│     │     THINKING  → ThinkingIndicator()
│     │     TYPING    → Text(state.streamingText, body2Regular)
│     └── item  messages.isEmpty() && phase == IDLE 일 때
│           FlowRow { suggestions.forEach { TodakunChatExampleChip("${emoji} ${label}") } }
└── TodakunChatInputField(value = input, onValueChange, onSendClick)
```

**결정 근거**

- **봇 답변은 `Text`로만 그린다.** Figma에 봇 말풍선이 없다. 새 컴포넌트를 만들지 않는다.
- **스트리밍 텍스트는 `messages`의 다음 item으로 그린다.** F-chat이 State를 분리해 둔 덕분에(2-6) 문자 하나가 늘 때 `LazyColumn`의 다른 item은 리컴포즈되지 않는다.
- **`THINKING`과 `TYPING`을 같은 item 슬롯에서 교체**한다. 인디케이터가 사라지고 텍스트가 나타나는 위치가 같아 화면이 튀지 않는다.
- **추천 칩은 대화가 비어 있을 때만.** 진입 직후에만 보이고 첫 전송 후 사라진다. 히스토리에서 진입하면 `messages`가 이미 있으므로 자동으로 안 보인다 — 별도 분기가 필요 없다.
- **자동 스크롤은 화면 로컬 `LaunchedEffect`로 처리한다.** 사이드이펙트 채널로 올리면 초당 40여 개 이벤트가 흘러 채널만 시끄러워진다. 스크롤 위치는 순수 UI 관심사다.

  ```kotlin
  LaunchedEffect(state.messages.size, state.streamingText) {
      if (listState.layoutInfo.totalItemsCount > 0) {
          listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
      }
  }
  ```

  `state.streamingText`가 키에 들어가므로 타이핑 중에도 따라 내려간다. 사용자가 위로 스크롤 중일 때 강제로 끌어내리는 문제는 4절 후속 항목.

### 2-5. `ThinkingIndicator` 설계

designsystem에 없고 이 화면 전용이므로 `feature:chat`에 만든다. 점 3개가 순차로 흐려졌다 진해지는 형태.

`rememberInfiniteTransition` + 점마다 다른 `initialStartOffset`으로 만든다. `Animatable` 3개를 직접 돌리거나 코루틴을 수동으로 굴리지 않는다 — Compose가 제공하는 것으로 충분하다(사다리 3번). 약 30줄.

### 2-6. Compose UI 테스트를 이 단위에서 작성하지 않는 이유

이 저장소에 Compose UI 테스트 선례가 0건이고, `androidTest`를 CI에서 돌리는 설정도 없다(에뮬레이터 미구성). 여기서 첫 UI 테스트 인프라까지 세우면 이 단위의 실제 목적(화면을 띄운다)이 뒤로 밀린다.

대신 **검증 경로를 명확히 남긴다**:
1. `@Preview` 4종 — 빈 상태(추천 칩) / 생각 중 / 타이핑 중 / 대화 이어보기. 상태별 렌더링을 눈으로 고정한다.
2. 실기기 스모크 — dev 서버 대상 전송 → **delta가 점진적으로 도착하는지**(C1/D의 최우선 리스크)와 타이핑이 자연스러운지 확인. **이 단위의 실질 완료 조건이다.**
3. 상태 기계 자체는 F-chat의 `ChatViewModelTest`가 이미 커버한다.

UI 테스트 인프라 도입은 별도 이슈로 등록한다(4절).

### 2-7. 실행 계획

1. `core/navigation`에 nav3 runtime 의존성 + `TodakunNavKey.kt` 작성
2. `app`에 `TodakunNavDisplay.kt` 작성, `MainActivity` 배선 → `./gradlew :app:assembleDebug` 통과
3. `ThinkingIndicator.kt` + `@Preview`로 애니메이션 확인
4. `strings.xml` → `ChatScreen.kt` 작성, `@Preview` 4종으로 상태별 렌더링 확인
5. `./gradlew :feature:chat:ktlintCheck :app:assembleDebug` 통과
6. **[수동 필수]** 실기기/에뮬레이터 스모크: 전송 → 생각 중 → 타이핑 → 완료, 그리고 두 번째 메시지가 같은 대화에 들어가는지

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/navigation/
├── build.gradle.kts                                        (수정) nav3 runtime 의존성
└── src/main/java/com/kikidan/navigation/
    └── TodakunNavKey.kt                                    (신규) ChatKey

app/src/main/java/com/kikidan/todakun/
├── MainActivity.kt                                         (수정) TodakunNavDisplay 호출
└── navigation/TodakunNavDisplay.kt                         (신규) 백스택 + entryProvider

feature/chat/src/main/
├── res/values/strings.xml                                  (신규) 화면 문구
└── java/com/kikidan/chat/
    ├── ChatScreen.kt                                       (신규) ChatScreen + 내부 private Composable
    └── ThinkingIndicator.kt                                (신규)
```

- [ ] `core/navigation/build.gradle.kts` — dependencies에 추가

```kotlin
// NavKey를 구현한 라우트 키를 공개하므로 api로 노출한다.
api(libs.androidx.navigation3.runtime)
```

- [ ] `core/navigation/src/main/java/com/kikidan/navigation/TodakunNavKey.kt` (신규)

```kotlin
package com.kikidan.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** conversationId가 null이면 새 대화, 있으면 히스토리에서 이어가기. */
@Serializable
data class ChatKey(
    val conversationId: String? = null,
) : NavKey
```

- [ ] `app/src/main/java/com/kikidan/todakun/navigation/TodakunNavDisplay.kt` (신규)

```kotlin
package com.kikidan.todakun.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.Modifier
import com.kikidan.chat.ChatScreen
import com.kikidan.navigation.ChatKey

/**
 * 앱 전역 네비게이션의 최소 골격. 채팅 화면 하나에 도달하는 경로만 만든다.
 * 화면이 늘어나면 그때 그래프 구조를 정한다.
 */
@Composable
fun TodakunNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(ChatKey())

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<ChatKey> { key ->
                    ChatScreen(
                        conversationId = key.conversationId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        // G-history에서 ChatHistoryKey를 push하도록 교체한다.
                        onNavigateToHistory = {},
                    )
                }
            },
    )
}
```

> ⚠️ nav3 1.1.3의 정확한 API 표기(`rememberNavBackStack` vs `remember { mutableStateListOf() }`, `entryDecorators` 파라미터명, decorator 함수명)는 **구현 착수 시 IDE 자동완성으로 확인**한다. 확인 실패 시 폴백은 4절.

- [ ] `app/src/main/java/com/kikidan/todakun/MainActivity.kt` (수정) — `Greeting` 제거, `Scaffold` 내부를 교체

```kotlin
setContent {
    TodakunTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            TodakunNavDisplay(modifier = Modifier.padding(innerPadding))
        }
    }
}
```

> `app`의 `com.kikidan.todakun.ui.theme.TodakunTheme`과 designsystem의 `com.kikidan.designsystem.theme.TodakunTheme`이 별개로 존재한다. 이 단위에서는 기존 app 테마를 그대로 두고, 통합은 4절 후속 항목으로 둔다(범위 확대 방지).

- [ ] `feature/chat/src/main/res/values/strings.xml` (신규)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="chat_header_title">토닥이</string>
    <string name="chat_thinking_description">토닥이가 생각하고 있어요</string>
</resources>
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ThinkingIndicator.kt` (신규)

```kotlin
package com.kikidan.chat

// 점 3개가 순차로 흐려졌다 진해진다. designsystem에 없어 이 화면 전용으로 만든다.
@Composable
internal fun ThinkingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "thinking")
    Row(
        modifier = modifier.semantics { contentDescription = 생각중_문구 },
        horizontalArrangement = Arrangement.spacedBy(DotSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DOT_COUNT) { index ->
            val alpha by transition.animateFloat(
                initialValue = MinAlpha,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = CycleMillis, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * CycleMillis / DOT_COUNT),
                    ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(DotSize)
                    .clip(CircleShape)
                    .background(TodakunColor.gray400.copy(alpha = alpha)),
            )
        }
    }
}
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ChatScreen.kt` (신규)

```kotlin
package com.kikidan.chat

@Composable
fun ChatScreen(
    conversationId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // nav entry당 정확히 1회 (설계 2-3)
    LaunchedEffect(Unit) { viewModel.load(conversationId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ChatSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    ChatScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        onSuggestionClick = viewModel::onSuggestionClick,
        onNewConversationClick = viewModel::startNewConversation,
        onCloseClick = onNavigateBack,
        onHistoryClick = onNavigateToHistory,
        modifier = modifier,
    )
}

// 상태 없는 오버로드. @Preview와 (도입 시) UI 테스트가 이쪽을 쓴다.
@Composable
internal fun ChatScreen(
    state: ChatState,
    snackbarHostState: SnackbarHostState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onNewConversationClick: () -> Unit,
    onCloseClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

  화면 본문은 2-4의 구조를 그대로 구현한다. 내부 private Composable:
  - `ChatMessageItem(message: ChatMessage)` — USER면 `Row(Arrangement.End) { TodakunChatUserInputBubble }`, 아니면 `Text(body2Regular)`
  - `SuggestionChips(suggestions, onClick)` — `FlowRow` + `TodakunChatExampleChip("${emoji} ${label}")`, 탭 시 `seedPrompt` 전달
  - 여백 상수는 파일 하단 `private object ChatScreenDefaults`에 모은다 (`TodakunChatInputFieldDefaults`와 같은 패턴)

- [ ] `@Preview` 4종 — 빈 상태(추천 칩) / `THINKING` / `TYPING`(streamingText 중간값) / 대화 이어보기(messages 3개)

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[높음] Navigation 3 (1.1.3) API 표기 미검증** — 이 앱 최초 도입이라 `rememberNavBackStack`/`entryDecorators`/`rememberViewModelStoreNavEntryDecorator`의 정확한 시그니처를 코드로 확인하지 못했다. **구현 착수 시 가장 먼저 확인할 항목.**
  폴백: 백스택을 `remember { mutableStateListOf<NavKey>(ChatKey()) }`로 직접 만들고 `NavDisplay(backStack = ..., entryProvider = ...)`의 최소 형태만 쓴다. 상태 복원은 포기하고 후속 이슈로 넘긴다(이 단위 목적인 "화면 도달"은 충족).
- [ ] **[높음] delta 점진 도착 여부(C1 최상위 리스크)가 이 화면에서 처음 눈에 보인다** — OkHttp 엔진이 응답을 버퍼링하면 답변이 종료 시점에 통째로 도착하고, `typewriter()`는 그것을 "거대한 단일 청크"로 받아 catch-up 속도로 쏟아낸다. 즉 **화면은 죽지 않지만 타이핑 효과가 사라진다.** 실기기 스모크에서 반드시 확인하고, 실패 시 C1 문서의 대안(CIO 엔진 / `Accept-Encoding: identity`)으로 되돌아간다.
- [ ] **[중간] Figma 확인 필요 — 추천 칩의 위치** — 본 설계는 리스트 최하단(입력창 바로 위)에 둔다. Figma가 인사말 바로 아래에 두는 구성이면 item 순서만 바꾸면 된다.
- [ ] **[중간] 자동 스크롤이 사용자 스크롤을 뺏는다** — 타이핑 중 사용자가 위로 올려 이전 대화를 보면 매 틱 아래로 끌려 내려간다. 수정하려면 "이미 바닥 근처일 때만 스크롤"(`listState.layoutInfo`로 판정) 조건을 추가한다(약 5줄). 실기기에서 거슬리면 그때 넣는다.
- [ ] **[중간] `app`과 `core:designsystem`에 `TodakunTheme`이 각각 존재한다** — 채팅 화면은 designsystem 토큰을 직접 참조하므로 당장 문제는 없지만, 테마가 두 개인 상태는 오래 두면 안 된다. 통합 이슈 별도 등록 권장.
- [ ] **[중간] Compose UI 테스트 인프라 부재** — 2-6 참조. `androidTest` CI 실행 환경 도입을 별도 이슈로 등록.
- [ ] **[낮음] `ChatAction` 렌더링 미포함** — Figma에 액션 카드 디자인이 없다. `ChatMessage.action`으로 State에는 이미 담기므로 후속 이슈에서 UI만 추가하면 된다.
- [ ] **[낮음] 헤더 닫기 버튼의 목적지** — 현재 백스택 최하단이 `ChatKey`라 닫기를 누르면 갈 곳이 없다(`removeLastOrNull`이 빈 백스택을 만들 수 있음). 홈 화면이 생기기 전까지는 **닫기 시 앱 종료(`finish()`)** 또는 무시가 필요하다. 구현 시 `backStack.size > 1`일 때만 pop하도록 가드를 둔다.
- [ ] **[낮음] `NavDisplay`의 시작 목적지가 채팅** — 로그인/홈이 없어 임시로 채팅을 시작 화면으로 둔다. 로그인 라우트가 생기면 교체될 자리다.
