# 설계 문서 — #59 하위 A: 토닥이 채팅 Domain 모델 + ChatRepository 인터페이스

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 A (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기

## 1. 범위

`core:domain` 모듈에 채팅 도메인 모델과 `ChatRepository` 인터페이스만 추가한다.
구현체(`core:data`), DTO(`core:data-remote`), UseCase(단위 B)는 이 단위에 포함하지 않는다.

- 포함:
  - `model/chat/` 하위 도메인 모델 5개 파일 (ChatMessage / ChatAction / Conversation / ChatEntry / ChatStreamEvent)
  - `repository/ChatRepository.kt`
  - `ChatStreamException` (SSE 스트림 실패를 표현하는 도메인 예외)
- 제외:
  - UseCase (단위 B)
  - DTO / DataSource / RepositoryImpl (단위 C1·C2·D)
  - UI 상태 모델 (단위 F/G)

**파일 변경 개수: 6개 (신규 6, 수정 0)** — 10개 기준 충족.

### 전체 작업 단위 의존 그래프

전체 9개 단위의 실행 의존성은 `designs/issue-59-task-dependency-graph.md`를 단일 소스로 참고한다(개별 문서에 중복 작성하지 않는다).

## 2. 설계 및 실행 계획

### 2-1. 규칙 준수 근거 (`.claude/rules/10-domain.md`)

| 규칙 | 준수 방법 |
|------|-----------|
| 모든 필드 `val` (위반 P1) | 전 모델 `val`만 사용. `var` 없음 |
| `data class` 또는 `sealed interface/class` | 전 모델 `data class`, `ChatStreamEvent`만 `sealed interface` |
| DTO/Entity 그대로 노출 금지 (P1) | 서버 스키마의 `role`/`status` String을 enum으로 승격, `createdAt`/`lastMessageAt` String을 `java.time.Instant`로 승격 |
| Repository 반환 타입은 `Result<T>` / `Flow<Result<T>>` (위반 P1) | 5개 함수 전부 준수. 스트리밍은 `Flow<Result<ChatStreamEvent>>` |
| Repository는 "데이터의 관심사" 단위 | 채팅(entry/메시지/대화)은 하나의 서버 도메인(`/api/v1/chat/**`)이므로 `ChatRepository` 단일 인터페이스 |

### 2-2. 핵심 트레이드오프 — 스트리밍 반환 타입

`sendMessage`는 서버가 SSE로 `start -> delta* -> (action) -> done` 또는 `error`를 보내므로 다중 방출이 필수다.
여기서 **에러 채널을 어디에 둘 것인가**가 이 단위의 유일한 실질적 설계 결정이다.

**옵션 1 — `Flow<ChatStreamEvent>` + sealed 안에 `Error` 이벤트 포함**
- 장점: collect 지점이 `when(event)` 하나로 끝나 가장 읽기 쉽다. SSE 프로토콜과 1:1.
- 단점: **`rules/10-domain.md`의 Repository 반환 타입 규칙을 정면으로 위반(P1)한다.** 또한 "서버가 보낸 error 이벤트"와 "연결 끊김/파싱 실패 예외"가 서로 다른 경로(이벤트 vs 예외)로 오게 되어, 결국 호출부가 `catch`도 따로 달아야 한다.

**옵션 2 — `Flow<Result<ChatStreamEvent>>` + sealed 안에도 `Error` 이벤트 유지**
- 단점: 에러 채널이 2개(`Result.failure` / `Error` 이벤트)가 되어 호출부가 두 가지를 모두 분기해야 한다. 가장 나쁜 조합.

**옵션 3 — `Flow<Result<ChatStreamEvent>>` + sealed에서 `Error` 제거, 서버 error 이벤트는 `Result.failure(ChatStreamException)`으로 통일 ← 채택**
- 장점:
  - `rules/10-domain.md` 규칙 준수 (P1 회피).
  - **에러 채널이 하나뿐이다.** 연결 실패·HTTP 4xx/5xx·JSON 파싱 실패·서버 error 이벤트가 전부 `Result.failure`로 수렴한다. 호출부는 `fold` 한 번만 하면 된다.
  - 서버 error 이벤트는 스트림의 종결 사유이지 중간 이벤트가 아니다. "실패로 끝났다"는 의미론이 `Result.failure` + 스트림 종료와 정확히 일치한다.
- 단점: delta마다 `Result` 래핑을 벗겨야 한다. → `Result`는 value class라 성공 경로에서 힙 할당이 없으므로 실측 비용은 없다. 호출부 부담은 `map`/`fold` 한 줄.
- 보완: 서버가 보낸 error의 code/message를 잃지 않도록 도메인 예외 `ChatStreamException(code, message)`를 정의해 `Result.failure`에 실어 보낸다.

> 결론: **옵션 3**. sealed interface는 `Start / Delta / Action / Done` 4종이며 `Error` 변형은 두지 않는다.
> (오케스트레이터 지시의 `Error` 변형은 이 트레이드오프 검토 결과 `ChatStreamException`으로 대체한다.)

### 2-3. 그 외 설계 판단

- **id 타입**: 서버는 `uuid`지만 도메인은 `String`으로 둔다. 도메인에 `java.util.UUID`를 넣으면 Navigation 3 인자 직렬화·테스트 픽스처 작성이 불필요하게 무거워지고, 클라이언트는 id를 파싱하지 않고 그대로 전달만 한다. (YAGNI)
- **시각 타입**: `createdAt`, `lastMessageAt`은 `java.time.Instant`. `core:domain`은 순수 JVM 모듈(`java-library`)이라 `java.time` 사용 가능하며 기존 `model/user/User.kt`가 이미 `LocalDate`/`LocalTime`을 쓰고 있어 선례가 있다. 표시용 포맷/타임존 변환은 presentation 책임.
- **`ChatAction.type` / `category`를 enum이 아닌 `String`으로 두는 이유**: Swagger에 허용 값 목록이 없다. 값 집합이 확정되기 전에 enum을 만들면 서버가 새 값을 추가하는 순간 매핑이 깨진다. String 유지 + 리스크 등록 → 백엔드 확정 후 후속 이슈에서 enum 승격. 단, `role`/`status`는 SSE 렌더링 분기에 직접 쓰이므로 enum으로 만들되 `UNKNOWN` 폴백을 둔다.
- **`ChatQuota.remaining`**: `limit - used`를 화면 두 곳(진입 배너, 전송 버튼 비활성)에서 쓰게 되므로 도메인에 계산 프로퍼티로 한 줄 둔다. 저장 필드가 아니므로 불변성 규칙에 저촉되지 않는다.
- **`Conversation` vs `ConversationSummary` 분리**: 목록 API와 상세 API의 응답 형태가 실제로 다르다(`unread`/`lastMessageAt`은 목록에만, `messages`는 상세에만). 하나로 합쳐 nullable 필드를 늘리는 것보다 두 모델이 정직하다.

### 2-4. 실행 계획

1. `core/domain/.../model/chat/` 디렉터리 생성
2. 모델 파일 5개 작성 (아래 3절 시그니처 그대로)
3. `repository/ChatRepository.kt` 작성
4. `./gradlew :core:domain:compileKotlin` 통과 확인 (이 단위는 순수 선언이라 유닛 테스트 대상 로직이 없음 — `ChatQuota.remaining`의 coerce 동작만 B/D 단계 테스트에서 간접 검증)

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/domain/src/main/java/com/kikidan/domain/
├── model/
│   └── chat/
│       ├── ChatMessage.kt          (신규) ChatMessage, MessageRole, MessageStatus
│       ├── ChatAction.kt           (신규) ChatAction
│       ├── Conversation.kt         (신규) Conversation, ConversationSummary
│       ├── ChatEntry.kt            (신규) ChatEntry, ChatSuggestion, ChatQuota
│       └── ChatStreamEvent.kt      (신규) ChatStreamEvent(sealed), ChatStreamException
└── repository/
    └── ChatRepository.kt           (신규) ChatRepository
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/model/chat/ChatMessage.kt`

```kotlin
package com.kikidan.domain.model.chat

import java.time.Instant

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val status: MessageStatus,
    val action: ChatAction?,
    val createdAt: Instant,
)

enum class MessageRole {
    USER,
    ASSISTANT,
    UNKNOWN,
}

enum class MessageStatus {
    PENDING,
    STREAMING,
    COMPLETED,
    FAILED,
    UNKNOWN,
}
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/model/chat/ChatAction.kt`

```kotlin
package com.kikidan.domain.model.chat

import java.time.LocalDate

// type/category의 허용 값이 서버에 명시돼 있지 않아 String으로 둔다. 확정 후 enum 승격 예정.
data class ChatAction(
    val type: String,
    val label: String,
    val category: String,
    val date: LocalDate?,
)
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/model/chat/Conversation.kt`

```kotlin
package com.kikidan.domain.model.chat

import java.time.Instant

data class Conversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
)

data class ConversationSummary(
    val id: String,
    val title: String,
    val lastMessageAt: Instant,
    val unread: Boolean,
)
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/model/chat/ChatEntry.kt`

```kotlin
package com.kikidan.domain.model.chat

data class ChatEntry(
    val greeting: String,
    val suggestions: List<ChatSuggestion>,
    val quota: ChatQuota,
)

data class ChatSuggestion(
    val emoji: String,
    val label: String,
    val seedPrompt: String,
    val category: String,
)

data class ChatQuota(
    val used: Int,
    val limit: Int,
) {
    val remaining: Int get() = (limit - used).coerceAtLeast(0)
}
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/model/chat/ChatStreamEvent.kt`

```kotlin
package com.kikidan.domain.model.chat

/**
 * SSE 이벤트 순서: Start -> Delta* -> (Action) -> Done.
 * 실패는 이 sealed의 변형이 아니라 Flow의 Result.failure(ChatStreamException)으로 전달된다.
 */
sealed interface ChatStreamEvent {
    data class Start(
        val conversationId: String,
        val messageId: String?,
    ) : ChatStreamEvent

    data class Delta(
        val content: String,
    ) : ChatStreamEvent

    data class Action(
        val action: ChatAction,
    ) : ChatStreamEvent

    data class Done(
        val message: ChatMessage,
    ) : ChatStreamEvent
}

class ChatStreamException(
    val code: String?,
    override val message: String,
) : Exception(message)
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/repository/ChatRepository.kt`

```kotlin
package com.kikidan.domain.repository

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatEntry(): Result<ChatEntry>

    /**
     * conversationId가 null이면 새 대화를 시작한다. 새 대화의 id는 첫 Start 이벤트로 내려온다.
     * 실패(연결 끊김, HTTP 에러, 서버 error 이벤트)는 Result.failure로 방출되고 스트림이 종료된다.
     */
    fun sendMessage(
        conversationId: String?,
        content: String,
    ): Flow<Result<ChatStreamEvent>>

    suspend fun getConversations(): Result<List<ConversationSummary>>

    suspend fun getConversationDetail(conversationId: String): Result<Conversation>

    suspend fun deleteConversation(conversationId: String): Result<Unit>
}
```

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[높음] SSE 이벤트 페이로드 스키마 미확정** — Swagger의 `SseEmitter`에는 `timeout`만 있고 각 이벤트 `data`의 JSON 스키마가 없다. `ChatStreamEvent.Start`가 `conversationId`/`messageId`를 갖는다는 것, `Delta`가 텍스트 조각 하나만 갖는다는 것은 **추론**이다. 백엔드에 확인 필요. (실제 매핑 방어 로직은 단위 D에서 처리)
- [ ] **[중간] `ChatStreamEvent.Start`에 quota 정보가 함께 오는가?** — 화면에 "남은 횟수"를 전송 직후 갱신해야 한다면 start 이벤트에 quota가 실려야 한다. 없으면 전송 후 `/chat/entry`를 재조회해야 하는데 이는 추가 왕복이다. 백엔드 확인 필요.
- [ ] **[중간] `MessageStatus` 허용 값** — `PENDING/STREAMING/COMPLETED/FAILED`는 추론이다. `UNKNOWN` 폴백을 두었으므로 앱이 죽지는 않지만 분기 로직이 어긋날 수 있다.
- [ ] **[중간] `ChatAction.type` / `category` 허용 값** — 확정되면 enum으로 승격하는 후속 이슈 필요. 현재는 presentation이 String 비교로 분기해야 한다.
- [ ] **[낮음] `ChatActionResponse.date`가 optional인지** — 캘린더 액션이 아닌 액션 타입이 생기면 null일 수 있어 `LocalDate?`로 두었다. 항상 존재한다면 non-null로 조여도 된다.
- [ ] **[낮음] `ChatMessage.id`가 SSE 스트리밍 중에는 없을 수 있는가?** — presentation이 스트리밍 중인 임시 메시지를 어떤 키로 식별할지는 단위 F/G에서 결정한다(도메인 모델은 그대로 두고 UI 모델에서 처리 권장).
