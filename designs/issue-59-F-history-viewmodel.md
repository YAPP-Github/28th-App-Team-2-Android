# 설계 문서 — #59 하위 F-history: HistoryContract + HistoryViewModel (Orbit MVI)

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 F-history (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: A, B(`GetConversationsUseCase`/`DeleteConversationUseCase` 직접 주입 — 컴파일 의존), F-chat(`designs/issue-59-F-chat-viewmodel.md`, `feature:chat` 모듈과 테스트용 `FakeChatRepository`가 이미 있어야 함) — **G-chat은 필요 없음**(같은 모듈에 있을 뿐 `ChatState`/`ChatViewModel`을 참조하지 않는다). 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

대화 히스토리 목록의 상태 기계. 화면은 G-history가 붙인다.

- 포함: `HistoryContract.kt`, `HistoryViewModel.kt`, `HistoryViewModelTest.kt`
- 제외:
  - Composable / 라우트 (G-history)
  - **새 모듈 생성** — 히스토리는 채팅 기능의 일부다. `feature:chat`에 넣는다 (2-2)
  - 페이지네이션 / 검색 / 로컬 캐시 — 요구사항에 없다 (YAGNI)

**파일 변경 개수: 3개 (신규 3, 수정 0)** — 이 프로젝트에서 가장 작은 단위다. 새 gradle 설정도, DI 모듈도, 새 의존성도 필요 없다.

### 이 단위의 위치

전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고. 이 단위는 A, B에 컴파일 의존하고 D에는 런타임/DI로만 의존한다(F-chat이 이미 겪은 것과 동일한 성격).

## 2. 설계 및 실행 계획

### 2-1. 규칙 준수 근거

| 규칙 | 준수 방법 |
|------|-----------|
| `rules/30`: 도메인 로직 직접 수행 금지 (P2) | `GetConversationsUseCase` / `DeleteConversationUseCase`만 주입. Repository 직접 주입 없음 |
| `rules/30`: ViewModel은 상태 관리에 집중 | 정렬은 서버(B 문서 가정), 시각 포맷은 화면(G-history), 삭제 규칙은 UseCase. VM은 목록 보관과 낙관적 갱신만 한다 |
| `rules/30`: `SavedStateHandle` 검토 | **불필요.** 화면 인자가 없고 목록은 진입 시 재조회하는 편이 정확하다(다른 화면에서 대화가 늘었을 수 있다) |
| `rules/00`: feature 간 참조 금지 (P1) | 새 모듈을 만들지 않아 위반 여지 자체가 없다 (2-2) |

### 2-2. `feature:history` 새 모듈을 만들지 않는 이유

히스토리 항목을 탭하면 채팅 화면으로 간다. 두 화면을 다른 feature 모듈에 두면 **한쪽이 다른 쪽의 화면/라우트를 알아야 하고, 그 순간 `rules/00`의 "feature 모듈끼리 직접 참조 금지(P1)"에 걸린다.** 회피하려면 라우트 키를 `core:navigation`으로 올리고 화면 조립을 전부 app으로 빼는 우회가 필요한데(G-chat이 이미 그렇게 하고는 있다), 그래도 모듈 하나가 gradle 설정·매니페스트·테스트 셋업을 통째로 복제하며 늘어난다.

히스토리는 채팅 기능의 하위 화면이지 별개 기능이 아니다. **`feature:chat` 한 모듈에 둔다.** 모듈이 필요해지는 신호(별도 팀 소유, 빌드 시간, 재사용)가 나타나면 그때 쪼갠다.

### 2-3. **[핵심 트레이드오프] 삭제를 낙관적으로 처리할 것인가**

| 옵션 | 동작 | 장점 | 단점 | 판정 |
|------|------|------|------|------|
| **비관적** | 응답을 기다렸다 목록에서 제거 | 화면이 항상 서버와 일치. 롤백 코드 없음 | 파괴적 동작에 200~500ms 정지 구간이 생긴다. 스피너를 띄우면 UI가 하나 더 늘고, 안 띄우면 "탭이 씹혔나?" 싶은 침묵이 생긴다 | 기각 |
| **낙관적 + 목록 전체 스냅샷 롤백 ← 채택** | 즉시 제거, 실패하면 되돌리고 스낵바 | 즉각 반응. 롤백이 **2줄**이다 — 지운 항목과 인덱스를 따로 기억할 필요 없이 삭제 전 `List` 참조를 통째로 들고 있다가 그대로 되돌린다(불변 리스트라 복사 비용 없음) | 실패 시 항목이 되살아나 깜빡인다 | **채택** |
| 낙관적 + 재조회 | 실패 시 목록 재조회 | 서버와 확실히 일치 | 실패 상황에서 왕복이 하나 더 늘고, 그 요청도 실패할 수 있다 | 기각 |

낙관적 갱신이 위험한 곳은 여러 갱신이 겹치는 경우인데, 이 화면은 항목 단위 삭제뿐이고 스냅샷을 각 intent가 지역 변수로 들고 있어 서로 간섭하지 않는다.

**삭제 확인 다이얼로그는 만들지 않는다.** 요구사항에 없고 Figma에서 확인되지 않았다. `TodakunDialog`가 designsystem에 이미 있으므로 필요해지면 붙이는 데 몇 줄이면 된다. (4절 등록)

### 2-4. 목록 재조회 시점

`load()`를 화면 진입 시 호출한다. 채팅 화면에서 대화를 하고 히스토리로 돌아오면 목록이 바뀌어 있어야 하므로, 백스택 복귀 시에도 다시 불려야 한다.

Nav3에서 백스택 복귀 시 `LaunchedEffect(Unit)`는 재실행되지 않을 수 있다(entry가 살아 있으면 컴포지션이 유지). 그러나 **G-history의 화면 구조상 히스토리→채팅 이동은 push이고, 돌아오면 히스토리 entry가 재개된다.** 목록이 낡을 수 있다.

이 화면은 `init`이 아니라 **명시적 `load()`** 를 두고, G-history가 `LaunchedEffect(Unit)`로 1회 호출한다. 복귀 시 갱신은 4절 후속 항목으로 등록한다 — `Lifecycle.RESUMED` 관찰을 지금 넣으면 검증할 수 없는 코드가 하나 늘어난다.

### 2-5. State 형태

- `ConversationSummary`(도메인)를 그대로 담는다. UI 모델을 만들지 않는다 — 화면이 필요로 하는 것이 `title`/`lastMessageAt`/`unread`/`id` 넷뿐이고 전부 이미 있다.
- **상대 시각 문자열("30분 전")을 State에 넣지 않는다.** 넣으면 VM이 문자열을 만들고, 그 문자열은 시간이 지나도 갱신되지 않는 죽은 값이 된다. 화면이 `Instant`를 받아 그리는 편이 정직하고, Android 플랫폼의 `DateUtils.getRelativeTimeSpanString`으로 한 줄에 처리된다(G-history 참조).
- 에러는 State가 아니라 `ShowMessage` 사이드이펙트. F-chat과 동일한 규칙을 유지해 화면 두 개의 에러 처리가 갈라지지 않게 한다.
- 빈 목록과 로딩을 구분한다: `isLoading` + `conversations.isEmpty()` 조합으로 화면이 스피너/빈 상태/목록 셋을 분기한다. 별도 enum을 만들 만큼 상태가 많지 않다.
- **항목 클릭은 사이드이펙트로 내보내지 않는다.** F-chat과 같은 이유로, 클릭 시 화면이 받은 `onConversationClick(id)` 람다를 직접 호출한다. VM이 라우팅을 모르므로 `feature:chat`이 `core:navigation`에 의존하지 않는다.

  > 오케스트레이터 지시는 "항목 클릭 시 SideEffect로 네비게이션 이벤트 방출"이었으나, 이 경우 사이드이펙트는 **State 변경 없이 화면이 이미 아는 값(id)을 VM을 거쳐 되돌려 받는 왕복**에 불과하다. VM이 개입할 판단이 없다. G-chat이 이미 같은 이유로 네비게이션 사이드이펙트를 쓰지 않기로 했으므로 두 화면의 규칙을 일치시킨다. (삭제 후 화면 이동처럼 VM의 판단이 필요한 이벤트가 생기면 그때 사이드이펙트로 승격한다.)

### 2-6. 실행 계획

1. `HistoryContract.kt` 작성
2. `HistoryViewModelTest.kt` **먼저 작성**(TDD) → `HistoryViewModel.kt` 구현
3. `./gradlew :feature:chat:test :feature:chat:ktlintCheck` 통과

**테스트 계획** (`orbit-test` + F-chat이 만든 `FakeChatRepository` 재사용)

| 케이스 | 기대 |
|--------|------|
| `load()` 성공 | `conversations`가 채워지고 `isLoading == false` |
| `load()` 실패 | `conversations`가 비고 `isLoading == false`, `ShowMessage` 방출 |
| `load()` 성공 + 빈 목록 | `conversations.isEmpty()`, `isLoading == false`, 사이드이펙트 없음 |
| `delete("c-1")` 성공 | 해당 항목이 즉시 목록에서 사라지고 그대로 유지, 사이드이펙트 없음 |
| **`delete("c-1")` 실패** | 목록이 삭제 이전 상태로 **완전히 복원**되고 `ShowMessage` 방출 (롤백 회귀 방지 — 이 단위의 핵심 케이스) |
| `delete` 실패 시 순서 | 낙관적 제거 → 복원 순으로 State가 두 번 바뀐다 (중간 상태가 실제로 방출되는지 확인) |
| 없는 id로 `delete` | 목록 변화 없음, UseCase는 호출됨 |

## 3. 파일 변경 계획 (구현 체크리스트)

```
feature/chat/src/
├── main/java/com/kikidan/chat/history/
│   ├── HistoryContract.kt                                  (신규) HistoryState / HistorySideEffect
│   └── HistoryViewModel.kt                                 (신규)
└── test/java/com/kikidan/chat/history/
    └── HistoryViewModelTest.kt                             (신규)
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/history/HistoryContract.kt` (신규)

```kotlin
package com.kikidan.chat.history

import com.kikidan.domain.model.chat.ConversationSummary

data class HistoryState(
    val isLoading: Boolean = true,
    val conversations: List<ConversationSummary> = emptyList(),
)

sealed interface HistorySideEffect {
    data class ShowMessage(val message: String) : HistorySideEffect
}
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/history/HistoryViewModel.kt` (신규)

```kotlin
package com.kikidan.chat.history

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.DeleteConversationUseCase
import com.kikidan.domain.usecase.GetConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val getConversations: GetConversationsUseCase,
        private val deleteConversation: DeleteConversationUseCase,
    ) : ViewModel(),
        ContainerHost<HistoryState, HistorySideEffect> {
        override val container = container<HistoryState, HistorySideEffect>(HistoryState())

        fun load() =
            intent {
                reduce { state.copy(isLoading = true) }
                getConversations()
                    .onSuccess { conversations ->
                        reduce { state.copy(isLoading = false, conversations = conversations) }
                    }.onFailure {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(HistorySideEffect.ShowMessage(LOAD_FAILED_MESSAGE))
                    }
            }

        /** 낙관적 삭제. 실패하면 삭제 이전 목록을 그대로 되돌린다 (설계 2-3). */
        fun delete(conversationId: String) =
            intent {
                val previous = state.conversations
                reduce { state.copy(conversations = previous.filterNot { it.id == conversationId }) }

                deleteConversation(conversationId).onFailure {
                    reduce { state.copy(conversations = previous) }
                    postSideEffect(HistorySideEffect.ShowMessage(DELETE_FAILED_MESSAGE))
                }
            }

        private companion object {
            const val LOAD_FAILED_MESSAGE = "대화 목록을 불러오지 못했어요."
            const val DELETE_FAILED_MESSAGE = "대화를 삭제하지 못했어요."
        }
    }
```

- [ ] `feature/chat/src/test/java/com/kikidan/chat/history/HistoryViewModelTest.kt` (신규) — 2-6의 7케이스.
  F-chat의 `FakeChatRepository`에 `getConversations` / `deleteConversation`의 반환값을 주입하는 필드가 있는지 확인하고, 없으면 그 Fake에 추가한다(같은 모듈 test 소스셋이라 자유롭게 확장 가능).

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[중간] 채팅에서 돌아왔을 때 목록이 낡는다** — 2-4 참조. `load()`가 화면 진입 1회만 불리므로, 채팅 후 히스토리로 복귀하면 방금 나눈 대화가 목록에 없거나 시각이 낡을 수 있다. 실기기에서 거슬리면 `Lifecycle.RESUMED` 관찰 또는 `NavDisplay` 복귀 감지로 재조회를 붙인다. **동작을 보고 판단할 항목이지 지금 만들 것은 아니다.**
- [ ] **[중간] 삭제 확인 다이얼로그 필요 여부** — 파괴적 동작인데 확인 절차가 없다. Figma에 다이얼로그가 있는지 확인 필요. 있다면 `TodakunDialog` 재사용으로 G-history에서 처리한다(VM 변경 불필요 — 화면이 확인 후 `delete`를 호출하면 된다).
- [ ] **[중간] 대화 목록 정렬 보장** — B 문서의 미해결 항목이 그대로 상속된다. 서버가 `lastMessageAt` 내림차순을 보장하지 않으면 목록이 뒤죽박죽으로 보인다. 이 경우 수정 위치는 VM이 아니라 **`GetConversationsUseCase`** 다(B 문서 결정). VM은 손대지 않는다.
- [ ] **[중간] `unread` 플래그를 클라이언트가 지우지 않는다** — 대화를 열면 서버가 읽음 처리하는 것으로 가정했다(A 문서). 서버가 처리하지 않으면 목록에 돌아왔을 때 여전히 안 읽음 뱃지가 남는다. 백엔드 확인 필요.
- [ ] **[낮음] 삭제 중복 탭** — 같은 항목의 삭제 아이콘을 빠르게 두 번 누르면 두 번째는 이미 목록에 없어 `filterNot`이 무의미하지만 UseCase는 두 번 호출된다. 서버가 멱등(두 번째는 404/200)이면 무해하다. 필요하면 진행 중 id 집합을 State에 두는 방식으로 막을 수 있으나, 항목이 즉시 사라져 두 번 누르기가 사실상 어려우므로 만들지 않는다.
- [ ] **[낮음] 페이지네이션 부재** — D 문서와 동일. 대화가 수백 개면 목록 응답과 `LazyColumn`이 모두 커진다. 서버에 상한이 있는지 확인 권장.
