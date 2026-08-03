# 설계 문서 — #59 하위 C1: Ktor SSE 클라이언트 기반 구축

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #74 (상위 이슈: #59)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: 없음 (A/B와 병렬 진행 가능). 전체 그래프는 `designs/issue-59-task-dependency-graph.md` 참고

## 1. 범위

`core:data-remote`가 SSE(`text/event-stream`) 응답을 스트리밍으로 받을 수 있게 만드는 **인프라만** 다룬다. 채팅 도메인 지식은 이 단위에 들어오지 않는다.

- 포함:
  - `gradle/libs.versions.toml`에 `ktor-client-sse` 추가
  - `core/data-remote/build.gradle.kts`에 의존성 추가
  - `NetworkExtension.kt`의 `installTodakunDefaults`에 `SSE` 플러그인 install
  - 재사용 가능한 `HttpClient.serverSentEvents(...): Flow<ServerSentEvent>` 확장 함수
  - MockEngine 기반 유닛 테스트
- 제외: Chat DTO(C2), DataSource/Repository(D)

**파일 변경 개수: 5개 (신규 2, 수정 3)** — 10개 기준 충족.

## 2. 설계 및 실행 계획

### 2-1. 왜 별도 확장 함수인가

Ktor의 SSE API는 **세션 스코프 람다**(`client.sse(request) { incoming.collect { ... } }`) 형태다. 이걸 DataSource에서 직접 쓰면:

- DataSource가 `ChatStreamEvent` 매핑과 세션 수명 관리(취소·close)를 동시에 떠안는다.
- 향후 SSE를 쓰는 다른 엔드포인트가 생기면 세션 관리 코드가 복사된다.
- **가장 중요한 것**: 아래 2-2의 타임아웃 함정을 매 호출부가 기억해야 한다.

따라서 "SSE 요청 → `Flow<ServerSentEvent>`" 어댑터 **한 개**만 만든다. 그 이상은 만들지 않는다 — 이벤트 파싱은 도메인 지식이므로 D의 매퍼 몫이다.

### 2-2. **[핵심] 기존 `HttpTimeout` 설정이 SSE를 죽인다**

`core/data-remote/di/NetworkExtension.kt`의 현재 설정:

```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 15_000
    connectTimeoutMillis = 10_000
    socketTimeoutMillis = 15_000
}
```

`requestTimeoutMillis = 15_000`은 **요청 시작부터 응답 완료까지의 전체 시간**이다. SSE는 LLM 응답이 끝날 때까지 응답 본문이 열려 있으므로, 답변이 15초를 넘기는 순간 `HttpRequestTimeoutException`으로 스트림이 끊긴다. 토닥이 답변은 15초를 쉽게 넘긴다.

이 프로젝트에서 이 설정을 그대로 두고 SSE를 붙이면 **개발 중에는 짧은 답변으로 통과하고 실사용에서 끊긴다**. 반드시 SSE 요청에서 per-request로 덮어써야 한다:

```kotlin
timeout {
    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
    socketTimeoutMillis = SSE_SOCKET_TIMEOUT_MS   // 60_000: 이벤트 간 무응답 감지용
}
```

- `requestTimeoutMillis`: 무한. 스트림 전체 길이에 상한을 두면 안 된다.
- `socketTimeoutMillis`: **무한으로 두지 않는다.** 이걸 무한으로 하면 서버/네트워크가 조용히 죽었을 때 앱이 영원히 "생각 중"에 머문다. 이벤트 간 간격 상한(60초)으로 두면 죽은 연결을 감지할 수 있다. LLM 토큰 간 간격은 60초를 넘지 않는다.
- `connectTimeoutMillis`: 기존 10초 유지 (연결 수립 단계라 SSE와 무관).

> 이 타임아웃 오버라이드를 확장 함수 **안에** 넣어 호출부가 잊을 수 없게 만든다. 이것이 확장 함수를 만드는 가장 실질적인 이유다.

### 2-3. 전역 클라이언트 재사용 vs 별도 SSE 클라이언트

- **별도 클라이언트 추가**: SSE 전용 `@Named("sse") HttpClient`를 만들면 타임아웃을 클라이언트 레벨에서 분리할 수 있다. 하지만 Bearer Auth 설정·토큰 갱신 로직·`BearerTokenCacheInvalidator` 연동을 전부 이중화해야 하고, 두 클라이언트가 서로 다른 토큰 캐시를 갖는 미묘한 버그가 생긴다. → **기각**.
- **기존 클라이언트에 `SSE` 플러그인만 install ← 채택**: `SSE` 플러그인은 SSE 요청에만 관여하고 일반 요청에는 영향이 없다. 타임아웃은 2-2처럼 per-request로 처리한다. 변경 표면이 `install(SSE)` 한 줄이다.

### 2-4. `installTodakunDefaults`에 넣는 이유

`installTodakunDefaults`는 프로덕션 `NetworkModule`과 기존 유닛 테스트(`RemoteAuthDataSourceImplTest`, `BearerAuthIntegrationTest`)가 **공유하는 단일 설정 지점**이다. 여기에 넣어야 MockEngine 테스트에서도 SSE가 동작한다. 별도 함수로 빼면 테스트에서 install을 빠뜨리는 사고가 난다.

### 2-5. 기존 플러그인과의 상호작용 점검

| 플러그인 | SSE 영향 | 판단 |
|---------|---------|------|
| `ContentNegotiation(json)` | SSE 응답은 `body<T>()`를 거치지 않고 `incoming` Flow로 소비되므로 개입하지 않음 | 문제 없음 |
| `DefaultRequest { contentType(Application.Json) }` | POST **요청 본문**이 JSON인 게 맞다. SSE 플러그인이 `Accept: text/event-stream`을 별도로 세팅 | 문제 없음 |
| `Auth (bearer)` | `sendWithoutRequest`의 `NO_AUTH_PATHS`에 `api/v1/chat/**`가 없으므로 Bearer 토큰이 자동 첨부됨 | 문제 없음 |
| `expectSuccess = true` | 비-2xx 응답 시 세션 생성 단계에서 `ClientRequestException`/`ServerResponseException`을 throw → Flow 밖으로 전파 → D의 Repository가 `Result.failure`로 수렴 | **의도된 동작** |

### 2-6. Flow 어댑터 구현 방식

```kotlin
flow {
    val session = sseSession(request)
    try {
        emitAll(session.incoming)
    } finally {
        session.cancel()
    }
}
```

- `flow { }` + `emitAll`을 쓴다. `channelFlow`가 아닌 이유: 크로스 컨텍스트 방출이 없어서 불필요한 채널 오버헤드를 만들 필요가 없다.
- `finally { session.cancel() }`: **필수**. 사용자가 화면을 벗어나 collect가 취소되면 세션도 닫혀야 커넥션이 반납된다. 이게 없으면 화면 재진입마다 커넥션이 샌다.
- 서버 사이드 사이드이펙트에 대한 참고: Swagger가 "SSE 연결이 끊겨도 답변 생성·저장·알림 발송은 서버에서 끝까지 진행된다"고 명시하므로, 취소로 커넥션을 끊는 것은 안전하다(답변은 히스토리에 남는다).

> ⚠️ Ktor 3.1.3의 정확한 함수명(`sseSession` vs `sse`)과 `SSESession`이 `CoroutineScope`를 구현하는지는 **구현 시점에 IDE 자동완성으로 확인**한다. 확인 실패 시 폴백은 아래 4절 참조.

### 2-7. 실행 계획

1. `libs.versions.toml`에 라이브러리 항목 추가 (`ktor` 버전 참조 = 3.1.3)
2. `core/data-remote/build.gradle.kts`에 `implementation(libs.ktor.client.sse)` 추가
3. `NetworkExtension.kt`의 `installTodakunDefaults`에 `install(SSE)` 추가
4. `core/data-remote/.../sse/SseFlow.kt` 작성
5. `SseFlowTest` 작성 후 `./gradlew :core:data-remote:test` 통과
6. **[수동 검증 필수]** dev 서버에 실제 POST `/api/v1/chat/messages`를 쏘는 임시 스모크 코드로 OkHttp 엔진에서 이벤트가 **점진적으로** 도착하는지(한 번에 몰려오지 않는지) 확인 후 코드 제거

**테스트 계획** (MockEngine으로 `text/event-stream` 응답 시뮬레이션):

| 케이스 | 기대 |
|--------|------|
| `event: start\ndata: {...}\n\n` 3개 연속 응답 | `ServerSentEvent` 3개가 순서대로 방출 |
| 이벤트 이름 없는 `data: hello\n\n` | `event == null`, `data == "hello"`인 이벤트 방출 |
| 서버가 스트림을 정상 종료 | Flow가 정상 완료 |
| 404 응답 | `ClientRequestException`이 collect 시점에 throw |
| collect 취소 | 세션이 닫히고 예외 없이 종료 |

## 3. 파일 변경 계획 (구현 체크리스트)

```
gradle/
└── libs.versions.toml                                          (수정) ktor-client-sse 추가
core/data-remote/
├── build.gradle.kts                                            (수정) 의존성 1줄
└── src/
    ├── main/java/com/kikidan/data_remote/
    │   ├── di/NetworkExtension.kt                              (수정) install(SSE)
    │   └── sse/SseFlow.kt                                      (신규) serverSentEvents 확장 함수
    └── test/java/com/kikidan/data_remote/
        └── sse/SseFlowTest.kt                                  (신규)
```

- [ ] `gradle/libs.versions.toml` — `[libraries]` 섹션, 기존 ktor 항목 옆에 추가

```toml
ktor-client-sse = { group = "io.ktor", name = "ktor-client-sse", version.ref = "ktor" }
```

- [ ] `core/data-remote/build.gradle.kts` — `implementation(libs.ktor.client.auth)` 아래에 추가

```kotlin
implementation(libs.ktor.client.sse)
```

- [ ] `core/data-remote/src/main/java/com/kikidan/data_remote/di/NetworkExtension.kt` — `installTodakunDefaults` 내부, `install(DefaultRequest)` 뒤에 추가

```kotlin
import io.ktor.client.plugins.sse.SSE
// ...
internal fun HttpClientConfig<*>.installTodakunDefaults(
    json: Json,
    baseUrl: String,
) {
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }
    install(DefaultRequest) {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
    install(SSE)          // <-- 추가
    expectSuccess = true
}
```

- [ ] `core/data-remote/src/main/java/com/kikidan/data_remote/sse/SseFlow.kt` (신규)

```kotlin
package com.kikidan.data_remote.sse

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.sse.sseSession
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// 이벤트 간 무응답 상한. 무한으로 두면 서버가 조용히 죽었을 때 앱이 영원히 대기한다.
private const val SSE_SOCKET_TIMEOUT_MS = 60_000L

/**
 * SSE 엔드포인트를 열어 서버 이벤트를 Flow로 방출한다.
 * collect가 취소되면 세션도 닫힌다. 비-2xx 응답은 collect 시점에 예외로 전파된다.
 */
fun HttpClient.serverSentEvents(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): Flow<ServerSentEvent> =
    flow {
        val session =
            sseSession {
                url(urlString)
                block()
                // 전역 HttpTimeout(requestTimeoutMillis=15s)이 장시간 스트림을 끊으므로 반드시 덮어쓴다.
                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    socketTimeoutMillis = SSE_SOCKET_TIMEOUT_MS
                }
            }
        try {
            emitAll(session.incoming)
        } finally {
            session.cancel()
        }
    }
```

- [ ] `core/data-remote/src/test/java/com/kikidan/data_remote/sse/SseFlowTest.kt` (신규) — 2-7의 5케이스. MockEngine 응답 헤더는 `ContentType.Text.EventStream`, 본문은 `ByteReadChannel("event: start\ndata: {\"conversationId\":\"c-1\"}\n\n...")`

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[높음] OkHttp 엔진의 SSE 스트리밍 동작 검증** — Ktor의 `SSE` 플러그인은 응답 본문 채널 위에 구현되어 엔진 비의존적이지만, OkHttp 엔진이 응답을 버퍼링하면 이벤트가 실시간이 아니라 스트림 종료 시 한꺼번에 도착할 수 있다. 이 경우 "한 글자씩 타이핑" UX가 성립하지 않는다.
  **→ 반드시 dev 서버 대상 실기기/에뮬레이터 스모크 테스트로 확인할 것.** MockEngine 테스트로는 이 문제를 잡을 수 없다.
  실패 시 대안: (1) `ktor-client-cio` 엔진을 SSE 전용으로 추가, (2) OkHttp 레벨에서 `Interceptor`로 응답 압축/버퍼링 해제(`Accept-Encoding: identity`).
- [ ] **[높음] Ktor 3.1.3의 SSE에서 POST + 요청 본문 지원 여부** — Ktor 2.x 시절 SSE는 GET 전용이었다. 3.x에서 `HttpRequestBuilder`를 그대로 받으므로 `method = HttpMethod.Post` + `setBody(...)`가 동작해야 하지만, 구현 착수 시 **가장 먼저 확인해야 할 항목**이다.
  실패 시 대안: SSE 플러그인을 쓰지 않고 `client.preparePost(...).execute { it.bodyAsChannel() }`로 직접 라인 파싱(`data:` / `event:` 접두사). 이 경우 `SseFlow.kt`가 수동 파서로 바뀌며 파일 1개 규모는 유지된다.
- [ ] **[중간] `sseSession` API 시그니처 확인** — `sseSession` / `sse` 중 어느 것이 3.1.3에 있는지, `SSESession`이 `CoroutineScope`를 구현해 `session.cancel()`이 가능한지 확인. 불가하면 `channelFlow { sse(request) { incoming.collect { send(it) } } }` 형태로 대체한다(동작 동일, 채널 오버헤드만 추가).
- [ ] **[중간] `SSE_SOCKET_TIMEOUT_MS = 60_000` 적정성** — 서버가 heartbeat(빈 comment 라인)를 보내는지에 따라 조여도 된다. 백엔드에 heartbeat 유무 확인 권장. heartbeat가 있다면 15~20초로 줄여 죽은 연결을 더 빨리 감지할 수 있다.
- [ ] **[중간] 401 재발급과 SSE의 상호작용** — Auth 플러그인의 `refreshTokens`는 최초 요청 응답이 401일 때 동작한다. SSE 세션 수립 단계의 401은 정상 처리되지만, **스트림 도중** 토큰이 만료되는 경우는 재시도되지 않고 연결이 끊긴다. 답변 생성 시간이 토큰 유효기간보다 짧으므로 실사용 문제는 아니라고 판단했으나 확인 필요.
- [ ] **[낮음] 재연결(reconnection) 정책** — Ktor SSE의 `reconnectionTime`/자동 재연결은 사용하지 않는다. 재연결 시 중복 delta가 도착해 답변이 두 번 출력될 수 있고, 서버는 어차피 답변을 저장하므로 실패 시 히스토리 재조회가 더 안전하다. 이 판단에 이견이 있으면 검토 필요.
