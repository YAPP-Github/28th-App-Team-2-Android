# 설계 문서 — #59 하위 C2: 토닥이 채팅 DTO

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #75 (상위 이슈: #59)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **2026-08-04 갱신**: 백엔드 실제 SSE 스키마 확인 후 교정 (근거: 사용자 제공 `SseEmitterChatStreamListener` 및 이벤트 DTO 소스)
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
| `ChatStreamResponse.kt` | ChatStreamStartResponse, ChatStreamDeltaResponse, ChatStreamDoneResponse, ChatStreamErrorResponse | SSE 이벤트별 data payload |

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
- optional 가능성이 있는 필드: nullable + `= null` 기본값 (`action`, `date`)
- **SSE payload는 백엔드 소스로 스키마가 확정됐으므로 전 필드 non-null이다.** 기본값·nullable로 느슨하게 두면 매퍼가 존재하지 않는 폴백 경로를 갖게 되고, 필드명이 어긋나도 조용히 빈 답변이 나온다. 스펙이 확정된 지금은 **역직렬화에서 즉시 실패하는 편이 낫다**(D의 `Flow.catch`가 `Result.failure`로 수렴시킨다).

### 2-5. SSE 이벤트 payload 설계 (백엔드 소스 확정, 2026-08-04)

백엔드 `com.yapp.todakun.chat.adapter.web.sse` 패키지의 이벤트 DTO를 직접 확인했다. 어댑터가 Spring MVC `SseEmitter.event().name(eventName).data(data)`로 보내고 `data`는 Jackson이 직렬화하므로 **모든 이벤트의 payload가 JSON 객체**다(`delta`도 예외 없음).

| 이벤트 | 백엔드 DTO | data 형태 | 클라이언트 DTO |
|--------|-----------|----------|---------------|
| `start` | `ChatStartEvent` | `{"conversationId":"...","userMessageId":"...","assistantMessageId":"...","quotaUsed":1,"quotaLimit":5}` | `ChatStreamStartResponse` |
| `delta` | `ChatDeltaEvent` | `{"text":"안"}` | `ChatStreamDeltaResponse` |
| `action` | `ChatActionEvent` | `{"type":"...","label":"...","category":"...","date":"2026-08-04"}` | `ChatActionResponse` 재사용 |
| `done` | `ChatDoneEvent` | `{"assistantMessageId":"..."}` | `ChatStreamDoneResponse` |
| `error` | `ChatErrorEvent` | `{"code":"...","message":"..."}` | `ChatStreamErrorResponse` |

- **`delta`의 평문 폴백은 폐기한다.** 항상 `{"text":"..."}` JSON이다. D의 `extractDeltaContent()` 방어 함수도 함께 삭제한다.
- **`done`은 `ChatMessageResponse`를 재사용하지 않는다.** 서버가 보내는 것은 id 하나뿐이라 전용 `ChatStreamDoneResponse`가 필요하다. 최종 메시지 조립은 presentation 책임(F-chat).
- **`action`은 `ChatActionResponse` 재사용을 유지한다.** 필드 구성이 정확히 같다. 단 `date`는 SSE 경로에서 항상 존재한다(아래 4절 참조).
- **UUID는 `String`으로 받는다.** 백엔드가 `java.util.UUID`지만 Jackson이 문자열로 직렬화하므로 그대로 `String`이다(2-3의 원칙 유지).

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
    └── ChatStreamResponse.kt                   (신규) ChatStreamStartResponse, ChatStreamDeltaResponse, ChatStreamDoneResponse, ChatStreamErrorResponse
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

// SSE 이벤트 data payload. 백엔드 이벤트 DTO와 1:1이며 전 필드 non-null이다.

@Serializable
data class ChatStreamStartResponse(
    val conversationId: String,
    val userMessageId: String,
    val assistantMessageId: String,
    val quotaUsed: Int,
    val quotaLimit: Int,
)

@Serializable
data class ChatStreamDeltaResponse(
    val text: String,
)

@Serializable
data class ChatStreamDoneResponse(
    val assistantMessageId: String,
)

@Serializable
data class ChatStreamErrorResponse(
    val code: String? = null,
    val message: String? = null,
)
```

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [x] **[해결됨 · 2026-08-04] SSE 이벤트 data payload 필드명** — 백엔드 소스로 확정. delta는 **항상 JSON**이고 텍스트 필드명은 `content`가 아니라 **`text`**다. start는 `conversationId`/`userMessageId`/`assistantMessageId`/`quotaUsed`/`quotaLimit`, done은 `assistantMessageId` 하나, error는 `code`/`message`. 평문 폴백과 nullable 기본값을 모두 제거했다.
- [x] **[해결됨 · 2026-08-04] `delta`가 누적값인가 증분값인가** — **증분(append)**. 백엔드 `ChatDeltaEvent` 주석이 "답변 토큰 조각"이라고 명시한다. F-chat의 `typewriter()` 전제(append)가 그대로 유효하다.
- [ ] **[낮음] `ChatActionResponse.date`가 SSE와 REST에서 다르게 취급된다** — SSE `ChatActionEvent.date`는 백엔드에서 non-null(`LocalDate`)이지만, REST 조회 경로의 Swagger는 required를 명시하지 않아 `String? = null`을 유지한다. 같은 DTO를 두 경로가 공유하므로 **느슨한 쪽에 맞춘다.** SSE 경로에서 실제로 null이 오는 일은 없으므로 매퍼의 `date?.let(LocalDate::parse)`가 손해를 보지 않는다.
- [ ] **[중간] `date-time` 포맷** — `Instant.parse`가 되는 오프셋 포함 형태(`2026-08-03T12:00:00Z`)인지, 오프셋 없는 `LocalDateTime` 형태(`2026-08-03T12:00:00`)인지 미확인. D의 매퍼에서 양쪽 모두 처리하는 폴백을 두어 리스크를 흡수한다(D 문서 참조).
- [ ] **[중간] `ChatQuotaResponse`가 optional인가** — `ChatEntryResponse.quota`를 non-null로 두었다. quota 기능이 서버에서 비활성화될 수 있다면 nullable로 바꿔야 하고, 그러면 도메인 `ChatEntry.quota`도 nullable이 되어 단위 A에 역파급된다.
- [ ] **[중간] `ChatMessageResponse.status`의 실제 값 집합** — DTO는 String이라 안전하지만, 매핑 시 `UNKNOWN`으로 떨어지면 UI 분기가 어긋난다.
- [ ] **[낮음] `ConversationSummaryResponse.unread` 기본값 `false`** — 서버가 항상 준다면 기본값을 빼도 된다. 현재는 누락 시 "읽음"으로 보수적으로 처리한다(안 읽음 뱃지를 잘못 띄우는 것보다 낫다는 판단).
- [ ] **[낮음] `SendChatMessageRequest.conversationId`가 null일 때 필드 자체가 빠져야 하는가** — `TodakunJson`의 `explicitNulls = false` 설정으로 인해 null 필드는 직렬화에서 **생략**된다. 서버가 `conversationId: null`을 명시적으로 기대한다면 문제가 되므로 확인 필요(생략이 일반적이므로 현재 설정이 맞다고 판단).
