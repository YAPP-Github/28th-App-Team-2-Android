# 설계 문서 — #59 하위 C2: 토닥이 채팅 DTO

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 C2 (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: 없음 (A/B/C1과 병렬 진행 가능). 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

`core:data-remote`의 `dto/chat/` 패키지에 채팅 API의 요청/응답 DTO를 **선언만** 한다.

- 포함: REST 4개 엔드포인트의 Request/Response DTO + SSE 이벤트 payload DTO
- 제외:
  - **`toDomain()` 매퍼는 이 단위에 포함하지 않는다** (단위 D)
  - DataSource / Repository (단위 D)

**파일 변경 개수: 5개 (신규 5, 수정 0)** — 10개 기준 충족.

## 2. 설계 및 실행 계획

### 2-1. 매퍼를 이 단위에서 분리하는 이유 (기존 컨벤션과의 의도적 차이)

기존 `dto/auth/LoginResponse.kt`는 DTO 선언과 `fun LoginResponse.toDomain()`을 **같은 파일**에 둔다. 그 컨벤션을 그대로 따르면 C2와 D가 동일한 5개 파일을 동시에 편집하게 되어, 두 이슈를 병렬로 진행할 수 없거나 충돌이 난다.

따라서 **C2는 순수 DTO 선언, D는 `dto/chat/ChatMapper.kt` 단일 파일에 매퍼 집중**으로 나눈다.
`rules/20-data.md`는 "Mapper **또는** 확장 함수로 변환"이라고 명시하므로 두 방식 모두 규칙 준수다.
채팅 도메인은 매핑 대상이 9종이고 SSE 이벤트 매핑처럼 DTO 하나에 귀속되지 않는 로직(이벤트 이름 → sealed 분기)이 있어, 매퍼를 한 파일에 모으는 편이 오히려 응집도가 높다.

### 2-2. 파일 분할 기준

DTO를 클래스당 1파일로 하면 9개 + SSE 3개 = 12파일로 "이슈당 10개 내외" 기준을 넘긴다. 대신 **"하나의 응답 트리 = 하나의 파일, 파일명은 최상위 DTO 이름"** 규칙으로 5파일로 묶는다.

| 파일 | 담는 DTO | 근거 |
|------|---------|------|
| `SendChatMessageRequest.kt` | SendChatMessageRequest | 유일한 Request |
| `ChatMessageResponse.kt` | ChatMessageResponse, ChatActionResponse | ChatActionResponse는 ChatMessageResponse에 중첩되며 SSE action 이벤트에서도 재사용 |
| `ChatEntryResponse.kt` | ChatEntryResponse, ChatSuggestionResponse, ChatQuotaResponse | `/chat/entry` 응답 트리 전체 |
| `ConversationListResponse.kt` | ConversationListResponse, ConversationSummaryResponse | `/chat/conversations` 응답 트리 |
| `ConversationDetailResponse.kt` | ConversationDetailResponse | `/chat/conversations/{id}` 응답 |
| `ChatStreamResponse.kt` | ChatStreamStartResponse, ChatStreamDeltaResponse, ChatStreamErrorResponse | SSE 이벤트별 data payload |

`rules/20-data.md`의 "`~Request`, `~Response` 네이밍 사용, 재사용하지 않는다"(P2)는 **클래스 이름 규칙**이며 파일 배치 규칙이 아니다. 모든 클래스가 `~Request`/`~Response` 접미사를 갖고, 각 엔드포인트가 자기 전용 최상위 Response를 갖는다(`ChatActionResponse`만 중첩 값 객체로 공유되며 이는 서버 스키마 자체가 공유하는 형태다).

### 2-3. 타입 매핑 원칙 — "Raw 서버 데이터 형태 유지"

`rules/20-data.md`: "도메인 로직과 무관하게 Raw 서버 데이터 형태를 유지한다."

- `uuid` → **`String`** (UUID 파싱은 매핑 단계가 아니라 필요한 곳에서)
- `date-time` → **`String`** (`Instant` 변환은 D의 매퍼 책임)
- `date` → **`String`**
- `role`, `status`, `type`, `category` → **`String`** (enum 승격은 D의 매퍼 책임)

즉 **DTO에는 `java.time` 타입도 enum도 넣지 않는다.** 서버가 예상 밖 포맷/값을 보내도 역직렬화 자체는 성공하고, 변환 실패는 매퍼라는 한 지점에서만 발생한다. `kotlinx.serialization`의 enum 역직렬화는 알 수 없는 값에 대해 예외를 던지므로, DTO에 enum을 쓰면 서버가 새 status 값을 추가하는 순간 **화면 전체가 실패**한다.

### 2-4. nullable / 기본값 정책

`TodakunJson`은 `ignoreUnknownKeys = true`, `explicitNulls = false`, `coerceInputValues = true`로 설정되어 있다. 서버가 필드를 **추가**하는 것은 안전하다. 위험한 것은 **누락**이다.

- 서버가 반드시 준다고 Swagger가 보장하는 필드: non-null (`content`, `greeting`, `id` 등)
- optional 가능성이 있는 필드: nullable + `= null` 기본값 (`action`, `messageId`, `date`)
- **SSE payload는 스키마 미확정이므로 전 필드에 기본값을 준다.** 필드 하나가 다른 이름으로 와도 파싱이 죽지 않고 매퍼가 폴백을 탄다.

### 2-5. SSE 이벤트 payload 설계 (추론 기반)

Swagger의 `SseEmitter` 스키마에는 `timeout`만 있어 이벤트 data의 형태가 명시돼 있지 않다. 이벤트 이름과 인접 스키마(`ChatMessageResponse`, `ChatActionResponse`)로부터 추론한다.

| 이벤트 | data 형태 (추론) | DTO |
|--------|----------------|-----|
| `start` | `{"conversationId":"...","messageId":"..."}` | `ChatStreamStartResponse` |
| `delta` | `{"content":"안"}` **또는** 평문 `안` | `ChatStreamDeltaResponse` (+ 평문 폴백은 D의 매퍼가 처리) |
| `action` | `ChatActionResponse`와 동일 | `ChatActionResponse` 재사용 |
| `done` | `ChatMessageResponse`와 동일 | `ChatMessageResponse` 재사용 |
| `error` | `{"code":"...","message":"..."}` | `ChatStreamErrorResponse` |

**`delta`의 평문 폴백을 설계에 포함한 이유**: SSE 구현체 상당수가 delta를 JSON이 아닌 평문으로 보낸다(`data: 안녕`). 어느 쪽인지 확정되지 않은 상태에서 한쪽만 가정하면 백엔드 확인 결과에 따라 재작업이 발생한다. `data` 문자열이 `{`로 시작하는지 여부로 분기하는 **한 줄짜리 방어**로 양쪽을 모두 처리한다(구현은 D의 `ChatMapper.kt`).

`done`/`action`이 REST DTO를 그대로 재사용하는 것은 서버가 같은 직렬화기를 쓸 것이므로 자연스럽고, 별도 DTO를 만들면 중복이 된다.

### 2-6. 실행 계획

1. `core/data-remote/src/main/java/com/kikidan/data_remote/dto/chat/` 디렉터리 생성
2. 아래 3절의 파일 5개 작성
3. `./gradlew :core:data-remote:compileDebugKotlin` 통과 확인
4. **유닛 테스트는 이 단위에서 작성하지 않는다** — 매퍼 없는 순수 `@Serializable` 선언에는 검증할 로직이 없다. 실제 JSON 역직렬화 검증은 D의 `ChatMapperTest`/`RemoteChatDataSourceImplTest`가 Swagger 예시 JSON으로 커버한다.

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/data-remote/src/main/java/com/kikidan/data_remote/dto/
└── chat/                                       (신규 디렉터리)
    ├── SendChatMessageRequest.kt               (신규)
    ├── ChatMessageResponse.kt                  (신규) ChatMessageResponse, ChatActionResponse
    ├── ChatEntryResponse.kt                    (신규) ChatEntryResponse, ChatSuggestionResponse, ChatQuotaResponse
    ├── ConversationListResponse.kt             (신규) ConversationListResponse, ConversationSummaryResponse
    ├── ConversationDetailResponse.kt           (신규) ConversationDetailResponse
    └── ChatStreamResponse.kt                   (신규) ChatStreamStartResponse, ChatStreamDeltaResponse, ChatStreamErrorResponse
```

- [ ] `.../dto/chat/SendChatMessageRequest.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class SendChatMessageRequest(
    val conversationId: String? = null,
    val content: String,
)
```

- [ ] `.../dto/chat/ChatMessageResponse.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageResponse(
    val id: String,
    val role: String,
    val content: String,
    val status: String,
    val action: ChatActionResponse? = null,
    val createdAt: String,
)

@Serializable
data class ChatActionResponse(
    val type: String,
    val label: String,
    val category: String,
    val date: String? = null,
)
```

- [ ] `.../dto/chat/ChatEntryResponse.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatEntryResponse(
    val greeting: String,
    val suggestions: List<ChatSuggestionResponse> = emptyList(),
    val quota: ChatQuotaResponse,
)

@Serializable
data class ChatSuggestionResponse(
    val emoji: String,
    val label: String,
    val seedPrompt: String,
    val category: String,
)

@Serializable
data class ChatQuotaResponse(
    val used: Int,
    val limit: Int,
)
```

- [ ] `.../dto/chat/ConversationListResponse.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ConversationListResponse(
    val conversations: List<ConversationSummaryResponse> = emptyList(),
)

@Serializable
data class ConversationSummaryResponse(
    val id: String,
    val title: String,
    val lastMessageAt: String,
    val unread: Boolean = false,
)
```

- [ ] `.../dto/chat/ConversationDetailResponse.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDetailResponse(
    val id: String,
    val title: String,
    val messages: List<ChatMessageResponse> = emptyList(),
)
```

- [ ] `.../dto/chat/ChatStreamResponse.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

// SSE 이벤트 data payload. 스키마가 Swagger에 명시돼 있지 않아 전 필드에 기본값을 둔다.
// (필드명이 달라도 역직렬화가 실패하지 않고 매퍼의 폴백 경로를 타게 하려는 의도)

@Serializable
data class ChatStreamStartResponse(
    val conversationId: String? = null,
    val messageId: String? = null,
)

@Serializable
data class ChatStreamDeltaResponse(
    val content: String? = null,
)

@Serializable
data class ChatStreamErrorResponse(
    val code: String? = null,
    val message: String? = null,
)
```

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[높음] SSE 이벤트 data payload 필드명 백엔드 확인 필수** — `ChatStreamStartResponse.conversationId`, `ChatStreamDeltaResponse.content`, `ChatStreamErrorResponse.code/message`는 전부 **추론**이다. 실제 필드명이 다르면(`delta`, `text`, `chunk`, `errorCode` 등) 전 필드가 null로 역직렬화되어 답변이 화면에 아무것도 안 나온다.
  **확인 요청 항목**: (1) delta의 data가 JSON인가 평문인가, (2) JSON이면 텍스트 필드명, (3) start에 conversationId·messageId 외 다른 필드(quota 등)가 있는가, (4) error의 필드명.
- [ ] **[높음] `delta`가 누적값인가 증분값인가** — "한 글자씩 타이핑되듯"이라는 UX 설명상 증분(chunk)으로 가정했다. 누적 전체 텍스트를 매번 보내는 서버도 있으며, 이 경우 presentation이 append가 아니라 replace를 해야 한다. **F/G 단위에 직접 영향**을 주므로 반드시 확정해야 한다.
- [ ] **[중간] `date-time` 포맷** — `Instant.parse`가 되는 오프셋 포함 형태(`2026-08-03T12:00:00Z`)인지, 오프셋 없는 `LocalDateTime` 형태(`2026-08-03T12:00:00`)인지 미확인. D의 매퍼에서 양쪽 모두 처리하는 폴백을 두어 리스크를 흡수한다(D 문서 참조).
- [ ] **[중간] `ChatQuotaResponse`가 optional인가** — `ChatEntryResponse.quota`를 non-null로 두었다. quota 기능이 서버에서 비활성화될 수 있다면 nullable로 바꿔야 하고, 그러면 도메인 `ChatEntry.quota`도 nullable이 되어 단위 A에 역파급된다.
- [ ] **[중간] `ChatMessageResponse.status`의 실제 값 집합** — DTO는 String이라 안전하지만, 매핑 시 `UNKNOWN`으로 떨어지면 UI 분기가 어긋난다.
- [ ] **[낮음] `ConversationSummaryResponse.unread` 기본값 `false`** — 서버가 항상 준다면 기본값을 빼도 된다. 현재는 누락 시 "읽음"으로 보수적으로 처리한다(안 읽음 뱃지를 잘못 띄우는 것보다 낫다는 판단).
- [ ] **[낮음] `SendChatMessageRequest.conversationId`가 null일 때 필드 자체가 빠져야 하는가** — `TodakunJson`의 `explicitNulls = false` 설정으로 인해 null 필드는 직렬화에서 **생략**된다. 서버가 `conversationId: null`을 명시적으로 기대한다면 문제가 되므로 확인 필요(생략이 일반적이므로 현재 설정이 맞다고 판단).
