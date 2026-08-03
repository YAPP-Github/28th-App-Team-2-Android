# 설계 문서 — #59 하위 G-history: HistoryScreen + 히스토리 라우트

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 G-history (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: G-chat (`designs/issue-59-G-chat-screen.md`) — Navigation 3 골격 필요 / F-history (`designs/issue-59-F-history-viewmodel.md`) — `HistoryState` 필요. 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

대화 목록 화면을 그리고, 채팅 화면과 서로 오갈 수 있게 라우트를 연결한다. **이 단위가 완료되면 #59의 화면 흐름이 닫힌다.**

- 포함:
  - `HistoryScreen` Composable (목록 / 빈 목록 / 로딩)
  - `ChatHistoryKey` 라우트 추가 + `NavDisplay`에 entry 등록
  - G-chat이 비워 둔 `onNavigateToHistory` 연결, 항목 클릭 시 `ChatKey(conversationId)` push
  - 화면 문구 추가
- 제외:
  - 삭제 확인 다이얼로그 (4절 — Figma 미확인)
  - 검색 / 정렬 UI / 스와이프 삭제 / 당겨서 새로고침 — 요구사항에 없다 (YAGNI)
  - Compose UI 테스트 (G-chat 2-6과 동일한 이유)

**파일 변경 개수: 4개 (신규 1, 수정 3)** — 10개 기준 충족.

### 이 단위의 위치 (전체 그래프 마무리)

전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고. 이 단위로 #59의 9개 작업 단위 의존 그래프가 닫힌다.

## 2. 설계 및 실행 계획

### 2-1. 규칙 준수 근거

| 규칙 | 준수 방법 |
|------|-----------|
| `rules/00`: feature 간 참조 금지 (P1) | 히스토리와 채팅이 같은 `feature:chat` 모듈에 있다(F-history 2-2). 라우트 키는 `core:navigation`, 조립은 `app` |
| `rules/40`: 디자인 값 하드코딩 금지 | `TodakunChatHistoryItem`을 **그대로** 재사용한다. 여백/구분선/뱃지가 전부 컴포넌트 안에 있어 화면에서 새로 정의할 값이 거의 없다 |
| `rules/40`: 불필요한 리컴포지션 (P2) | `LazyColumn`에 `key = { it.id }`. 항목 람다는 `remember` 없이도 강한 건너뛰기로 커버되지만, 삭제/클릭 콜백은 화면 상단에서 한 번만 만들어 내려보낸다 |
| `rules/30`: ViewModel은 상태 관리에 집중 | 상대 시각 포맷은 화면 책임(F-history 2-5) |

### 2-2. 상대 시각("30분 전")을 어떻게 만드는가

`TodakunChatHistoryItem`은 `relativeTime: String`을 받고, 도메인은 `Instant`를 준다. 사이를 누가 메우는가.

| 옵션 | 판정 |
|------|------|
| ViewModel이 문자열로 변환해 State에 담는다 | 기각 — 시간이 지나도 갱신되지 않는 죽은 문자열이 State에 남는다. VM에 문구가 하드코딩되거나 `Context`가 들어온다 |
| 직접 포맷 함수를 작성한다 (분/시간/일 분기) | 기각 — 로케일·복수형·"어제" 처리를 손으로 다시 만드는 일이다 |
| **Android 플랫폼의 `DateUtils.getRelativeTimeSpanString` ← 채택** | 한 줄. 시스템 로케일에 맞춰 "30분 전", "어제" 등을 반환한다. 새 의존성 0개, 테스트할 자체 로직 0줄 |

```kotlin
@Composable
private fun rememberRelativeTime(instant: Instant): String =
    remember(instant) {
        DateUtils
            .getRelativeTimeSpanString(
                instant.toEpochMilli(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
            ).toString()
    }
```

`remember(instant)`로 항목당 1회만 계산한다. 화면이 열려 있는 동안 값이 갱신되지 않지만, 목록은 진입 시 재조회되므로 실질 문제가 없다.

### 2-3. 화면 구성

```
Column (fillMaxSize, background = TodakunColor.white)
├── TodakunSubHeader(...)                       ← designsystem 기존 컴포넌트 (제목 + 뒤로가기)
└── when {
      isLoading                → 로딩 인디케이터 (중앙)
      conversations.isEmpty()  → 빈 상태 (중앙 안내 문구)
      else                     → LazyColumn(items, key = { it.id }) {
                                     TodakunChatHistoryItem(
                                         title, relativeTime, isUnread = unread,
                                         onClick = { onConversationClick(it.id) },
                                         onDeleteClick = { viewModel.delete(it.id) })
                                 }
    }
```

- **헤더**: `core:designsystem`에 `TodakunSubHeader`가 이미 있다. 구현 시 실제 파라미터를 확인해 재사용하고, 맞지 않으면 `TodakunChatHeader`가 아닌 최소 `Row`로 대체한다(새 designsystem 컴포넌트를 만들지 않는다).
- **빈 상태**: 아이콘 없이 문구 한 줄(`TodakunTypography.body2Regular`, `TodakunColor.gray500`). Figma에 일러스트가 있으면 그때 이미지를 넣는다. 지금 없는 에셋을 가정해 만들지 않는다.
- **로딩**: 첫 진입 시 잠깐이므로 `CircularProgressIndicator` 하나. 스켈레톤 UI는 만들지 않는다.
- **구분선**: `TodakunChatHistoryItem`이 내부에 `HorizontalDivider`를 갖고 있으므로 화면에서 넣지 않는다. (컴포넌트를 실제로 읽어 확인함)

### 2-4. 라우팅 연결

G-chat이 세운 골격에 키 하나와 entry 하나를 더한다. **`NavDisplay`의 구조는 건드리지 않는다.**

```
ChatKey(conversationId)  ──(노트 아이콘)──▶  ChatHistoryKey
ChatHistoryKey  ──(항목 클릭)──▶  ChatKey(conversationId = 클릭한 id)
```

`ChatHistoryKey`는 인자가 없으므로 `data object`다.

**주의: 항목 클릭 시 `ChatKey`를 push하면 백스택에 채팅이 두 개 쌓인다** (시작 화면 `ChatKey()` → `ChatHistoryKey` → `ChatKey("c-1")`). 뒤로 가기를 누르면 히스토리로 돌아오므로 흐름 자체는 자연스럽다. 시작 화면을 대체하는 replace 동작은 홈 화면이 생겨 시작 목적지가 바뀌면 자연히 해소되므로 지금 특수 처리하지 않는다.

### 2-5. 검증 방법

G-chat 2-6과 동일하게 Compose UI 테스트는 작성하지 않는다(인프라 부재). 대신:

1. `@Preview` 3종 — 목록 있음(안 읽음 항목 포함) / 빈 목록 / 로딩
2. 실기기 스모크 (**이 단위의 실질 완료 조건**):
   - 채팅 → 노트 아이콘 → 히스토리 → 항목 탭 → **그 대화의 과거 메시지가 채팅 화면에 실제로 뜨는지** (F-chat의 `getConversationDetail` 경로가 여기서 처음 끝까지 검증된다)
   - 삭제 아이콘 → 즉시 사라지는지 / 기내모드에서 되돌아오는지 (F-history의 롤백 경로)
3. 상태 기계는 F-history의 `HistoryViewModelTest`가 커버한다.

### 2-6. 실행 계획

1. `core/navigation/.../TodakunNavKey.kt`에 `ChatHistoryKey` 추가
2. `strings.xml`에 문구 추가
3. `HistoryScreen.kt` 작성 → `@Preview` 3종 확인
4. `TodakunNavDisplay.kt`에 entry 추가 + G-chat이 비워 둔 `onNavigateToHistory` 연결
5. `./gradlew :feature:chat:ktlintCheck :app:assembleDebug` 통과
6. **[수동 필수]** 2-5의 스모크 시나리오 2종

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/navigation/src/main/java/com/kikidan/navigation/
└── TodakunNavKey.kt                                        (수정) ChatHistoryKey 추가

app/src/main/java/com/kikidan/todakun/navigation/
└── TodakunNavDisplay.kt                                    (수정) 히스토리 entry + 양방향 연결

feature/chat/src/main/
├── res/values/strings.xml                                  (수정) 히스토리 문구
└── java/com/kikidan/chat/history/
    └── HistoryScreen.kt                                    (신규)
```

- [ ] `core/navigation/src/main/java/com/kikidan/navigation/TodakunNavKey.kt` (수정) — 추가

```kotlin
@Serializable
data object ChatHistoryKey : NavKey
```

- [ ] `feature/chat/src/main/res/values/strings.xml` (수정) — 추가

```xml
<string name="chat_history_title">대화 기록</string>
<string name="chat_history_empty">아직 토닥이와 나눈 대화가 없어요.</string>
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/history/HistoryScreen.kt` (신규)

```kotlin
package com.kikidan.chat.history

@Composable
fun HistoryScreen(
    onConversationClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.load() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is HistorySideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    HistoryScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onConversationClick = onConversationClick,
        onDeleteClick = viewModel::delete,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// 상태 없는 오버로드. @Preview가 이쪽을 쓴다.
@Composable
internal fun HistoryScreen(
    state: HistoryState,
    snackbarHostState: SnackbarHostState,
    onConversationClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
)
```

  본문은 2-3의 구조를 그대로 구현한다. 목록 부분:

```kotlin
LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(state.conversations, key = { it.id }) { conversation ->
        TodakunChatHistoryItem(
            title = conversation.title,
            relativeTime = rememberRelativeTime(conversation.lastMessageAt),
            isUnread = conversation.unread,
            onClick = { onConversationClick(conversation.id) },
            onDeleteClick = { onDeleteClick(conversation.id) },
        )
    }
}
```

  `rememberRelativeTime`은 2-2의 private Composable을 같은 파일에 둔다.

- [ ] `app/src/main/java/com/kikidan/todakun/navigation/TodakunNavDisplay.kt` (수정) — `entryProvider` 교체

```kotlin
entryProvider {
    entry<ChatKey> { key ->
        ChatScreen(
            conversationId = key.conversationId,
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateToHistory = { backStack.add(ChatHistoryKey) },   // G-chat의 빈 람다를 채운다
        )
    }
    entry<ChatHistoryKey> {
        HistoryScreen(
            onConversationClick = { conversationId -> backStack.add(ChatKey(conversationId)) },
            onNavigateBack = { backStack.removeLastOrNull() },
        )
    }
}
```

- [ ] `@Preview` 3종 — 목록(안 읽음 포함) / 빈 목록 / 로딩

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[중간] 삭제 확인 다이얼로그 필요 여부** — F-history 4절과 동일 항목. 파괴적 동작에 확인 절차가 없다. Figma 확인 필요. 필요하다면 **VM 변경 없이 이 화면에서만** 처리한다: `remember { mutableStateOf<String?>(null) }`로 대상 id를 들고 있다가 `TodakunDialog` 확인 시 `onDeleteClick(id)`를 호출한다(약 15줄).
- [ ] **[중간] `TodakunSubHeader`의 실제 시그니처 미확인** — 파일 존재만 확인했고 파라미터는 읽지 않았다. 제목+뒤로가기 형태가 아니면 최소 `Row`로 대체하되 **designsystem에 새 헤더 컴포넌트를 추가하지 않는다**(이 단위의 범위가 아니다).
- [ ] **[중간] 빈 목록 상태의 Figma 디자인 미확인** — 문구 한 줄로 두었다. 일러스트/CTA 버튼("토닥이와 대화하기")이 있으면 추가 에셋과 이동 경로가 필요하다.
- [ ] **[중간] 채팅 복귀 시 목록 갱신** — F-history 2-4/4절과 동일 항목. `LaunchedEffect(Unit)`이 백스택 복귀 시 재실행되지 않으면 방금 나눈 대화가 목록에 없다. **실기기 스모크에서 이 동작을 반드시 확인하고**, 갱신되지 않으면 그때 `Lifecycle.RESUMED` 관찰을 붙인다(약 5줄).
- [ ] **[중간] `unread` 뱃지가 사라지는 시점** — 대화를 열었다가 돌아왔을 때 뱃지가 사라져야 자연스럽다. 서버가 상세 조회 시 읽음 처리하고 클라이언트가 목록을 재조회해야 반영된다 — 위 항목과 같은 원인으로 묶인다.
- [ ] **[낮음] 백스택에 채팅이 두 번 쌓인다** — 2-4 참조. 홈 화면 도입 시 자연 해소되므로 지금 특수 처리하지 않는다. 뒤로 가기 동작이 어색하다는 피드백이 나오면 그때 다룬다.
- [ ] **[낮음] 목록 항목의 `title`이 비어 있는 경우** — 서버가 첫 메시지로 제목을 만든다고 가정했다. 빈 문자열이 오면 항목이 시각만 보이는 형태가 된다. 백엔드에 보장 여부 확인 권장(폴백이 필요하면 `GetConversationsUseCase`가 아니라 화면에서 `title.ifBlank { "제목 없는 대화" }` 한 줄).
