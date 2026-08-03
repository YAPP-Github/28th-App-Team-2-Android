# 설계 문서 — #59 하위 G-chat: ChatScreen (Figma 반영, Navigation 3 제외)

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #78 (상위 이슈: #59)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **2026-08-04 갱신**: Navigation 3 범위 제외 + Figma 화면 재설계(사용자 지시)
- **상태**: 검토 대기
- **선행**: F-chat (`designs/issue-77-chat-viewmodel.md`) — `ChatState`/`ChatViewModel` 필요. 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

F-chat이 만든 `ChatState`를 Figma 디자인대로 그리고, **`MainActivity`가 그 화면 하나를 직접 띄운다.**

- 포함:
  - `ChatScreen` Composable — 진입 상태(캐릭터 + 제목 + 추천 칩 6개) / 대화 상태(메시지·생각 중·타이핑) / 입력창 / 바텀 네비게이션
  - `ChatGreetingOverlay` 신규 Composable — 최초 진입 시 3초 뜨고 사라지는 그리팅 말풍선 (Figma 노드 B)
  - `ThinkingIndicator` 신규 Composable (점 3개 애니메이션 — designsystem에 없음)
  - `MainActivity` 배선 (`Greeting("Android")` 제거)
  - 화면 문구 `strings.xml`
- 제외:
  - **Navigation 3 전부** — 라우트 키·`NavDisplay`·백스택·`core:navigation` 변경 없음 (2-2)
  - 히스토리 화면·이동 경로 (G-history — 2-2의 결정으로 보류 상태가 된다)
  - 탭 전환 (바텀 네비게이션은 표시만, `onItemSelect`는 no-op — 2-6)
  - 액션 카드(`ChatAction`) 렌더링 — Figma 미확정, 후속 이슈
  - 캐릭터 이미지 asset export — Figma 이미지 추출은 이 문서 범위 밖. 플레이스홀더로 자리만 잡는다 (4절)
  - Compose UI 테스트 (2-8 참조)

**파일 변경 개수: 5개 (신규 4, 수정 1)** — 이전 설계의 8개에서 3개 줄었다. `core:navigation`과 `app/navigation/`을 통째로 들어냈기 때문이다.

### 이 단위의 위치

전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고. 이 단위는 F-chat에만 컴파일 의존한다(이전 설계와 동일). 대신 **G-history가 이 단위에 걸던 "Navigation 3 골격" 의존이 사라져 보류 상태가 된다** — 2-2 참조.

## 2. 설계 및 실행 계획

### 2-1. 규칙 준수 근거

| 규칙 | 준수 방법 |
|------|-----------|
| `rules/00`: feature → domain만 허용, feature 간 참조 금지 (P1) | `feature:chat`은 `core:domain` + `core:designsystem`만 의존한다. `core:navigation` 의존이 **이번 변경으로 아예 사라졌다** |
| `rules/00`: DI는 Hilt | `@AndroidEntryPoint MainActivity` + `hiltViewModel()`. 수동 팩토리 없음 (2-3) |
| `rules/40`: 디자인 값 하드코딩 금지 | 색·타이포는 `TodakunColor`/`TodakunTypography` 토큰만 사용. 스크림 40%는 토큰에 없어 근접값을 쓰고 근거를 남긴다 (2-5). 여백 dp는 화면 로컬 `private object`에 모은다 |
| `rules/40`: 불필요한 리컴포지션 (P2) | 람다는 `viewModel::onSendClick` 형태의 메서드 참조. `LazyColumn`에 `key = { it.id }` |
| `rules/40`: 리소스 네이밍 | 신규 drawable 없음 (기존 `ic_*` 재사용, 캐릭터는 플레이스홀더) |
| `rules/30`: designsystem 컴포넌트 재사용 | 헤더/입력창/사용자 버블/추천 칩/바텀네비 **5개를 그대로** 쓴다. 신규 작성은 `ThinkingIndicator`와 `ChatGreetingOverlay` 둘뿐 |

### 2-2. **[핵심 결정] Navigation 3를 이 단위에서 뺀다**

**사용자 확정 지시**(2026-08-04)다. 설계상 근거를 남겨 둔다.

이전 설계의 **[높음] 리스크였던 "Navigation 3 (1.1.3) API 표기 미검증"이 이걸로 해소된다 — 문제를 푼 게 아니라 회피한 것이다.** `rememberNavBackStack` / `entryDecorators` / `rememberViewModelStoreNavEntryDecorator`의 정확한 시그니처를 이 단위에서 확인할 필요 자체가 없어졌다. 이 앱 최초의 nav3 도입을, 목적지가 하나뿐인 시점에 강행할 이유가 없다.

**대가**: `conversationId`를 인자로 전달하는 경로가 검증되지 않는다. 이전 설계가 옵션 B(네비게이션 없이 Composable만)를 기각했던 근거가 바로 이것이었다. 지금은 다르게 판단한다 — 그 경로를 쓰는 유일한 소비자가 G-history인데, **G-history 자체가 함께 보류되므로 검증할 대상이 아직 존재하지 않는다.** 존재하지 않는 소비자를 위한 경로를 미리 검증하는 것은 순서가 뒤집힌 일이다.

`ChatScreen`은 `conversationId: String?` 파라미터를 **그대로 유지한다.** 지금은 `MainActivity`가 항상 `null`을 넘기지만, 시그니처를 지워 두면 G-history 재개 시 화면 진입 계약을 다시 설계해야 한다. 파라미터 하나를 남기는 비용은 0이다.

**이 결정의 파급 (반드시 인지할 것)**

| 문서 | 영향 |
|------|------|
| `designs/issue-80-history-screen.md` | 선행 조건이던 Navigation 3 골격이 사라져 **착수 불가**. 문서는 그대로 두고 "선행" 줄에 보류 사유만 기록 |
| `designs/issue-79-history-viewmodel.md` | 화면이 없으면 붙을 곳이 없어 함께 보류. 동일 처리 |
| `designs/issue-59-task-dependency-graph.md` | 그래프 재작성 없이 "2026-08-04 결정" 절만 추가 |

### 2-3. **[핵심 결정] Navigation 3 없이 `ChatScreen`을 어떻게 띄우는가**

| 옵션 | 내용 | 판정 |
|------|------|------|
| **A. `MainActivity.setContent`가 `ChatScreen`을 직접 그린다 ← 채택** | `TodakunTheme { ChatScreen(conversationId = null, ...) }` | **채택.** 화면이 하나뿐이므로 라우터가 결정할 것이 없다. 코드 3줄. (YAGNI — 사다리 1번) |
| B. 직접 만든 `when(screen)` 화면 전환기 | `enum class Screen`을 `remember`로 들고 분기 | 기각 — 목적지가 1개인 분기문은 분기가 아니다. 화면이 늘어나면 어차피 nav3로 갈아탈 것이고, 그때 이 코드는 통째로 삭제된다 |
| C. Fragment / 별도 Activity | 기각 — Compose 단일 Activity 구조를 되돌리는 일이다. `feature:auth`도 이미 Compose다 |

**`hiltViewModel()`이 Nav3 없이 동작하는가 — 확인 필요 사항**

`MainActivity`는 이미 `@AndroidEntryPoint`이고 `app/build.gradle.kts`에 `libs.hilt.navigation.compose`가 있다. `hiltViewModel()`은 `LocalViewModelStoreOwner`(= Activity)를 찾아 `@HiltViewModel`을 생성하므로 Nav3 없이 동작하는 것이 정상이다. **다만 이 저장소 최초의 Hilt-Compose 배선이므로 구현 착수 시 가장 먼저 확인한다** (4절). 폴백은 `MainActivity`에서 `by viewModels<ChatViewModel>()`로 받아 `ChatScreen(viewModel = ...)`에 넘기는 것이다(2줄).

**VM 스코프가 Activity가 된다 (Nav3 entry가 아니라)**. 실질 차이는 없다 — 화면이 하나뿐이라 Activity 생명주기 = 화면 생명주기다. 화면이 늘면 nav3 도입과 함께 자연히 entry 스코프로 옮겨간다.

**`conversationId` 주입은 이전 설계 그대로** `LaunchedEffect(Unit) { viewModel.load(conversationId) }`다. Activity 스코프 VM + `LaunchedEffect(Unit)`이므로 컴포지션당 1회 실행된다. F-chat 2-5의 "`SavedStateHandle`을 쓰지 않는다"는 결정도 그대로 유효하다.

### 2-4. 화면 구성 (Figma 노드 A — 1069:14000)

```
Box (fillMaxSize, background = TodakunColor.white)
│
├── Column (fillMaxSize, safeDrawingPadding)          ← 시스템 바 + IME를 한 번에 처리
│   ├── TodakunChatHeader(
│   │       title = "토닥이",
│   │       freeChatUsed = quota?.used ?: 0, freeChatTotal = quota?.limit ?: 0,
│   │       onCloseClick, onChatIconClick = ::startNewConversation, onNotesIconClick)
│   │
│   ├── Box(weight 1f)
│   │     ├─ 진입 상태 (messages.isEmpty() && phase == IDLE)  ← Figma 노드 A가 그리는 화면
│   │     │    Column(verticalScroll, horizontalAlignment = CenterHorizontally)
│   │     │    ├── CharacterAvatar(60.dp)              ← 플레이스홀더 (4절)
│   │     │    ├── Text("오늘은 어떤게 궁금해?", body1Medium)
│   │     │    └── suggestions.forEach {
│   │     │           TodakunChatExampleChip("${it.emoji} ${it.label}") { onSuggestionClick(it.seedPrompt) } }
│   │     │
│   │     └─ 대화 상태 (그 외)
│   │          LazyColumn(state = listState)
│   │          ├── items(messages, key = { it.id })
│   │          │     USER → Row(Arrangement.End) { TodakunChatUserInputBubble(content) }
│   │          │     그 외 → Text(content, body2Regular)      ← 봇 답변은 말풍선이 아니다
│   │          └── item  phase != IDLE 일 때
│   │                THINKING → ThinkingIndicator()
│   │                TYPING   → Text(streamingText, body2Regular)
│   │
│   ├── TodakunChatInputField(value = input, onValueChange, onSendClick)
│   └── TodakunBottomNavigation(selectedItem = TODAK_CHAT, onItemSelect = {})   ← 2-6
│
├── SnackbarHost(snackbarHostState) { TodakunSnackbar(it.visuals.message) }     ← align BottomCenter
└── ChatGreetingOverlay(...)                          ← 2-5. Box 전체를 덮는다(시스템 바 포함)
```

**결정 근거**

- **`Scaffold`를 쓰지 않는다.** 이전 설계는 `MainActivity`의 `Scaffold` 안에 화면을 넣었는데, 그러면 **그리팅 오버레이의 스크림이 상태바 영역을 못 덮는다**(Figma 노드 B는 전체 화면을 덮는다). 루트를 `Box`로 두고 오버레이를 형제로 놓으면 이 문제가 사라지고 중첩도 한 겹 줄어든다. 인셋은 `Modifier.safeDrawingPadding()` 한 줄이 시스템 바와 IME를 모두 처리한다(`safeDrawing`에 `ime`가 포함된다) — `Scaffold`가 주던 것과 동일하다.
- **진입 상태와 대화 상태를 `LazyColumn` 하나에 섞지 않고 분기한다.** 이전 설계는 추천 칩을 `LazyColumn`의 마지막 item으로 넣었다. Figma 노드 A는 칩을 "캐릭터 + 제목"과 한 덩어리로 가운데 세로 배치하며, 이는 메시지 목록과 레이아웃 규칙이 전혀 다르다. 분기하면 **이전 설계의 "[중간] 추천 칩의 위치 — Figma 확인 필요" 리스크가 해소된다.**
- **추천 칩은 `FlowRow`가 아니라 `Column`이다.** Figma가 세로 1열이다. `FlowRow`는 실험적 API 옵트인이 필요한데, 세로 나열에는 필요 없다. 항목이 6개 고정 수준이라 `LazyColumn`도 과하다 — `Column(verticalScroll)`이면 작은 화면에서 잘리는 것까지 막힌다.
- **칩 텍스트는 `"${emoji} ${label}"`.** `TodakunChatExampleChip`의 자체 `@Preview`가 `"📅 중요한 일정 잡기 좋은 날인지 궁금해"`를 쓰고 있어 Figma 텍스트와 문자 단위로 일치한다. `seedPrompt`는 표시가 아니라 **탭 시 전송할 문장**이므로 `onSuggestionClick(seedPrompt)`로 넘긴다(F-chat의 `onSuggestionClick(seedPrompt: String)` 시그니처와 그대로 맞는다).
- **입력창 placeholder를 넘기지 않는다.** `TodakunChatInputField`의 기본값이 이미 `stringResource(R.string.todak_chat_place_holder_chat_input)` = "토닥이에게 운세 물어보기"다. Figma와 일치하므로 화면에서 다시 지정하면 문구가 두 곳에 생긴다.
- **봇 답변은 `Text`로만 그린다.** Figma에 봇 말풍선이 없다. 새 컴포넌트를 만들지 않는다.
- **스트리밍 텍스트는 `messages`의 다음 item으로 그린다.** F-chat이 State를 분리해 둔 덕분에(2-6) 문자 하나가 늘 때 다른 item은 리컴포즈되지 않는다.
- **`THINKING`과 `TYPING`을 같은 item 슬롯에서 교체**한다. 인디케이터가 사라지고 텍스트가 나타나는 위치가 같아 화면이 튀지 않는다.
- **스낵바는 `TodakunSnackbar`로 브랜딩한다.** `SnackbarHost`의 `snackbar` 람다에 넣는 한 줄이다. 기본 M3 스낵바를 쓰면 `app`의 `TodakunTheme`이 다이내믹 컬러라 기기마다 색이 달라진다.
- **자동 스크롤은 화면 로컬 `LaunchedEffect`로 처리한다** (이전 설계 유지). 사이드이펙트 채널로 올리면 초당 40여 개 이벤트가 흘러 채널만 시끄러워진다.

  ```kotlin
  LaunchedEffect(state.messages.size, state.streamingText) {
      if (listState.layoutInfo.totalItemsCount > 0) {
          listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
      }
  }
  ```

### 2-5. **[핵심] 그리팅 오버레이 (Figma 노드 B — 3112:29138)**

요구사항: **최초 진입 시 3초간 떴다가 자동 페이드아웃, X 버튼을 누르면 즉시 닫힘.**
(근거로 삼은 Figma 주석: "3초 애니메이션 (자동으로 페이드아웃) ... X 버튼 클릭 시, 더 빨리 닫기 가능")

#### (1) 언제 띄우는가 — `ChatState`에 필드를 추가하는가

**추가하지 않는다.** 판정 조건이 전부 이미 있는 값으로 결정된다.

```kotlin
val showGreeting = conversationId == null && state.greeting.isNotBlank() && !dismissed
```

| 항목 | 출처 | 근거 |
|------|------|------|
| `conversationId == null` | **`ChatScreen`의 파라미터** (State가 아님) | 신규 대화 진입일 때만 그리팅이 성립한다(히스토리 이어가기는 이미 대화가 있다). `state.conversationId`는 첫 전송의 `Start` 이벤트에서 서버 id로 채워져 값이 바뀌지만, **화면 진입 인자는 끝까지 불변**이라 판정이 안정적이다 |
| `greeting.isNotBlank()` | `ChatState.greeting` (F-chat 기존 필드) | `getChatEntry()`가 비동기라 진입 직후엔 빈 문자열이다. 이 조건이 곧 "그릴 내용이 도착했다"는 신호이며, 오버레이가 나타나는 순간 3초 타이머가 시작된다. entry 조회가 실패하면 그리팅이 아예 뜨지 않는다 — 빈 말풍선을 3초 띄우는 것보다 낫다 |
| `dismissed` | 화면 로컬 `rememberSaveable { mutableStateOf(false) }` | 순수 UI 상태다. VM에 올리면 "이미 본 오버레이"를 지우는 코드가 State에 따라붙는다(F-chat 2-6이 에러를 State에 두지 않기로 한 것과 같은 이유). `rememberSaveable`이 화면 회전을 커버하고, 프로세스 사망 후 다시 뜨는 것은 실질 문제가 아니다 |

> **후속 처리로 넘기는 것**: 위 판정은 지금 구조(진입점 1개)에서 정확하다. 다만 G-history가 재개되어 "히스토리 → 이어가기" 진입이 생기면, `conversationId != null`인데도 서버가 그리팅을 내려주는 경우의 정책을 **F-chat 쪽에서** 정해야 할 수 있다(예: `ChatState.isNewConversation` 같은 명시적 필드). **F-chat 문서 수정은 이번 작업 범위 밖이며, 별도 후속으로 넘긴다.** 지금 필드를 추가하면 소비자가 없는 상태 필드가 하나 늘 뿐이다.

#### (2) `ChatEntry.greeting`(단일 `String`)을 제목+부제 2단으로 어떻게 나누는가 — **미해결**

Figma는 제목("성취운을 알려줄게!", 22sp Bold)과 부제("커리어, 학업, 목표 등 궁금한 점이나 고민은 전부 물어봐줘.", 16sp Regular, coolGray600)의 2단 구성인데, 도메인 모델(A 문서)의 `ChatEntry.greeting`은 `String` 하나다.

| 방안 | 내용 | 판정 |
|------|------|------|
| **(a) 개행 스플릿 ← 기본 채택** | `greeting`에 개행이 있다고 보고 첫 줄을 제목, 나머지를 부제로 쓴다 | **채택.** 클라이언트에 어떤 문구도 하드코딩하지 않는다. 서버 문구와 화면이 어긋날 여지가 0이다 |
| (b) 제목 고정 + `greeting`은 부제 | 제목을 클라이언트 하드코딩하거나 `ChatSuggestion.category`로 매핑 | 기각(기본값으로는). `category` 값의 전체 집합이 확인되지 않았고, **틀린 제목이 화면에 박히는 것이 제목이 없는 것보다 나쁘다.** 백엔드가 (b)로 확정되면 그때 매핑 테이블을 넣는다 |

**백엔드 확정 전까지 양쪽 모두에서 깨지지 않게 방어적으로 만든다** — 3줄이면 된다.

```kotlin
// 개행이 있으면 첫 줄이 제목, 없으면 제목 없이 전체를 부제로 그린다.
// greeting 스키마가 확정되기 전까지 (a)/(b) 어느 쪽이 와도 문구가 유실되지 않는다.
val parts = greeting.split("\n", limit = 2)
val title = parts.getOrNull(1)?.let { parts[0] }      // 2줄 이상일 때만 제목이 생긴다
val body = parts.getOrNull(1)?.trim() ?: greeting
```

제목이 `null`이면 `Text`를 아예 그리지 않는다. **어떤 경우에도 `greeting` 원문 전체가 화면에 남는다**(유실 없음)는 것이 이 처리의 유일한 불변식이다. 확정은 dev 서버 확인 후 (4절).

#### (3) 3초 타이머 + 페이드

```kotlin
AnimatedVisibility(visible = showGreeting, enter = fadeIn(), exit = fadeOut()) { ... }

LaunchedEffect(showGreeting) {
    if (showGreeting) {
        delay(GREETING_DURATION_MILLIS)   // 3_000L
        dismissed = true
    }
}
```

`AnimatedVisibility`의 기본 `fadeIn`/`fadeOut`을 쓴다. `Animatable`을 직접 굴리거나 커스텀 `AnimationSpec`을 만들지 않는다 — Compose가 주는 것으로 충분하다. X 탭도 `dismissed = true` 한 줄이라 **자동 종료와 수동 종료가 같은 경로로 수렴한다**(타이머 취소 같은 별도 처리가 필요 없다 — `showGreeting`이 false가 되면 `LaunchedEffect`가 알아서 취소된다).

`GREETING_DURATION_MILLIS = 3_000L`은 화면 로컬 상수로 두되, **체감 시간은 실기기에서 조정할 노브**로 남긴다(문구 길이에 따라 3초가 짧을 수 있다).

#### (4) 스크림 색 — 40% 토큰이 없다

Figma는 `rgba(0,0,0,0.4)`인데 `TodakunColor`에는 `blackOpacity30`과 `blackOpacity50`만 있다(105~107행에서 확인). **`blackOpacity30`을 쓴다.**

근거: 이 오버레이의 시각적 주인공은 확대된 캐릭터다. 50%는 캐릭터를 어둡게 눌러 의도를 해치고, 30%는 그렇지 않다. 흰색 말풍선 카드는 30%에서도 충분히 읽힌다. **`Color(0x66000000)` 같은 값을 화면에 직접 쓰는 것은 `rules/40`의 하드코딩 금지 위반이므로 선택지가 아니다.** 디자이너가 정확히 40%를 요구하면 올바른 해법은 화면 하드코딩이 아니라 `TodakunColor`에 `blackOpacity40`을 추가하는 것이며, designsystem 변경이라 이 단위 범위 밖이다 (4절).

#### (5) 말풍선 카드

- 모서리 둥근 흰 사각형 + **아래쪽 꼬리 삼각형**. 삼각형은 `GenericShape` 한 개로 끝난다 — `Canvas`나 커스텀 `Shape` 클래스를 만들지 않는다.

  ```kotlin
  private val BubbleTail = GenericShape { size, _ ->
      moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height); close()
  }
  ```

- 제목 `TodakunTypography.heading4Bold` (22sp Bold — Figma 22sp와 일치, 89~97행에서 확인)
- 부제 `TodakunTypography.body2Regular` + `TodakunColor.coolGray600` (16sp Regular — Figma와 일치)
- 우측 상단 X 아이콘 20dp — `R.drawable.ic_close` 재사용(`TodakunChatHeader`가 쓰는 것과 같은 리소스)
- 스크림 자체에는 클릭을 붙이지 않는다. Figma가 X 버튼만 닫기 경로로 두고 있다. 다만 **스크림이 아래 화면의 터치를 먹어야** 오버레이 중 입력창이 눌리지 않는다 — `Modifier.clickable(indication = null) {}`로 흡수한다(비어 있는 람다에 이유 주석을 단다).

### 2-6. 바텀 네비게이션의 `onItemSelect`를 no-op으로 두는 이유

Figma 노드 A의 최하단에 `TodakunBottomNavigation`이 있고 선택 탭은 `TODAK_CHAT`이다. 나머지 세 탭(운세/행운 액션/마이)에 해당하는 **화면이 이 앱에 아직 존재하지 않는다.** Navigation 3도 없다. 갈 곳이 없으므로 `onItemSelect = {}`다.

이건 임시방편이 아니라 **팀이 이미 내린 판단과 같은 결**이다 — `TodakunNavItem.kt` 11행에 `// TODO Navigation Route 클래스를 프로퍼티에 추가` 주석이 이미 달려 있다. 즉 "탭별 라우트는 화면이 생긴 뒤"라는 것이 컴포넌트 작성 시점부터의 전제였다. 이 단위에서 그 TODO를 앞당겨 해소하지 않는다.

`selectedItem = TodakunNavItem.TODAK_CHAT`을 상수로 넘긴다. 선택 상태를 State에 둘 이유가 없다 — 바꿀 수 있는 탭이 없다.

같은 이유로 **헤더의 노트 아이콘(`onNotesIconClick`)도 no-op**이다. 히스토리 화면(G-history)이 보류됐다(2-2).

**헤더의 X(닫기)는 `MainActivity`의 `finish()`에 연결한다.** 채팅이 유일한 화면이므로 "이 화면을 닫는다"의 유일한 해석이 앱 종료다. 아무 동작 없는 버튼을 남기는 것보다 낫고, 시스템 뒤로 가기와 동작이 일치한다. 홈 화면이 생기면 그때 "홈으로"로 바뀔 자리다 (4절).

### 2-7. 캐릭터 아바타 — 플레이스홀더로 두는 이유

Figma 노드 A의 60×60 원형 "Mask group"과 노드 B의 확대 캐릭터는 **이미지 asset이다.** designsystem에도 `res/drawable`에도 없고, Figma 이미지 export는 이 설계 문서의 범위 밖이다.

없는 에셋을 가정한 `painterResource(R.drawable.img_todak_character)`를 써 두면 **컴파일이 안 된다.** 대신 크기와 자리만 잡는 플레이스홀더를 두고, 에셋이 들어오면 `Box` 내부만 `Image`로 교체한다(1줄).

```kotlin
@Composable
private fun CharacterAvatar(size: Dp, modifier: Modifier = Modifier) {
    // 캐릭터 이미지 asset 미확보. 크기·위치만 잡아 두고 에셋 반입 시 Image로 교체한다.
    Box(modifier.size(size).clip(CircleShape).background(TodakunColor.primary100))
}
```

메인 화면은 `60.dp`, 오버레이는 확대 크기(Figma 실측값)를 넘긴다. 파라미터 하나로 두 곳이 같은 코드를 쓴다.

### 2-8. Compose UI 테스트를 이 단위에서 작성하지 않는 이유 (이전 설계 유지)

이 저장소에 Compose UI 테스트 선례가 0건이고 `androidTest`를 CI에서 돌리는 설정도 없다(에뮬레이터 미구성). 여기서 첫 UI 테스트 인프라까지 세우면 이 단위의 실제 목적(화면을 띄운다)이 뒤로 밀린다.

대신 **검증 경로를 명확히 남긴다**:

1. `@Preview` 5종 — 진입 상태(캐릭터+제목+칩) / 그리팅 오버레이 / 생각 중 / 타이핑 중 / 대화 이어보기
2. 실기기 스모크 — **이 단위의 실질 완료 조건이다.** 2-9의 6번 참조
3. 상태 기계 자체는 F-chat의 `ChatViewModelTest`가 이미 커버한다

UI 테스트 인프라 도입은 별도 이슈로 등록한다(4절).

### 2-9. 실행 계획

1. `MainActivity` 배선 — `Greeting`/`Scaffold` 제거, `ChatScreen` 직접 호출 → `./gradlew :app:assembleDebug` 통과.
   **이 단계에서 `hiltViewModel()`이 실제로 `ChatViewModel`을 만들어 내는지 실기기로 먼저 확인한다** (2-3, 4절). 실패하면 폴백(`by viewModels()`)으로 즉시 전환하고 나머지 단계를 진행한다
2. `strings.xml` 작성
3. `ThinkingIndicator.kt` + `@Preview`로 애니메이션 확인
4. `ChatScreen.kt` 작성 → `@Preview` 4종(오버레이 제외)으로 상태별 렌더링 확인
5. `ChatGreetingOverlay.kt` 작성 → `@Preview` 1종. **개행 있는 greeting / 개행 없는 greeting 두 경우를 Preview로 모두 확인한다** (2-5 (2)의 방어 로직 검증)
6. `./gradlew :feature:chat:ktlintCheck :app:assembleDebug` 통과
7. **[수동 필수]** 실기기/에뮬레이터 스모크:
   - 앱 실행 → 채팅 화면이 뜨고 추천 칩 6개가 보이는지
   - 그리팅 오버레이가 3초 후 자동으로 사라지는지 / X 탭 시 즉시 사라지는지
   - **dev 서버의 실제 `greeting` 문자열에 개행이 있는지** (2-5 (2) 확정)
   - 전송 → 생각 중 → 타이핑 → 완료, 그리고 두 번째 메시지가 같은 대화에 들어가는지
   - **delta가 점진적으로 도착하는지** (C1/D의 최우선 리스크 — 4절)

## 3. 파일 변경 계획 (구현 체크리스트)

```
app/src/main/java/com/kikidan/todakun/
└── MainActivity.kt                                         (수정) Greeting/Scaffold 제거 → ChatScreen 직접 호출

feature/chat/src/main/
├── res/values/strings.xml                                  (신규) 화면 문구
└── java/com/kikidan/chat/
    ├── ChatScreen.kt                                       (신규) ChatScreen + 내부 private Composable
    ├── ChatGreetingOverlay.kt                              (신규) 그리팅 말풍선 오버레이
    └── ThinkingIndicator.kt                                (신규)
```

> `core/navigation/` 및 `app/.../navigation/TodakunNavDisplay.kt`는 **만들지 않는다.** 이전 설계에 있던 `ChatKey` / `NavDisplay` / 백스택 / `core:navigation` build.gradle 수정은 전부 삭제됐다 (2-2).

- [ ] `app/src/main/java/com/kikidan/todakun/MainActivity.kt` (수정) — `Greeting` Composable과 그 Preview를 삭제하고 `setContent` 본문을 교체

```kotlin
setContent {
    // 화면이 하나뿐이라 라우터를 두지 않는다. 두 번째 화면이 생기면 그때 Navigation 3를 도입한다 (설계 2-3).
    TodakunTheme {
        ChatScreen(
            conversationId = null,
            onCloseClick = ::finish,
            onNavigateToHistory = {},   // 히스토리 화면(G-history) 보류 중
        )
    }
}
```

> `TodakunTheme`은 **`app`의 것**(`com.kikidan.todakun.ui.theme.TodakunTheme`)을 그대로 쓴다. designsystem의 동명 함수는 `content()`만 호출하는 통과 함수라 `MaterialTheme`을 제공하지 않는데, designsystem 컴포넌트들이 M3 `Text`/`Icon`/`SnackbarHost`를 쓰므로 M3 테마가 필요하다. 테마가 두 개인 상태 자체는 4절 후속 항목.
>
> `Scaffold`를 제거한다. 인셋은 `ChatScreen`이 `safeDrawingPadding()`으로 직접 처리하고, 스낵바 호스트도 `ChatScreen`이 갖는다 (설계 2-4).

- [ ] `feature/chat/src/main/res/values/strings.xml` (신규)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="chat_header_title">토닥이</string>
    <string name="chat_entry_question">오늘은 어떤게 궁금해?</string>
    <string name="chat_thinking_description">토닥이가 생각하고 있어요</string>
    <string name="chat_greeting_close_description">그리팅 닫기</string>
</resources>
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ThinkingIndicator.kt` (신규) — 이전 설계 그대로 유지

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

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ChatGreetingOverlay.kt` (신규)

```kotlin
package com.kikidan.chat

/**
 * 최초 진입 시 3초간 떴다가 페이드아웃되는 그리팅 말풍선 (Figma 3112:29138).
 * 표시 여부와 3초 타이머는 호출부(ChatScreen)가 관리한다 — 이 Composable은 그리기만 한다.
 */
@Composable
internal fun ChatGreetingOverlay(
    greeting: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 개행이 있으면 첫 줄이 제목, 없으면 제목 없이 전체를 부제로 그린다.
    // greeting 스키마가 확정되기 전까지 어느 쪽이 와도 문구가 유실되지 않는다 (설계 2-5).
    val parts = greeting.split("\n", limit = 2)
    val title = parts.getOrNull(1)?.let { parts[0].trim() }
    val body = parts.getOrNull(1)?.trim() ?: greeting

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.blackOpacity30)   // Figma 40%, 근접 토큰 (설계 2-5)
                // 오버레이 아래 입력창/칩이 눌리지 않도록 터치를 흡수한다.
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
        contentAlignment = Alignment.Center,
    ) {
        CharacterAvatar(size = ChatGreetingDefaults.CharacterSize)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .clip(ChatGreetingDefaults.CardShape)
                    .background(TodakunColor.white)
                    .padding(ChatGreetingDefaults.CardPadding),
            ) {
                Column {
                    if (title != null) {
                        Text(title, style = TodakunTypography.heading4Bold, color = TodakunColor.coolGray900)
                    }
                    Text(body, style = TodakunTypography.body2Regular, color = TodakunColor.coolGray600)
                }
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.chat_greeting_close_description),
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(ChatGreetingDefaults.CloseIconSize)
                            .clickable(onClick = onCloseClick),
                )
            }
            // 말풍선 꼬리. Shape 클래스를 새로 만들지 않고 GenericShape 한 개로 끝낸다.
            Box(
                Modifier
                    .size(ChatGreetingDefaults.TailWidth, ChatGreetingDefaults.TailHeight)
                    .background(TodakunColor.white, BubbleTail),
            )
        }
    }
}

private val BubbleTail =
    GenericShape { size, _ ->
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width / 2f, size.height)
        close()
    }

private object ChatGreetingDefaults { /* 캐릭터 크기·카드 여백·꼬리 크기 등 Figma 실측값 */ }
```

  > 캐릭터·카드·꼬리의 정확한 배치(캐릭터가 카드 위 어디에 오는지, 꼬리가 카드의 어느 지점을 가리키는지)는 **구현 시 Figma 실측값으로 맞춘다.** 위 코드는 요소 구성과 상하 순서를 확정한 것이고, `Column`/`Box` 정렬 세부는 실측 후 조정한다.

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ChatScreen.kt` (신규)

```kotlin
package com.kikidan.chat

@Composable
fun ChatScreen(
    conversationId: String?,
    onCloseClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 컴포지션당 정확히 1회 (설계 2-3)
    LaunchedEffect(Unit) { viewModel.load(conversationId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ChatSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    ChatScreen(
        state = state,
        // 신규 대화 진입일 때만 그리팅이 성립한다. state.conversationId는 첫 전송 후 값이 바뀌므로
        // 진입 인자를 쓴다 (설계 2-5).
        isNewConversation = conversationId == null,
        snackbarHostState = snackbarHostState,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        onSuggestionClick = viewModel::onSuggestionClick,
        onNewConversationClick = viewModel::startNewConversation,
        onCloseClick = onCloseClick,
        onHistoryClick = onNavigateToHistory,
        modifier = modifier,
    )
}

// 상태 없는 오버로드. @Preview와 (도입 시) UI 테스트가 이쪽을 쓴다.
@Composable
internal fun ChatScreen(
    state: ChatState,
    isNewConversation: Boolean,
    snackbarHostState: SnackbarHostState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onNewConversationClick: () -> Unit,
    onCloseClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var greetingDismissed by rememberSaveable { mutableStateOf(false) }
    val showGreeting = isNewConversation && state.greeting.isNotBlank() && !greetingDismissed

    // 3초 뒤 자동 종료. X 탭도 같은 플래그를 세우므로 타이머를 따로 취소할 필요가 없다 (설계 2-5).
    LaunchedEffect(showGreeting) {
        if (showGreeting) {
            delay(ChatScreenDefaults.GREETING_DURATION_MILLIS)
            greetingDismissed = true
        }
    }

    Box(modifier.fillMaxSize().background(TodakunColor.white)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) { /* 설계 2-4의 구조 */ }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding(),
        ) { data -> TodakunSnackbar(text = data.visuals.message) }

        AnimatedVisibility(visible = showGreeting, enter = fadeIn(), exit = fadeOut()) {
            ChatGreetingOverlay(
                greeting = state.greeting,
                onCloseClick = { greetingDismissed = true },
            )
        }
    }
}
```

  화면 본문은 2-4의 구조를 그대로 구현한다. 내부 private Composable:
  - `ChatEntryContent(suggestions, onSuggestionClick)` — `CharacterAvatar(60.dp)` + 제목 `Text` + 칩 `Column(verticalScroll)`
  - `ChatMessageList(state, listState)` — `LazyColumn`. USER면 `Row(Arrangement.End) { TodakunChatUserInputBubble }`, 아니면 `Text(body2Regular)`. 마지막에 `THINKING`/`TYPING` item
  - `CharacterAvatar(size: Dp)` — 2-7의 플레이스홀더. `ChatGreetingOverlay`와 공유하므로 `internal`
  - 여백 상수는 파일 하단 `private object ChatScreenDefaults`에 모은다 (`TodakunChatInputFieldDefaults`와 같은 패턴)

- [ ] `@Preview` 5종 — 진입 상태(칩 6개) / 그리팅 오버레이(개행 있는 greeting + 개행 없는 greeting) / `THINKING` / `TYPING`(streamingText 중간값) / 대화 이어보기(messages 3개)

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [x] **[해소됨 · 2026-08-04] Navigation 3 (1.1.3) API 표기 미검증** — 이전 설계의 최상위 [높음] 리스크. **Navigation 3를 이 단위에서 제외하기로 확정(2-2)해 회피됐다.** nav3를 실제로 도입하는 시점(두 번째 화면 추가 시)에 이 리스크가 그대로 되살아난다는 점을 기억한다. `app/build.gradle.kts`의 nav3 의존성 4종은 코드가 0줄인 채로 남는다 — 지금 제거하지 않는다(곧 쓸 것이고, 제거/복구가 왕복 작업이 된다).
- [x] **[해소됨 · 2026-08-04] 추천 칩의 위치 (Figma 확인 필요)** — Figma 노드 A로 확정. 캐릭터 + "오늘은 어떤게 궁금해?" 아래 **세로 1열**이다 (2-4).
- [ ] **[높음] `ChatEntry.greeting`의 실제 형태 미확정** — 2-5 (2). 제목+부제 2단 구성을 단일 `String`이 어떻게 담는지 백엔드 스펙이 불명확하다. **개행 스플릿(방안 a)을 기본으로 채택하되, 개행이 없어도 문구가 유실되지 않게 방어적으로 구현한다.** dev 서버의 실제 응답을 확인해 확정한다(2-9의 스모크 항목). (b)로 확정되면 제목을 `ChatSuggestion.category` 매핑으로 바꾸는 변경이 `ChatGreetingOverlay` 안에서만 일어난다.
- [ ] **[높음] 캐릭터 이미지 asset 미확보** — 2-7. Figma 노드 A의 60×60 원형 캐릭터와 노드 B의 확대 캐릭터가 모두 이미지다. **Figma 이미지 export가 선행돼야 화면이 디자인대로 완성된다.** 그때까지 `CharacterAvatar`는 색 원 플레이스홀더다. 반입 시 `res/drawable/img_*`(png) 또는 `ic_*`(벡터) 네이밍 규칙(`rules/40`)을 따른다.
- [ ] **[높음] delta 점진 도착 여부(C1 최상위 리스크)가 이 화면에서 처음 눈에 보인다** — OkHttp 엔진이 응답을 버퍼링하면 답변이 종료 시점에 통째로 도착하고, `typewriter()`는 그것을 "거대한 단일 청크"로 받아 catch-up 속도로 쏟아낸다. 즉 **화면은 죽지 않지만 타이핑 효과가 사라진다.** 실기기 스모크에서 반드시 확인하고, 실패 시 C1 문서의 대안(CIO 엔진 / `Accept-Encoding: identity`)으로 되돌아간다.
- [ ] **[중간] `hiltViewModel()`이 Nav3 없이 동작하는지 미검증** — 2-3. 이 저장소 최초의 Hilt-Compose 배선이다. **2-9의 1번 단계에서 가장 먼저 확인한다.** 실패 시 폴백은 `MainActivity`에서 `by viewModels<ChatViewModel>()`로 받아 파라미터로 넘기는 것(2줄).
- [ ] **[중간] 스크림 40% 토큰 부재** — 2-5 (4). Figma는 `rgba(0,0,0,0.4)`인데 `TodakunColor`에는 30%/50%만 있어 `blackOpacity30`을 쓴다. 디자이너가 정확한 값을 요구하면 **화면 하드코딩이 아니라 `TodakunColor.blackOpacity40` 추가**가 올바른 해법이며, designsystem 변경이라 별도 처리한다.
- [ ] **[중간] `ChatState`에 "신규 대화" 명시 필드가 필요해질 수 있다** — 2-5 (1). 현재는 `ChatScreen`의 `conversationId` 파라미터로 충분하다. G-history 재개 시 "히스토리 이어가기 진입인데 서버가 그리팅을 내려주는 경우"의 정책이 필요해지면 `ChatState`에 필드를 추가해야 한다. **F-chat 문서 수정은 이번 범위 밖이며 별도 후속으로 넘긴다** — 지금 추가하면 소비자 없는 필드가 하나 늘 뿐이다.
- [ ] **[중간] 그리팅 3초가 충분한지** — 2-5 (3). 부제가 2줄이면 3초에 다 못 읽을 수 있다. `GREETING_DURATION_MILLIS`를 실기기에서 조정할 노브로 남겼다. 실제 문구 길이를 보고 판단한다.
- [ ] **[중간] 자동 스크롤이 사용자 스크롤을 뺏는다** — 타이핑 중 사용자가 위로 올려 이전 대화를 보면 매 틱 아래로 끌려 내려간다. 수정하려면 "이미 바닥 근처일 때만 스크롤"(`listState.layoutInfo`로 판정) 조건을 추가한다(약 5줄). 실기기에서 거슬리면 그때 넣는다.
- [ ] **[중간] `app`과 `core:designsystem`에 `TodakunTheme`이 각각 존재한다** — designsystem 쪽은 `content()`만 호출하는 껍데기이고 M3 테마를 제공하지 않는다. 채팅 화면은 `app`의 것을 쓰고 designsystem 토큰을 직접 참조하므로 당장 문제는 없지만, 테마가 두 개인 상태는 오래 두면 안 된다. 통합 이슈 별도 등록 권장.
- [ ] **[중간] Compose UI 테스트 인프라 부재** — 2-8 참조. `androidTest` CI 실행 환경 도입을 별도 이슈로 등록.
- [ ] **[낮음] 바텀 네비게이션의 나머지 3개 탭이 아무 동작도 하지 않는다** — 2-6. 화면이 없으니 당연한 귀결이며 `TodakunNavItem.kt`의 기존 TODO와 같은 판단이다. 사용자에게는 "눌리지 않는 탭"으로 보이므로, 다른 탭 화면이 생기기 전까지 이 상태가 유지된다는 것을 팀이 인지할 필요는 있다.
- [ ] **[낮음] 헤더 X가 앱을 종료한다** — 2-6. 홈 화면이 없어 `finish()`가 유일한 해석이다. 홈이 생기면 "홈으로"로 바뀐다.
- [ ] **[낮음] `ChatAction` 렌더링 미포함** — Figma에 액션 카드 디자인이 없다. `ChatMessage.action`으로 State에는 이미 담기므로 후속 이슈에서 UI만 추가하면 된다.
