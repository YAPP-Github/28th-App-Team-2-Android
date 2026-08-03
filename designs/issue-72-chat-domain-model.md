# 설계 문서 — #59 하위 A: 토닥이 채팅 Domain 모델 + ChatRepository 인터페이스

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #72 (상위 이슈: #59)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **2026-08-04 갱신**: 백엔드 실제 SSE 스키마 확인 후 교정 (근거: 사용자 제공 `SseEmitterChatStreamListener` 및 이벤트 DTO 소스)
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

- **`Delta`의 필드명을 `text`로 둔다** — 백엔드 `ChatDeltaEvent`의 필드명이 `text`다. 도메인만 `content`로 바꾸면 DTO(`text`) → 도메인(`content`) → State(`streamingText`)로 이름이 세 번 바뀌어 디버깅 시 grep이 끊긴다. `ChatStreamEvent`는 "SSE 프로토콜의 도메인 표현"이라는 성격이 강하므로 **전 계층에서 `text`로 통일**한다. `ChatMessage.content`(REST 스키마 그대로)와 이름이 다른 것은 서버 스키마 자체가 다르기 때문이며 혼동 요소가 아니다.
- **`Start`가 `ChatQuota`를 통째로 갖는다** — 백엔드는 `quotaUsed`/`quotaLimit` 두 Int로 보내지만, 도메인에 느슨한 Int 2개를 두면 `remaining` 계산이 호출부로 샌다. 이미 있는 `ChatQuota`를 재사용해 매퍼가 한 번만 조립한다.
- **`Done`이 `assistantMessageId`만 갖는다** — 백엔드 `ChatDoneEvent`가 id 하나만 보낸다. 최종 텍스트/상태/시각은 클라이언트가 조립한다(누적 delta + `COMPLETED` + `Instant.now()`). 도메인 이벤트가 서버가 보내지 않은 값을 지어내면 안 되므로 `ChatMessage`를 싣지 않는다. 조립 책임은 presentation(F-chat).
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
 *
 * 필드 구성은 백엔드 이벤트 DTO(ChatStartEvent/ChatDeltaEvent/ChatActionEvent/ChatDoneEvent)와 1:1이다.
 */
sealed interface ChatStreamEvent {
    data class Start(
        val conversationId: String,
        val userMessageId: String,
        val assistantMessageId: String,
        val quota: ChatQuota,
    ) : ChatStreamEvent

    data class Delta(
        val text: String,
    ) : ChatStreamEvent

    data class Action(
        val action: ChatAction,
    ) : ChatStreamEvent

    data class Done(
        val assistantMessageId: String,
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

- [x] **[해결됨 · 2026-08-04] SSE 이벤트 페이로드 스키마 미확정** — 백엔드 이벤트 DTO 소스(`ChatStartEvent`/`ChatDeltaEvent`/`ChatActionEvent`/`ChatDoneEvent`/`ChatErrorEvent`)를 직접 확인했다. `start`는 `conversationId`·`userMessageId`·`assistantMessageId`·`quotaUsed`·`quotaLimit`, `delta`는 `text` 하나, `done`은 `assistantMessageId` 하나, `error`는 `code`+`message`다. 위 3절 sealed 정의가 이 스키마와 1:1이다. 추론 기반 방어 코드는 D에서 제거한다.
- [x] **[해결됨 · 2026-08-04] `ChatStreamEvent.Start`에 quota 정보가 함께 오는가?** — **온다.** `ChatStartEvent(quotaUsed, quotaLimit)`. 전송 직후 `/chat/entry` 재조회가 불필요하고, presentation의 quota 낙관적 증가 로직도 폐기한다(F-chat 문서 반영 완료).
- [ ] **[낮음] SSE `action`의 `date`는 non-null인데 도메인은 `LocalDate?`다** — 백엔드 `ChatActionEvent.date`는 `LocalDate`(non-null)이지만, REST 조회 경로(`ConversationDetailResponse` → `ChatMessageResponse.action`)의 Swagger는 required를 명시하지 않는다. 두 경로가 같은 `ChatAction`을 공유하므로 **느슨한 쪽(nullable)에 맞춰 `LocalDate?`를 유지**한다. REST 쪽도 항상 non-null임이 확인되면 non-null로 조인다.
- [ ] **[중간] `MessageStatus` 허용 값** — `PENDING/STREAMING/COMPLETED/FAILED`는 추론이다. `UNKNOWN` 폴백을 두었으므로 앱이 죽지는 않지만 분기 로직이 어긋날 수 있다.
- [ ] **[중간] `ChatAction.type` / `category` 허용 값** — 확정되면 enum으로 승격하는 후속 이슈 필요. 현재는 presentation이 String 비교로 분기해야 한다.
- [x] **[해결됨 · 2026-08-04] `ChatMessage.id`가 SSE 스트리밍 중에는 없을 수 있는가?** — `start`가 `userMessageId`·`assistantMessageId`를 모두 주므로 스트리밍 시작 시점에 양쪽 id를 안다. presentation은 낙관적 사용자 메시지의 로컬 임시 id를 `start` 수신 시 서버 id로 교체한다(F-chat 문서 참조). UI 전용 메시지 모델은 여전히 불필요하다.
