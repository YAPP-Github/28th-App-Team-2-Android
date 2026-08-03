# 설계 문서 — #59 하위 B: 토닥이 채팅 Domain UseCase

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #73 (상위 이슈: #59)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: 단위 A (`designs/issue-72-chat-domain-model.md`) — `ChatRepository`, 도메인 모델 필요. 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

`core:domain`에 UseCase 5개를 추가한다. 구현체는 단위 D가 제공하므로 이 단위는 **인터페이스 대상 코딩만** 하며 단독으로 컴파일·테스트 가능하다.

- 포함: `GetChatEntryUseCase`, `SendChatMessageUseCase`, `GetConversationsUseCase`, `GetConversationDetailUseCase`, `DeleteConversationUseCase` + `SendChatMessageUseCase` 유닛 테스트 + Fake Repository
- 제외: ViewModel 연결(단위 F/G), Repository 구현(단위 D)

**파일 변경 개수: 7개 (신규 7, 수정 0)** — 10개 기준 충족.

## 2. 설계 및 실행 계획

### 2-1. UseCase가 필요한가? (먼저 던진 질문)

5개 중 4개는 Repository 함수 하나를 그대로 위임하는 한 줄짜리다. 그럼에도 만드는 이유는 명확하다:

- `rules/30-presentation.md`: **"ViewModel 또는 상태 처리에서 직접 도메인 로직을 수행하지 말고 UseCase를 통한다(P2)"** — 팀 규약이 UseCase 경유를 강제한다.
- ViewModel이 `ChatRepository`를 직접 주입받으면 `core:domain`의 repository 패키지가 presentation에 노출되고, 이후 조합 로직(예: 전송 후 quota 갱신)이 생겼을 때 ViewModel에 로직이 눌러앉는다.

단, **불필요한 추상화는 만들지 않는다**: UseCase 인터페이스를 따로 두고 Impl을 만드는 식의 이중화는 하지 않는다(구현이 하나뿐인 인터페이스). 기존 `LoginUseCase`와 동일하게 `class ... @Inject constructor(...)` + `operator fun invoke` 단일 클래스다.

### 2-2. 각 UseCase의 성격 판정

| UseCase | 성격 | 근거 |
|---------|------|------|
| `GetChatEntryUseCase` | 단순 위임 | `/chat/entry` 응답이 이미 greeting+suggestions+quota를 한 번에 준다. 클라이언트 조합 불필요 |
| `SendChatMessageUseCase` | **입력 검증 포함** | content 길이 제약(1~500자)이 서버 계약이며, 칩 탭 경로와 직접 입력 경로 **둘 다** 여기를 지난다 |
| `GetConversationsUseCase` | 단순 위임 | 정렬은 서버가 `lastMessageAt` 기준으로 준다고 가정(리스크 등록). 클라 정렬 추가 시 여기에 붙인다 |
| `GetConversationDetailUseCase` | 단순 위임 | 읽음 처리는 서버 사이드이펙트라 클라 조합 없음 |
| `DeleteConversationUseCase` | 단순 위임 | 삭제 후 목록 재조회는 ViewModel의 화면 흐름 책임이지 도메인 조합이 아님 |

**조합(퍼사드) UseCase는 만들지 않는다.** "삭제 후 목록 재조회" 같은 것을 UseCase로 묶고 싶은 유혹이 있지만, 그건 화면 전환 타이밍에 종속된 UI 흐름이고 낙관적 UI를 쓸지 여부에 따라 달라진다. 필요해지면 그때 만든다. (YAGNI)

### 2-3. `SendChatMessageUseCase`의 검증 위치 트레이드오프

content는 **신뢰 경계 입력**(사용자 자유 입력)이다. 검증을 생략하면 빈 문자열 전송으로 서버 400을 받고, 500자 초과 시에도 왕복 한 번을 낭비한다.

- **검증을 UI에만 두기**: 채팅 입력창과 추천 칩 두 진입점이 각각 검증해야 하고, 히스토리에서 이어 쓰는 경로가 추가되면 세 번째 중복이 생긴다. → 기각.
- **검증을 Repository에 두기**: Repository는 데이터 접근 관심사다. 비즈니스 규칙(길이 제약)의 자리가 아니다. → 기각.
- **검증을 UseCase에 두기 ← 채택**: 모든 호출자가 반드시 통과하는 단 하나의 지점이다. 추가로 `MAX_CONTENT_LENGTH`를 public const로 노출해 **TextField의 maxLength도 같은 상수를 참조**하게 만들면 UI와 검증이 어긋날 수 없다.

**검증 실패의 표현**: `require`로 예외를 던지면 반환 타입 `Flow<Result<...>>`의 계약을 깨고 호출부가 try-catch를 따로 달아야 한다. 대신 `flowOf(Result.failure(...))` 단일 원소 Flow를 반환해 **성공 경로와 실패 경로의 처리 코드를 동일하게** 만든다.

또한 `content.trim()`을 적용해 공백만 입력된 케이스를 걸러내고, trim된 값을 그대로 서버에 보낸다(검증 대상과 전송 대상 불일치 방지).

### 2-4. 실행 계획

1. UseCase 5개 작성 (아래 시그니처 그대로)
2. `core/domain/src/test/`에 `FakeChatRepository` + `SendChatMessageUseCaseTest` 작성
   - `core/domain/build.gradle.kts`에 `testImplementation(libs.junit)`, `testImplementation(libs.kotlinx.coroutines.test)`가 없으므로 **추가 필요** (현재 domain 모듈에 테스트 의존성 없음)
3. `./gradlew :core:domain:test` 통과 확인

**테스트 계획** (단순 위임 4개는 테스트하지 않는다 — 검증할 로직이 없다):

| 케이스 | 기대 |
|--------|------|
| 정상 content 전송 | Repository의 Flow가 그대로 전달됨 |
| content가 빈 문자열 | Repository 호출 없이 `Result.failure<IllegalArgumentException>` 1개 방출 후 종료 |
| content가 공백만 (`"   "`) | 위와 동일 |
| content가 501자 | 위와 동일 |
| content가 앞뒤 공백 포함 (`" 안녕 "`) | Repository에 `"안녕"`(trim된 값)이 전달됨 |
| content가 정확히 500자 | 정상 통과 (경계값) |

## 3. 파일 변경 계획 (구현 체크리스트)

```
core/domain/
├── build.gradle.kts                                        (수정) test 의존성 추가
└── src/
    ├── main/java/com/kikidan/domain/usecase/
    │   ├── GetChatEntryUseCase.kt                          (신규)
    │   ├── SendChatMessageUseCase.kt                       (신규)
    │   ├── GetConversationsUseCase.kt                      (신규)
    │   ├── GetConversationDetailUseCase.kt                 (신규)
    │   └── DeleteConversationUseCase.kt                    (신규)
    └── test/java/com/kikidan/domain/
        ├── fake/FakeChatRepository.kt                      (신규)
        └── usecase/SendChatMessageUseCaseTest.kt           (신규)
```

- [ ] `core/domain/build.gradle.kts` — dependencies에 추가

```kotlin
testImplementation(libs.junit)
testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/usecase/GetChatEntryUseCase.kt`

```kotlin
package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatEntryUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(): Result<ChatEntry> = chatRepository.getChatEntry()
    }
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/usecase/SendChatMessageUseCase.kt`

```kotlin
package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SendChatMessageUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        operator fun invoke(
            conversationId: String?,
            content: String,
        ): Flow<Result<ChatStreamEvent>> {
            val trimmed = content.trim()
            if (trimmed.isEmpty() || trimmed.length > MAX_CONTENT_LENGTH) {
                return flowOf(
                    Result.failure(
                        IllegalArgumentException("메시지는 1자 이상 ${MAX_CONTENT_LENGTH}자 이하여야 합니다."),
                    ),
                )
            }
            return chatRepository.sendMessage(conversationId, trimmed)
        }

        companion object {
            // presentation의 입력창 maxLength도 이 상수를 참조한다.
            const val MAX_CONTENT_LENGTH = 500
        }
    }
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/usecase/GetConversationsUseCase.kt`

```kotlin
package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class GetConversationsUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(): Result<List<ConversationSummary>> = chatRepository.getConversations()
    }
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/usecase/GetConversationDetailUseCase.kt`

```kotlin
package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class GetConversationDetailUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(conversationId: String): Result<Conversation> =
            chatRepository.getConversationDetail(conversationId)
    }
```

- [ ] `core/domain/src/main/java/com/kikidan/domain/usecase/DeleteConversationUseCase.kt`

```kotlin
package com.kikidan.domain.usecase

import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class DeleteConversationUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(conversationId: String): Result<Unit> =
            chatRepository.deleteConversation(conversationId)
    }
```

- [ ] `core/domain/src/test/java/com/kikidan/domain/fake/FakeChatRepository.kt` — `sendMessage` 호출 인자를 기록하고 지정된 Flow를 반환하는 최소 Fake

```kotlin
class FakeChatRepository : ChatRepository {
    var lastSentConversationId: String? = null
    var lastSentContent: String? = null
    var sendMessageCallCount: Int = 0
    var streamEvents: List<Result<ChatStreamEvent>> = emptyList()
    // 나머지 4개 함수는 Result.failure(NotImplementedError())로 둔다 (이 단위 테스트 대상 아님)
}
```

- [ ] `core/domain/src/test/java/com/kikidan/domain/usecase/SendChatMessageUseCaseTest.kt` — 2-4의 테스트 계획 6케이스

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[중간] `core:domain`에 테스트 의존성이 없다** — `build.gradle.kts` 수정이 필요하다. 이 단위가 domain 모듈 첫 유닛 테스트를 도입하는 셈이므로, 팀이 domain 테스트를 다른 모듈로 몰고 싶어 하는지 확인 필요.
- [ ] **[중간] content 최소 길이** — Swagger는 `0~500자`(minLength 0)로 되어 있어 빈 문자열이 서버 스펙상 허용될 여지가 있다. 클라이언트는 빈 메시지 전송이 무의미하므로 1자 이상으로 조였다. 서버가 실제로 400을 주는지 확인하면 좋다.
- [ ] **[중간] 대화 목록 정렬 보장 여부** — 서버가 `lastMessageAt` 내림차순으로 준다고 가정했다. 보장되지 않으면 `GetConversationsUseCase`에 `sortedByDescending { it.lastMessageAt }` 한 줄을 추가한다(그때 이 UseCase만 테스트 대상이 된다).
- [ ] **[낮음] quota 소진 시 사전 차단 여부** — `ChatQuota.remaining == 0`일 때 전송 자체를 UseCase가 막을지, 서버 error 이벤트에 맡길지. 현재 설계는 **막지 않는다**(quota는 entry 조회 시점 값이라 stale할 수 있고, 서버가 최종 판정자다). 화면에서 버튼 비활성화 정도로 처리 권장 — 단위 F/G 결정 사항.
- [ ] **[낮음] `SendChatMessageUseCase.invoke`가 `suspend`가 아닌 점** — Flow를 반환하는 cold stream이라 suspend가 불필요하다. 다른 4개와 시그니처 모양이 달라 보이지만 의도된 것이다.
