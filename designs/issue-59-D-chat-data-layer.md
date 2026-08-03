# 설계 문서 — #59 하위 D: RemoteChatDataSource + ChatRepositoryImpl + Mapper

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 D (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: A(도메인 모델·Repository), B(UseCase, 컴파일 의존은 없음), C1(SSE 인프라), C2(DTO) — **4개 모두 머지된 뒤 시작**. 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

A의 `ChatRepository`를 실제로 동작시키는 데이터 레이어 전체.

- 포함: DataSource 인터페이스/구현, DTO→Domain 매퍼, RepositoryImpl, Hilt 바인딩, 유닛 테스트
- 제외: ViewModel·UI (단위 F/G)

**파일 변경 개수: 9개 (신규 7, 수정 2)** — 10개 기준 충족.

## 2. 설계 및 실행 계획

### 2-1. 모듈 배치 (`rules/00-architecture.md` 준수)

기존 auth 구조를 그대로 따른다. 인터페이스는 `core:data`, 구현은 `core:data-remote`에 둔다(`core:data-remote`가 `core:data`에 의존).

```
core:domain      ChatRepository (인터페이스)                          <- A
core:data        RemoteChatDataSource (인터페이스), ChatRepositoryImpl <- D
core:data-remote RemoteChatDataSourceImpl, ChatMapper, DTO            <- C2, D
```

의존 방향: `data -> domain`, `data-remote -> data -> domain`. 역방향·feature 간 참조 없음. **P1 위반 없음.**

### 2-2. DataSource 설계 (`rules/20-data.md` 준수)

| 규칙 | 준수 방법 |
|------|-----------|
| Remote DataSource 함수는 REST 엔드포인트와 1:1 | 5개 함수 = 5개 엔드포인트. 1:1 |
| 함수 접두사는 HTTP METHOD와 연관된 동사 (P2) | `getChatEntry`, `postChatMessage`, `getConversations`, `getConversation`, `deleteConversation` |
| 성공/실패를 try-catch 하지 않고 **그대로 throw** (삼키면 P1) | `runCatching` 사용 금지. `expectSuccess = true`가 던지는 예외를 그대로 통과시킨다 |
| Domain Model을 반환 | 매퍼를 거쳐 `ChatEntry`, `Conversation` 등 도메인 타입 반환 |

`getConversation`(상세)과 `getConversations`(목록)은 엔드포인트가 다르므로 별도 함수다. 단수/복수 이름 차이가 헷갈릴 여지가 있으나 URL 구조(`/conversations` vs `/conversations/{id}`)와 정확히 대응하므로 이쪽이 규칙에 맞다.

`postChatMessage`만 `suspend`가 아닌 `Flow` 반환이다. cold flow이므로 suspend가 불필요하고, 세션 수립이 collect 시점에 일어나야 취소 처리가 자연스럽다.

### 2-3. **[핵심] SSE 예외 처리 전략**

3개 실패 원인이 있고, 이들이 **하나의 채널로 수렴**해야 한다(단위 A의 옵션 3 결정).

| 실패 원인 | 발생 지점 | 전파 경로 |
|-----------|----------|-----------|
| HTTP 4xx/5xx, 연결 실패, socket timeout | `serverSentEvents`의 세션 수립/수신 | Flow 밖으로 예외 throw |
| 서버가 `error` 이벤트 전송 | 매퍼 | `throw ChatStreamException(code, message)` |
| 이벤트 data JSON 파싱 실패 | 매퍼 | `SerializationException` throw |

세 경로 모두 **예외**이므로 `ChatRepositoryImpl`의 `Flow.catch` 하나가 전부를 `Result.failure`로 변환한다. DataSource는 아무것도 잡지 않는다(P1 규칙 준수와 정확히 일치).

```kotlin
override fun sendMessage(conversationId: String?, content: String): Flow<Result<ChatStreamEvent>> =
    remoteChatDataSource
        .postChatMessage(conversationId, content)
        .map { Result.success(it) }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            emit(Result.failure(throwable))
        }
```

- **`CancellationException` 재throw**: `Flow.catch`는 규약상 업스트림 취소 예외를 잡지 않지만, 이 저장소는 이미 `runCatchingCancellable`로 취소를 Result로 감싸지 않겠다는 의사를 명시한 팀이다. 명시적 가드 한 줄로 의도를 코드에 남긴다. 화면 이탈 시 실패 상태가 잘못 그려지는 사고를 막는다.
- **`catch`가 `map` 뒤에 오는 순서**: `catch`는 업스트림만 잡는다. `map`을 먼저 걸어야 DataSource 예외를 잡으면서 다운스트림(collect 블록)의 예외는 잡지 않는다. 순서를 바꾸면 UI 렌더링 예외까지 삼켜 디버깅이 지옥이 된다.
- **`error` 이벤트 이후 스트림**: 예외로 전파되므로 Flow가 즉시 종료된다. 서버가 error 뒤에 done을 보내더라도 무시된다 — 의도된 동작.

### 2-4. 매퍼 설계

`core/data-remote/dto/chat/ChatMapper.kt` 한 파일에 모은다 (근거는 C2 문서 2-1).

**방어적 매핑 3가지** — 각각 실제 리스크를 하나씩 제거한다:

1. **`String.toInstantOrThrow()`** — 서버 `date-time` 포맷이 오프셋 포함인지 미확정(C2 리스크). `Instant.parse` 시도 후 실패하면 `LocalDateTime.parse` + KST 적용. 두 형태를 모두 처리해 백엔드 확인 결과와 무관하게 동작한다.
2. **enum 폴백** — `MessageRole`/`MessageStatus`는 `valueOf` 실패 시 `UNKNOWN`. 서버가 새 값을 추가해도 화면 전체가 죽지 않는다.
3. **delta 평문 폴백** — `data`가 `{`로 시작하면 JSON 파싱, 아니면 문자열 자체를 chunk로 사용(C2 문서 2-5). 백엔드 구현이 어느 쪽이든 동작한다.

이 3개는 "혹시 몰라서"가 아니라 **미확정 스펙 3건에 정확히 1:1 대응**하는 방어다. 스펙이 확정되면 제거 가능하며, 그 사실을 주석으로 남긴다.

`Json` 인스턴스는 `TodakunJson`(같은 모듈 `di` 패키지의 `internal val`)을 직접 참조한다. 별도 주입은 하지 않는다 — 설정이 고정된 값이고 매퍼는 순수 함수라 테스트에서도 그대로 쓸 수 있다.

### 2-5. Repository 설계 (`rules/20-data.md` 준수)

- REST 4개: `runCatchingCancellable { }` (기존 `AuthRepositoryImpl`과 동일 패턴). 예외 누수 없음 — **P1 회피**.
- 캐싱 없음. Swagger에 로컬 캐시 요구가 없고 `core:data-local`은 현재 토큰 저장 전용이다. "히스토리 오프라인 조회"는 요구사항에 없으므로 만들지 않는다. (YAGNI)
- 조합 로직 없음. 각 함수가 DataSource 함수 하나와 대응한다.

### 2-6. 실행 계획

1. `core/data`에 `RemoteChatDataSource` 인터페이스 추가
2. `core/data-remote`에 `ChatMapper.kt` 작성 → 매퍼 유닛 테스트 먼저(TDD)
3. `RemoteChatDataSourceImpl` 작성 → MockEngine 테스트
4. `ChatRepositoryImpl` 작성 → Fake DataSource 테스트
5. Hilt 바인딩 2개 추가
6. `./gradlew :core:data:test :core:data-remote:test :core:domain:test` 통과
7. **[수동]** dev 서버 스모크: 실제 전송 → delta 점진 도착 확인 (C1의 미검증 리스크와 동일 항목)

**테스트 계획**

`ChatMapperTest` (`core:data-remote`)
| 케이스 | 기대 |
|--------|------|
| `ChatEntryResponse` 정상 JSON | `ChatEntry` 필드 일치, `quota.remaining` 계산 확인 |
| `ChatMessageResponse.role = "ASSISTANT"` | `MessageRole.ASSISTANT` |
| `role = "assistant"` (소문자) | `MessageRole.ASSISTANT` (대소문자 무관) |
| `status = "WEIRD_NEW_VALUE"` | `MessageStatus.UNKNOWN` (예외 아님) |
| `createdAt = "2026-08-03T12:00:00Z"` | `Instant` 파싱 성공 |
| `createdAt = "2026-08-03T12:00:00"` (오프셋 없음) | KST 기준 `Instant`로 파싱 성공 |
| SSE `event: delta`, `data: {"content":"안"}` | `ChatStreamEvent.Delta("안")` |
| SSE `event: delta`, `data: 안` (평문) | `ChatStreamEvent.Delta("안")` |
| SSE `event: error`, `data: {"code":"QUOTA","message":"초과"}` | `ChatStreamException` throw |
| SSE `event: done`, `data: {ChatMessageResponse}` | `ChatStreamEvent.Done(message)` |
| SSE 알 수 없는 이벤트 이름 | `null` 반환 (무시) |
| SSE `event == null` (heartbeat/comment) | `null` 반환 (무시) |

`RemoteChatDataSourceImplTest` (`core:data-remote`, MockEngine)
| 케이스 | 기대 |
|--------|------|
| `getChatEntry` 정상 CommonResponse | `ChatEntry` 반환 |
| `getChatEntry`가 `data: null` 응답 | `bodyNotNull`이 `IllegalArgumentException` throw (예외를 삼키지 않음) |
| `getConversations` 정상 | `List<ConversationSummary>` 반환 |
| `deleteConversation` 200 | 예외 없이 반환 |
| `deleteConversation` 403 | `ClientRequestException` **그대로 throw** (P1 검증) |
| `postChatMessage` SSE 시퀀스 start→delta×2→done | `ChatStreamEvent` 4개 순서대로 방출 |
| `postChatMessage` 요청 본문 검증 | `conversationId`가 null이면 본문에서 생략, 있으면 포함 |

`ChatRepositoryImplTest` (`core:data`, `FakeRemoteChatDataSource`)
| 케이스 | 기대 |
|--------|------|
| `getChatEntry` 성공 | `Result.success` |
| `getChatEntry`가 `IOException` throw | `Result.failure<IOException>`, 예외 누수 없음 |
| `getChatEntry`가 `CancellationException` throw | 예외 그대로 전파 (Result로 감싸지 않음) |
| `sendMessage` 이벤트 3개 정상 | `Result.success` 3개 |
| `sendMessage`가 2번째 이벤트 후 `IOException` throw | `success, success, failure` 순서로 3개 방출 후 정상 완료 |
| `sendMessage`가 `ChatStreamException` throw | `Result.failure<ChatStreamException>`, code/message 보존 |
| `sendMessage` collect 취소 | 예외 없이 종료, `Result.failure` 미방출 |
| `deleteConversation` 실패 | `Result.failure` |

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/data/src/
├── main/java/com/kikidan/data/
│   ├── datasource/RemoteChatDataSource.kt              (신규)
│   ├── repository/ChatRepositoryImpl.kt                (신규)
│   └── di/RepositoryModule.kt                          (수정) @Binds 1개 추가
└── test/java/com/kikidan/data/
    ├── fake/FakeRemoteChatDataSource.kt                (신규)
    └── repository/ChatRepositoryImplTest.kt            (신규)

core/data-remote/src/
├── main/java/com/kikidan/data_remote/
│   ├── datasource/RemoteChatDataSourceImpl.kt          (신규)
│   ├── dto/chat/ChatMapper.kt                          (신규)
│   └── di/RemoteDataSourceModule.kt                    (수정) @Binds 1개 추가
└── test/java/com/kikidan/data_remote/
    ├── dto/chat/ChatMapperTest.kt                      (신규)
    └── datasource/RemoteChatDataSourceImplTest.kt      (신규)
```

- [ ] `core/data/src/main/java/com/kikidan/data/datasource/RemoteChatDataSource.kt`

```kotlin
package com.kikidan.data.datasource

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface RemoteChatDataSource {
    suspend fun getChatEntry(): ChatEntry

    fun postChatMessage(
        conversationId: String?,
        content: String,
    ): Flow<ChatStreamEvent>

    suspend fun getConversations(): List<ConversationSummary>

    suspend fun getConversation(conversationId: String): Conversation

    suspend fun deleteConversation(conversationId: String)
}
```

- [ ] `core/data-remote/src/main/java/com/kikidan/data_remote/dto/chat/ChatMapper.kt`

```kotlin
package com.kikidan.data_remote.dto.chat

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.domain.model.chat.ChatAction
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.ChatStreamException
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import io.ktor.sse.ServerSentEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

internal fun ChatEntryResponse.toDomain(): ChatEntry =
    ChatEntry(
        greeting = greeting,
        suggestions = suggestions.map { it.toDomain() },
        quota = quota.toDomain(),
    )

internal fun ChatSuggestionResponse.toDomain(): ChatSuggestion =
    ChatSuggestion(emoji = emoji, label = label, seedPrompt = seedPrompt, category = category)

internal fun ChatQuotaResponse.toDomain(): ChatQuota = ChatQuota(used = used, limit = limit)

internal fun ChatMessageResponse.toDomain(): ChatMessage =
    ChatMessage(
        id = id,
        role = role.toMessageRole(),
        content = content,
        status = status.toMessageStatus(),
        action = action?.toDomain(),
        createdAt = createdAt.toInstantOrThrow(),
    )

internal fun ChatActionResponse.toDomain(): ChatAction =
    ChatAction(
        type = type,
        label = label,
        category = category,
        date = date?.let(LocalDate::parse),
    )

internal fun ConversationListResponse.toDomain(): List<ConversationSummary> =
    conversations.map { it.toDomain() }

internal fun ConversationSummaryResponse.toDomain(): ConversationSummary =
    ConversationSummary(
        id = id,
        title = title,
        lastMessageAt = lastMessageAt.toInstantOrThrow(),
        unread = unread,
    )

internal fun ConversationDetailResponse.toDomain(): Conversation =
    Conversation(id = id, title = title, messages = messages.map { it.toDomain() })

/**
 * SSE 이벤트를 도메인 이벤트로 변환한다.
 * 알 수 없는 이벤트(heartbeat, comment 등)는 null을 반환해 무시한다.
 * 서버 error 이벤트는 ChatStreamException을 throw해 Flow를 종료시킨다.
 */
internal fun ServerSentEvent.toChatStreamEventOrNull(): ChatStreamEvent? {
    val payload = data ?: return null
    return when (event) {
        EVENT_START -> {
            val dto = TodakunJson.decodeFromString<ChatStreamStartResponse>(payload)
            ChatStreamEvent.Start(
                conversationId = dto.conversationId ?: return null,
                messageId = dto.messageId,
            )
        }

        EVENT_DELTA -> ChatStreamEvent.Delta(payload.extractDeltaContent() ?: return null)

        EVENT_ACTION ->
            ChatStreamEvent.Action(
                TodakunJson.decodeFromString<ChatActionResponse>(payload).toDomain(),
            )

        EVENT_DONE ->
            ChatStreamEvent.Done(
                TodakunJson.decodeFromString<ChatMessageResponse>(payload).toDomain(),
            )

        EVENT_ERROR -> {
            val dto = TodakunJson.decodeFromString<ChatStreamErrorResponse>(payload)
            throw ChatStreamException(dto.code, dto.message ?: "답변 생성에 실패했어요.")
        }

        else -> null
    }
}

// delta의 data가 JSON인지 평문인지 서버 스펙 미확정. 양쪽 모두 처리한다.
// (백엔드 확정 후 한쪽으로 정리 가능)
private fun String.extractDeltaContent(): String? =
    if (trimStart().startsWith("{")) {
        TodakunJson.decodeFromString<ChatStreamDeltaResponse>(this).content
    } else {
        this
    }

private fun String.toMessageRole(): MessageRole =
    runCatching { MessageRole.valueOf(uppercase()) }.getOrDefault(MessageRole.UNKNOWN)

private fun String.toMessageStatus(): MessageStatus =
    runCatching { MessageStatus.valueOf(uppercase()) }.getOrDefault(MessageStatus.UNKNOWN)

// 서버 date-time에 오프셋이 포함되는지 미확정. 없으면 KST로 간주한다.
private fun String.toInstantOrThrow(): Instant =
    runCatching { Instant.parse(this) }
        .getOrElse { LocalDateTime.parse(this).atZone(KST).toInstant() }

private val KST: ZoneId = ZoneId.of("Asia/Seoul")

private const val EVENT_START = "start"
private const val EVENT_DELTA = "delta"
private const val EVENT_ACTION = "action"
private const val EVENT_DONE = "done"
private const val EVENT_ERROR = "error"
```

- [ ] `core/data-remote/src/main/java/com/kikidan/data_remote/datasource/RemoteChatDataSourceImpl.kt`

```kotlin
package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteChatDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.chat.ChatEntryResponse
import com.kikidan.data_remote.dto.chat.ConversationDetailResponse
import com.kikidan.data_remote.dto.chat.ConversationListResponse
import com.kikidan.data_remote.dto.chat.SendChatMessageRequest
import com.kikidan.data_remote.dto.chat.toChatStreamEventOrNull
import com.kikidan.data_remote.dto.chat.toDomain
import com.kikidan.data_remote.sse.serverSentEvents
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class RemoteChatDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteChatDataSource {
        override suspend fun getChatEntry(): ChatEntry =
            client
                .get()
                .get(CHAT_ENTRY_URL)
                .bodyNotNull<ChatEntryResponse>()
                .toDomain()

        override fun postChatMessage(
            conversationId: String?,
            content: String,
        ): Flow<ChatStreamEvent> =
            client
                .get()
                .serverSentEvents(CHAT_MESSAGES_URL) {
                    method = HttpMethod.Post
                    setBody(SendChatMessageRequest(conversationId, content))
                }.mapNotNull { it.toChatStreamEventOrNull() }

        override suspend fun getConversations(): List<ConversationSummary> =
            client
                .get()
                .get(CONVERSATIONS_URL)
                .bodyNotNull<ConversationListResponse>()
                .toDomain()

        override suspend fun getConversation(conversationId: String): Conversation =
            client
                .get()
                .get(conversationUrl(conversationId))
                .bodyNotNull<ConversationDetailResponse>()
                .toDomain()

        override suspend fun deleteConversation(conversationId: String) {
            client
                .get()
                .delete(conversationUrl(conversationId))
                .body<CommonResponse<Unit>>()
        }

        companion object {
            private const val CHAT_ENTRY_URL = "api/v1/chat/entry"
            private const val CHAT_MESSAGES_URL = "api/v1/chat/messages"
            private const val CONVERSATIONS_URL = "api/v1/chat/conversations"

            private fun conversationUrl(conversationId: String): String =
                "$CONVERSATIONS_URL/$conversationId"
        }
    }
```

- [ ] `core/data/src/main/java/com/kikidan/data/repository/ChatRepositoryImpl.kt`

```kotlin
package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteChatDataSource
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.repository.ChatRepository
import com.kikidan.domain.util.runCatchingCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ChatRepositoryImpl
    @Inject
    constructor(
        private val remoteChatDataSource: RemoteChatDataSource,
    ) : ChatRepository {
        override suspend fun getChatEntry(): Result<ChatEntry> =
            runCatchingCancellable { remoteChatDataSource.getChatEntry() }

        override fun sendMessage(
            conversationId: String?,
            content: String,
        ): Flow<Result<ChatStreamEvent>> =
            remoteChatDataSource
                .postChatMessage(conversationId, content)
                .map { Result.success(it) }
                // catch는 업스트림만 잡는다. map 뒤에 두어야 collect 블록의 예외를 삼키지 않는다.
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(Result.failure(throwable))
                }

        override suspend fun getConversations(): Result<List<ConversationSummary>> =
            runCatchingCancellable { remoteChatDataSource.getConversations() }

        override suspend fun getConversationDetail(conversationId: String): Result<Conversation> =
            runCatchingCancellable { remoteChatDataSource.getConversation(conversationId) }

        override suspend fun deleteConversation(conversationId: String): Result<Unit> =
            runCatchingCancellable { remoteChatDataSource.deleteConversation(conversationId) }
    }
```

- [ ] `core/data/src/main/java/com/kikidan/data/di/RepositoryModule.kt` — 추가

```kotlin
@Binds
@Singleton
abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
```

- [ ] `core/data-remote/src/main/java/com/kikidan/data_remote/di/RemoteDataSourceModule.kt` — 추가

```kotlin
@Binds
@Singleton
abstract fun bindChatRemoteDataSource(impl: RemoteChatDataSourceImpl): RemoteChatDataSource
```

- [ ] `core/data/src/test/java/com/kikidan/data/fake/FakeRemoteChatDataSource.kt` — 반환값/throw 예외를 주입 가능한 Fake (기존 `FakeRemoteAuthDataSource` 스타일)
- [ ] `core/data/src/test/java/com/kikidan/data/repository/ChatRepositoryImplTest.kt` — 2-6의 8케이스
- [ ] `core/data-remote/src/test/java/com/kikidan/data_remote/dto/chat/ChatMapperTest.kt` — 2-6의 12케이스
- [ ] `core/data-remote/src/test/java/com/kikidan/data_remote/datasource/RemoteChatDataSourceImplTest.kt` — 2-6의 7케이스

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[높음] C1·C2의 미해결 리스크가 그대로 상속된다** — SSE 이벤트 필드명 미확정, delta 증분/누적 여부, OkHttp 엔진 SSE 스트리밍 동작. D는 이들을 방어 코드로 흡수했을 뿐 해결하지 않았다. **dev 서버 스모크 테스트가 이 단위의 실질적 완료 조건이다.**
- [ ] **[높음] `ChatStreamEvent.Start`에서 새 conversationId를 못 받으면 대화 이어가기가 깨진다** — 신규 대화에서 첫 응답의 start 이벤트가 conversationId를 주지 않으면(추론이 틀리면), 두 번째 메시지도 `conversationId = null`로 나가 매번 새 대화가 만들어진다. 현재 매퍼는 이 경우 start 이벤트를 `null`로 버려 조용히 실패한다. **가장 눈에 안 띄는 실패 모드**이므로 스모크 테스트에서 "두 번째 메시지가 같은 대화에 들어가는지"를 반드시 확인할 것.
- [ ] **[중간] MockEngine으로 SSE 응답을 만드는 방법 확인 필요** — `respond(content = ByteReadChannel(sseText), headers = text/event-stream)`으로 Ktor SSE 플러그인이 정상 파싱하는지 검증 필요. 불가하면 `RemoteChatDataSourceImplTest`의 SSE 케이스는 제외하고 `ChatMapperTest`가 `ServerSentEvent` 객체를 직접 만들어 커버한다(파싱 로직 자체는 Ktor 책임이므로 커버리지 손실은 크지 않다).
- [ ] **[중간] `deleteConversation`의 `CommonResponse<Unit>` 역직렬화** — 기존 `postLogout`이 같은 패턴을 쓰고 있어 동작한다고 판단했으나, `data` 필드가 아예 없을 때 `Unit?`가 정상 처리되는지 테스트로 확인한다. 실패 시 `bodyAsText()`만 소비하거나 `CommonResponse<JsonElement>`로 받는다.
- [ ] **[중간] `getChatEntry`의 `bodyNotNull`이 `IllegalArgumentException`을 던지는 것이 적절한가** — 기존 컨벤션을 따랐다. Repository가 `Result.failure(IllegalArgumentException)`을 주게 되는데, presentation이 "서버 응답 이상"과 "잘못된 입력"을 구분하기 어렵다. 도메인 예외 타입 도입은 이번 범위 밖으로 두되 후속 과제로 등록 권장.
- [ ] **[낮음] `ChatMapper.kt`가 `TodakunJson`(internal)을 직접 참조** — 같은 모듈이라 컴파일은 되지만 `di` 패키지 의존이 생긴다. 거슬리면 `TodakunJson`을 `di`에서 `util` 또는 `serialization` 패키지로 옮기는 리팩터링을 함께 하되, 기존 테스트 2개의 import가 바뀌므로 별도 커밋으로 분리할 것.
- [ ] **[낮음] 대화 목록/상세의 페이지네이션 부재** — Swagger에 페이징 파라미터가 없다. 대화가 수백 개 쌓이면 목록 응답이 커진다. 서버에 상한이 있는지 확인 권장(현 시점에서 클라이언트가 할 일은 없음).
