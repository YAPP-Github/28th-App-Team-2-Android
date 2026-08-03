# 설계 문서 — #59 하위 F-chat: feature:chat 모듈 + ChatViewModel (Orbit MVI)

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #59 하위 작업 단위 F-chat (사용자가 추후 별도 이슈로 생성 예정)
- **작성**: 설계 에이전트 (Opus) / 2026-08-03
- **상태**: 검토 대기
- **선행**: A(도메인 모델), B(UseCase), D(RepositoryImpl+Hilt 바인딩) — **3개 모두 머지된 뒤 시작**

## 1. 범위

이 저장소 **최초의 Compose + Orbit feature 모듈**을 만들고, 채팅 화면의 상태 기계를 구현한다.
화면(Composable)은 G-chat, 히스토리는 F-history/G-history가 같은 모듈에 이어 붙인다.

- 포함:
  - `feature:chat` 모듈 스캐폴딩 (`build.gradle.kts`, `AndroidManifest.xml`, `settings.gradle.kts` 등록, `app` 의존 추가)
  - `ChatContract.kt` — `ChatState` / `ChatPhase` / `ChatSideEffect`
  - `ChatViewModel.kt` — Orbit `ContainerHost`
  - `TypewriterFlow.kt` — **도착 속도와 표시 속도를 분리하는 Flow 연산자** (이 단위의 핵심)
  - 유닛 테스트 3종 (`TypewriterFlowTest`, `ChatViewModelTest`, `FakeChatRepository`)
- 제외:
  - Composable / Navigation (G-chat)
  - 히스토리 상태·화면 (F-history / G-history)
  - **Hilt `@Module` 파일** — 바인딩할 것이 없다. UseCase는 `@Inject constructor`, `ChatRepository` 바인딩은 D의 `core:data`가 이미 제공한다. 모듈 파일을 만들면 빈 껍데기가 된다. (YAGNI)

**파일 변경 개수: 11개 (신규 8, 수정 3)** — "10개 내외" 기준 경계. `settings.gradle.kts`/`app/build.gradle.kts`/`AndroidManifest.xml`은 모듈 신설에 딸린 한 줄짜리 변경이라 실질 작업 파일은 6개다.

### 전체 작업 단위 의존 그래프

전체 9개 단위의 실행 의존성은 `designs/issue-59-task-dependency-graph.md`를 단일 소스로 참고한다(개별 문서에 중복 작성하지 않는다). 이 단위는 그 문서의 **A, B(컴파일 의존) + D(런타임/DI 의존)**에 해당한다.

## 2. 설계 및 실행 계획

### 2-1. 규칙 준수 근거

| 규칙 | 준수 방법 |
|------|-----------|
| `rules/00`: feature → domain만 허용, feature 간 참조 금지 (P1) | `feature:chat`은 `core:domain` + `core:designsystem`만 의존한다. `core:data*`·`core:navigation`·다른 feature 의존 없음 |
| `rules/00`: DI는 Hilt, 수동 팩토리 금지 | `@HiltViewModel` + 생성자 주입. 서비스로케이터·수동 팩토리 없음 |
| `rules/30`: 도메인 로직을 ViewModel에서 직접 수행 금지 (P2) | 5개 UseCase만 주입. `ChatRepository`를 직접 주입하지 않는다 |
| `rules/30`: ViewModel은 상태 관리에 집중 | 검증(길이 제한)은 B의 `SendChatMessageUseCase`가, 매핑은 D가 담당. VM은 이벤트→State 변환만 한다 |
| `rules/30`: `SavedStateHandle` 검토 | **검토 후 사용하지 않는다.** 근거는 2-5 |
| `rules/40`: 불안정 파라미터로 인한 리컴포지션 (P2) | 2-6 참조 (Compose 강한 건너뛰기 + 문자 단위 갱신을 `String` 필드 하나로 격리) |

### 2-2. **[핵심] 타이핑 애니메이션을 어디에 둘 것인가**

문제 정의: SSE `delta` 한 개가 여러 글자를 담아 올 수 있고(청크 크기는 서버/네트워크가 결정), 실제 LLM 스트리밍은 초당 40~150자로 **일정하지 않게 뭉쳐서** 도착한다. "한 글자씩 순차 렌더링"을 만들려면 **도착 속도와 표시 속도를 분리**하는 버퍼 + 고정 간격 타이머가 반드시 필요하다.

| 옵션 | 구현 위치 | 장점 | 단점 | 판정 |
|------|-----------|------|------|------|
| **1. Composable** (`LaunchedEffect` + `remember` 버퍼) | G-chat | 상태 갱신이 UI 로컬이라 State 복사 비용 0 | 화면 회전/구성 변경에 버퍼가 통째로 사라져 답변이 중간에 끊긴다. "타이핑 완료" 시점을 VM이 몰라 전송 버튼 잠금·스크롤·완료 처리를 다시 위로 올려야 한다. 검증에 Compose UI 테스트(에뮬레이터)가 필요한데 이 저장소엔 인프라가 없다 | 기각 |
| **2. ViewModel 안에 인라인 타이머** | F-chat | 상태가 하나 | 버퍼·틱 루프·이벤트 분기가 한 `intent` 안에 뒤엉켜 40줄이 된다. 타이밍 로직만 따로 테스트할 수 없다 | 기각 |
| **3. `Flow<String>.typewriter()` 연산자 + VM이 collect ← 채택** | F-chat | VM은 "구독해서 `reduce`" 한 줄로 남는다. 타이밍 로직은 순수 Flow 연산자라 `runTest` 가상 시간으로 **밀리초 단위 정확히** 단위 테스트된다. 클래스·인터페이스를 새로 만들지 않는다(확장 함수 1개) | State 갱신이 틱마다 일어난다 → 2-6에서 비용 격리 | **채택** |

**옵션 3의 파이프라인 구성**

`sendMessage`는 `Flow<Result<ChatStreamEvent>>` 하나로 오는데, 타이핑 대상은 `Delta`뿐이고 `Start`/`Done`은 텍스트가 아니다. 그래서 파이프라인을 **둘로 쪼개지 않고** `transform`으로 갈라낸다.

```
sendChatMessage(...)                       Flow<Result<ChatStreamEvent>>
  └ transform { Start/Done은 지역 변수에 담고, Delta의 content만 아래로 emit }
                                           Flow<String>   (도착 속도 그대로)
  └ typewriter()                           Flow<String>   (표시 속도로 재타이밍, 누적 텍스트)
  └ collect { reduce { streamingText = it } }
```

이 구조의 이점이 결정적이다: **`typewriter()`가 완료되는 시점이 곧 "버퍼가 다 비었다"는 뜻**이므로, `collect`가 끝난 다음 줄에서 `Done` 메시지를 확정하면 "네트워크는 끝났는데 화면엔 아직 절반만 찍힌" 상태에서 답변이 튀는 문제가 자동으로 사라진다. 별도 동기화 장치가 필요 없다.

**연산자 내부 설계 — 왜 "한 틱에 한 글자"가 아닌가**

고정 1글자/틱은 실패한다. LLM 생성 속도(40~150자/s)가 사람이 읽기 좋은 타이핑 속도(~42자/s)보다 빠르므로 백로그가 답변 내내 단조 증가하고, 500자 답변이면 생성이 끝난 뒤에도 **7초 이상 혼자 타이핑**한다.

→ 매 틱 `1 + (남은 글자 / CATCH_UP_DIVISOR)`글자를 방출한다. 백로그가 작으면 1글자씩(= 한 글자씩 타이핑 효과), 백로그가 쌓이면 자동으로 빨라져 지연이 발산하지 않는다. 상수 2개(`TYPING_TICK_MS = 16`, `CATCH_UP_DIVISOR = 12`)는 실기기에서 조정할 **튜닝 노브**로 남긴다 — 체감 속도는 실제 서버 청크 크기에 좌우되므로 코드만 보고 확정할 수 없다.

틱 간격 16ms는 60fps 프레임 간격이다. 이보다 짧게 잡으면 한 프레임 안에서 두 번 `reduce`가 일어나 낭비다.

### 2-3. 에러 채널

A/D가 "모든 실패는 `Result.failure` 하나로 수렴"을 확정했다. VM은 `transform` 안에서 `result.getOrElse { throw it }`로 **예외로 되돌려** 파이프라인 밖으로 던지고, `try/catch` 한 곳에서 처리한다. 분기가 collect 지점 한 군데뿐이다.

`CancellationException`은 명시적으로 재throw한다. 화면 이탈로 스트림이 취소됐을 때 "답변 실패" 스낵바가 뜨는 사고를 막는다 (D 문서 2-3과 동일한 가드).

### 2-4. 진입 로드 — `getChatEntry`와 `getConversationDetail`의 관계

- `conversationId == null` (신규): `getChatEntry()`만. 인사말 + 추천 칩 + quota.
- `conversationId != null` (히스토리에서 진입): `getChatEntry()` + `getConversationDetail(id)`. **entry도 호출한다** — 헤더의 "오늘 무료 채팅 n/m"이 두 경우 모두 필요하기 때문이다. 추천 칩은 메시지가 이미 있으므로 화면에서 숨긴다(G-chat).

두 호출을 `async`로 병렬화할 수 있지만 **순차로 둔다.** 화면 진입 1회의 왕복 1회 추가일 뿐이고, 병렬화는 실측 후에 붙여도 늦지 않다. (YAGNI — 4절에 후속 항목으로 등록)

entry 조회가 실패해도 화면은 동작해야 한다(입력은 가능). 실패 시 State는 비워두고 `ShowMessage` 사이드이펙트만 낸다. 재시도 UI는 만들지 않는다.

### 2-5. `SavedStateHandle`을 쓰지 않는 이유 (rules/30 "검토" 항목)

`conversationId`는 Navigation 3에서 **백스택의 키 객체(`ChatKey`)가 들고 있는 값**이고, 백스택은 `rememberNavBackStack`으로 `SavedState`에 저장·복원된다. VM이 같은 값을 이중으로 저장하면 두 저장소가 어긋날 수 있다.

스트리밍 중인 텍스트는 프로세스 사망 시 사라지지만, **서버가 답변 생성·저장을 끝까지 진행한다는 것이 Swagger에 명시**돼 있으므로(C1 문서 2-6) 히스토리 재진입으로 온전히 복구된다. 프로세스 사망 대비 스트림 상태 직렬화는 얻는 것에 비해 복잡도가 크다.

→ **사용하지 않는다.** `conversationId`는 화면 진입 시 `load(conversationId)`로 1회 주입받는다.

### 2-6. State 형태 결정

- **스트리밍 텍스트를 `messages` 리스트 안에 넣지 않는다.** 넣으면 문자 하나마다 `List`를 새로 만들어 복사한다. 별도 `streamingText: String` 필드로 빼면 틱당 비용이 data class 얕은 복사 1회로 끝난다. 화면에선 리스트 마지막에 이어 그린다(G-chat).
- **UI 전용 메시지 모델을 만들지 않는다.** 도메인 `ChatMessage`를 그대로 State에 담는다. 낙관적으로 추가하는 사용자 메시지는 `id = "local-user-<epochMillis>"`, `status = PENDING`으로 만든다 — 도메인 모델이 이미 `PENDING`을 갖고 있어 별도 타입이 필요 없다.
- **불안정 클래스 우려**: `ChatMessage`는 Compose 미의존 모듈의 data class이고 `List`는 인터페이스라 둘 다 Compose가 안정으로 추론하지 못한다. 다만 Kotlin 2.2 / Compose 컴파일러의 **강한 건너뛰기(strong skipping)가 기본 활성**이라 값이 같으면 리컴포지션이 건너뛰어진다. `kotlinx-collections-immutable`을 이 한 건 때문에 새로 추가하지 않는다.
- **에러를 State에 두지 않는다.** 일회성 알림이므로 `ShowMessage` 사이드이펙트로만 낸다. State에 두면 "이미 본 에러"를 지우는 코드가 따라붙는다.

### 2-7. 전송 경로의 가드를 VM 한 곳에 모으는 이유

전송 진입점이 3개다: 입력창 전송, 추천 칩 탭, (향후) 액션 재전송. 가드를 화면에 두면 3중 중복이 된다. `send(content)` intent 하나에 모은다.

| 가드 | 처리 |
|------|------|
| 이미 스트리밍 중 (`phase != IDLE`) | 무시하고 반환. `TodakunChatInputField`에 `enabled` 파라미터가 없어 화면에서 막을 수 없다 |
| 입력 길이 > `MAX_CONTENT_LENGTH` | `onInputChange`에서 자른다. B가 노출한 상수를 그대로 참조해 UI와 검증이 어긋나지 않게 한다 |
| 공백/빈 문자열 | B의 UseCase가 `Result.failure`로 처리 → 기존 에러 경로 재사용 |
| quota 소진 (`remaining <= 0`) | 전송하지 않고 `ShowMessage`. B 문서가 "화면에서 버튼 비활성화 정도로 처리"를 F/G에 위임했고, 컴포넌트에 비활성 상태가 없으므로 안내 문구로 대체한다 |

### 2-8. `by container(...)` 위임에 대하여 (rules/30)

`rules/30`은 "`ContainerHost` 위임은 `by(Delegate)` 형태를 권장"한다고 적고 있으나, **Orbit 11의 `container()`는 델리게이트 제공자가 아니라 `Container`를 반환하는 팩토리 함수**다. 따라서 `by container(...)` 문법은 컴파일되지 않는다. 규칙의 실질 의도(= `ContainerHost` 구현을 손으로 짜지 말고 제공 팩토리를 쓸 것)는 `override val container = container<S, E>(...)`로 충족된다. 이 저장소 최초의 Orbit 사용처이므로 4절에 규칙 문구 정정 항목으로 등록한다.

### 2-9. 실행 계획

1. `settings.gradle.kts`에 `include(":feature:chat")`, `app/build.gradle.kts`에 `implementation(projects.feature.chat)` 추가
2. `feature/chat/build.gradle.kts` + `AndroidManifest.xml` 작성 → `./gradlew :feature:chat:assembleDebug` 통과 확인 (빈 모듈 상태에서 먼저)
3. `TypewriterFlow.kt` **테스트 먼저 작성**(TDD) → 구현 → `:feature:chat:test` 통과
4. `ChatContract.kt` → `ChatViewModel.kt`
5. `FakeChatRepository` + `ChatViewModelTest`
6. `./gradlew :feature:chat:test :feature:chat:ktlintCheck` 통과

**테스트 계획**

`TypewriterFlowTest` (`runTest` 가상 시간) — 이 단위에서 유일하게 비자명한 로직이므로 가장 촘촘히 검증한다.

| 케이스 | 기대 |
|--------|------|
| 청크 `"안녕하세요"` 1개 방출 | 방출이 2회 이상으로 쪼개지고, 각 방출은 직전 방출의 prefix 확장이며, 마지막 값 == `"안녕하세요"` |
| 청크 3개(`"안"`,`"녕"`,`"!"`)를 간격 두고 방출 | 마지막 값 == `"안녕!"` |
| **업스트림이 대량 청크 직후 즉시 완료** | 남은 버퍼가 전부 방출된 뒤 Flow가 완료된다 (가장 중요 — 답변 뒷부분 유실 회귀 방지) |
| 업스트림이 빈 채로 완료 | 방출 없이 정상 완료 |
| 업스트림이 예외 throw | 동일 예외가 collect 지점으로 전파 |
| 1000자 단일 청크 | catch-up 덕분에 총 소요 가상시간이 1000틱 미만 |
| collect 취소 | 예외 없이 종료, 업스트림 collect도 취소 |

`ChatViewModelTest` (`orbit-test`)

| 케이스 | 기대 |
|--------|------|
| `load(null)` + entry 성공 | `suggestions`/`quota`/`greeting` 반영, `isLoading = false` |
| `load(null)` + entry 실패 | `ShowMessage` 사이드이펙트, `isLoading = false` |
| `load("c-1")` | detail의 메시지가 `messages`에 채워짐 |
| `send("안녕")` | 사용자 메시지가 즉시 `messages`에 추가되고 `phase == THINKING` |
| Delta 도착 | `phase == TYPING`, `streamingText`가 단조 증가(각 값이 이전 값의 prefix 확장) |
| Done 수신 | `messages` 마지막이 서버 메시지, `streamingText == ""`, `phase == IDLE`, `quota.used`가 1 증가 |
| Start의 conversationId 저장 | 두 번째 `send` 시 Fake에 그 id가 전달됨 (**A/D 문서가 "가장 눈에 안 띄는 실패 모드"로 지목한 항목**) |
| 스트림이 `Result.failure` 방출 | `ShowMessage`, `phase == IDLE`, `streamingText == ""` |
| 스트리밍 중 `send` 재호출 | 무시됨 (Fake의 호출 횟수 1 유지) |
| `quota.remaining == 0`에서 `send` | 전송 없이 `ShowMessage` |
| `onInputChange`에 501자 | `input.length == 500` |
| `startNewConversation()` | `conversationId == null`, `messages` 비움, 추천 칩 유지 |

## 3. 파일 변경 계획 (구현 체크리스트)

```
settings.gradle.kts                                         (수정) include(":feature:chat")
app/build.gradle.kts                                        (수정) implementation(projects.feature.chat)
feature/chat/
├── build.gradle.kts                                        (신규)
└── src/
    ├── main/
    │   ├── AndroidManifest.xml                             (신규)
    │   └── java/com/kikidan/chat/
    │       ├── ChatContract.kt                             (신규) ChatState / ChatPhase / ChatSideEffect
    │       ├── ChatViewModel.kt                            (신규)
    │       └── TypewriterFlow.kt                           (신규) Flow<String>.typewriter()
    └── test/java/com/kikidan/chat/
        ├── FakeChatRepository.kt                           (신규)
        ├── TypewriterFlowTest.kt                           (신규)
        └── ChatViewModelTest.kt                            (신규)
```

- [ ] `settings.gradle.kts` — 마지막 줄에 추가

```kotlin
include(":feature:chat")
```

- [ ] `app/build.gradle.kts` — `implementation(projects.feature.auth)` 아래에 추가

```kotlin
implementation(projects.feature.chat)
```

- [ ] `feature/chat/build.gradle.kts` (신규) — Compose 설정은 `core/designsystem/build.gradle.kts`를, Hilt 설정은 `feature/auth/build.gradle.kts`를 따른다

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kikidan.chat"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.designsystem)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.orbit.test)
}
```

> `core:data*` / `core:navigation` / 다른 feature 모듈에 **의존하지 않는다**(rules/00 P1 회피). Hilt 바인딩은 app이 모으고, 라우팅은 G-chat이 app 쪽에서 조립한다.

- [ ] `feature/chat/src/main/AndroidManifest.xml` (신규)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/TypewriterFlow.kt` (신규)

```kotlin
package com.kikidan.chat

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 실기기에서 조정할 튜닝 노브. 체감 속도는 서버가 보내는 청크 크기에 좌우된다.
internal const val TYPING_TICK_MS = 16L          // 60fps 프레임 간격
private const val CATCH_UP_DIVISOR = 12          // 남은 글자의 1/12를 매 틱 추가 방출

/**
 * 도착 속도(네트워크)와 표시 속도(화면)를 분리한다.
 *
 * 업스트림 청크를 버퍼에 쌓고 tick마다 조금씩 잘라 "지금까지 보여줄 전체 텍스트"를 방출한다.
 * 백로그가 작으면 한 글자씩, 쌓이면 자동으로 빨라져 생성 종료 후 혼자 타이핑하는 지연을 막는다.
 * 업스트림이 끝나도 버퍼가 다 빌 때까지 방출을 이어간 뒤 완료한다.
 */
internal fun Flow<String>.typewriter(tickMillis: Long = TYPING_TICK_MS): Flow<String> =
    flow {
        val buffered = MutableStateFlow("")
        coroutineScope {
            val upstream = launch { this@typewriter.collect { chunk -> buffered.update { it + chunk } } }
            var shown = 0
            while (true) {
                // isActive를 value보다 먼저 읽어야 한다. 순서를 바꾸면
                // "value를 읽은 직후 도착한 마지막 청크"를 못 보고 종료할 수 있다.
                val upstreamDone = !upstream.isActive
                val full = buffered.value
                when {
                    shown < full.length -> {
                        shown = (shown + 1 + (full.length - shown) / CATCH_UP_DIVISOR)
                            .coerceAtMost(full.length)
                        // ponytail: 틱마다 substring이라 전체 O(n^2). 수천 자 답변까지는 무시 가능.
                        emit(full.substring(0, shown))
                    }
                    upstreamDone -> return@coroutineScope
                    else -> Unit
                }
                delay(tickMillis)
            }
        }
    }
```

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ChatContract.kt` (신규)

```kotlin
package com.kikidan.chat

import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatSuggestion

data class ChatState(
    val conversationId: String? = null,
    val isLoading: Boolean = true,
    val greeting: String = "",
    val suggestions: List<ChatSuggestion> = emptyList(),
    val quota: ChatQuota? = null,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val phase: ChatPhase = ChatPhase.IDLE,
    // 스트리밍 중인 답변. messages에 넣지 않는 이유는 설계 2-6 참조(틱마다 List 복사 회피).
    val streamingText: String = "",
)

enum class ChatPhase {
    IDLE,
    THINKING, // 전송했고 첫 delta 전
    TYPING, // delta 수신 중
}

sealed interface ChatSideEffect {
    data class ShowMessage(val message: String) : ChatSideEffect
}
```

> 네비게이션은 사이드이펙트로 내보내지 않는다. 헤더의 닫기/노트 아이콘은 `ChatScreen`이 받은 람다를 그대로 호출한다(G-chat). VM이 라우팅을 몰라도 되므로 `feature:chat`이 `core:navigation`에 의존하지 않는다.

- [ ] `feature/chat/src/main/java/com/kikidan/chat/ChatViewModel.kt` (신규)

```kotlin
package com.kikidan.chat

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.ChatStreamException
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import com.kikidan.domain.usecase.GetChatEntryUseCase
import com.kikidan.domain.usecase.GetConversationDetailUseCase
import com.kikidan.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.transform
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val getChatEntry: GetChatEntryUseCase,
        private val getConversationDetail: GetConversationDetailUseCase,
        private val sendChatMessage: SendChatMessageUseCase,
    ) : ViewModel(),
        ContainerHost<ChatState, ChatSideEffect> {
        override val container = container<ChatState, ChatSideEffect>(ChatState())

        /** 화면 진입 시 1회. conversationId가 있으면 과거 대화를 먼저 채운다. */
        fun load(conversationId: String?) =
            intent {
                reduce { state.copy(conversationId = conversationId, isLoading = true) }

                getChatEntry()
                    .onSuccess { entry ->
                        reduce {
                            state.copy(
                                greeting = entry.greeting,
                                suggestions = entry.suggestions,
                                quota = entry.quota,
                            )
                        }
                    }.onFailure { postSideEffect(ChatSideEffect.ShowMessage(it.toUserMessage())) }

                if (conversationId != null) {
                    getConversationDetail(conversationId)
                        .onSuccess { reduce { state.copy(messages = it.messages) } }
                        .onFailure { postSideEffect(ChatSideEffect.ShowMessage(it.toUserMessage())) }
                }

                reduce { state.copy(isLoading = false) }
            }

        fun onInputChange(value: String) =
            intent {
                reduce { state.copy(input = value.take(SendChatMessageUseCase.MAX_CONTENT_LENGTH)) }
            }

        fun onSendClick() =
            intent {
                val content = state.input
                reduce { state.copy(input = "") }
                send(content)
            }

        fun onSuggestionClick(seedPrompt: String) = intent { send(seedPrompt) }

        fun startNewConversation() =
            intent {
                reduce {
                    state.copy(
                        conversationId = null,
                        messages = emptyList(),
                        streamingText = "",
                        phase = ChatPhase.IDLE,
                        input = "",
                    )
                }
            }

        // 전송 진입점이 여러 개이므로 가드를 여기 한 곳에만 둔다 (설계 2-7).
        private suspend fun org.orbitmvi.orbit.syntax.simple.SimpleSyntax<ChatState, ChatSideEffect>.send(content: String) {
            if (state.phase != ChatPhase.IDLE) return
            if (state.quota?.let { it.remaining <= 0 } == true) {
                postSideEffect(ChatSideEffect.ShowMessage(QUOTA_EXHAUSTED_MESSAGE))
                return
            }

            val conversationId = state.conversationId
            reduce {
                state.copy(
                    messages = state.messages + localUserMessage(content),
                    phase = ChatPhase.THINKING,
                    streamingText = "",
                )
            }

            var streamConversationId: String? = conversationId
            var completed: ChatMessage? = null

            try {
                sendChatMessage(conversationId, content)
                    .transform { result ->
                        // 모든 실패를 예외 한 채널로 되돌린다 (설계 2-3).
                        when (val event = result.getOrElse { throw it }) {
                            is ChatStreamEvent.Start -> streamConversationId = event.conversationId
                            is ChatStreamEvent.Delta -> emit(event.content)
                            is ChatStreamEvent.Action -> Unit // Done.message.action으로도 오므로 이 범위에선 무시
                            is ChatStreamEvent.Done -> completed = event.message
                        }
                    }.typewriter()
                    .collect { shown -> reduce { state.copy(phase = ChatPhase.TYPING, streamingText = shown) } }

                // typewriter가 완료된 = 버퍼가 다 비워진 시점이라 화면과 어긋나지 않는다.
                reduce {
                    state.copy(
                        conversationId = streamConversationId,
                        messages = state.messages + (completed ?: localAssistantMessage(state.streamingText)),
                        streamingText = "",
                        phase = ChatPhase.IDLE,
                        // start 이벤트에 quota가 실려 오는지 미확정이라 낙관적으로 1 증가시킨다(4절 참조).
                        quota = state.quota?.let { it.copy(used = it.used + 1) },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                reduce { state.copy(streamingText = "", phase = ChatPhase.IDLE) }
                postSideEffect(ChatSideEffect.ShowMessage(e.toUserMessage()))
            }
        }

        private companion object {
            const val QUOTA_EXHAUSTED_MESSAGE = "오늘 무료 채팅을 모두 사용했어요."
            const val DEFAULT_ERROR_MESSAGE = "답변을 받지 못했어요. 잠시 후 다시 시도해 주세요."
        }
    }

private fun Throwable.toUserMessage(): String =
    (this as? ChatStreamException)?.message ?: "답변을 받지 못했어요. 잠시 후 다시 시도해 주세요."

private fun localUserMessage(content: String) =
    ChatMessage(
        id = "local-user-${System.currentTimeMillis()}",
        role = MessageRole.USER,
        content = content,
        status = MessageStatus.PENDING,
        action = null,
        createdAt = Instant.now(),
    )

// done 이벤트 없이 스트림이 정상 종료된 경우에도 그려진 답변을 잃지 않게 한다.
private fun localAssistantMessage(content: String) =
    ChatMessage(
        id = "local-assistant-${System.currentTimeMillis()}",
        role = MessageRole.ASSISTANT,
        content = content,
        status = MessageStatus.COMPLETED,
        action = null,
        createdAt = Instant.now(),
    )
```

> 구현 시 주의: `SimpleSyntax` 수신자를 갖는 `private suspend fun send(...)`의 정확한 타입 표기는 Orbit 11의 실제 시그니처(`SimpleSyntax` vs `IntentContext`)를 IDE로 확인해 맞춘다. 확인이 번거로우면 `send`를 별도 `intent { }`로 분리하고 `onSendClick`/`onSuggestionClick`이 그것을 호출하는 형태로 바꿔도 동작은 같다.

- [ ] `feature/chat/src/test/java/com/kikidan/chat/FakeChatRepository.kt` (신규)
  — `sendMessage` 호출 인자(`conversationId`/`content`)와 호출 횟수를 기록하고, 주입된 `List<Result<ChatStreamEvent>>`를 방출하는 Fake. 나머지 4개 함수는 주입된 `Result`를 그대로 반환.
  `core:domain`의 테스트 Fake는 다른 모듈의 test 소스셋이라 재사용할 수 없어 새로 작성한다(`java-test-fixtures` 도입은 이 범위 밖 — 4절).
  테스트는 이 Fake로 **실제 UseCase 인스턴스**를 만들어 VM에 주입한다(UseCase는 인터페이스가 아니므로 Fake 대상이 Repository여야 한다).

- [ ] `feature/chat/src/test/java/com/kikidan/chat/TypewriterFlowTest.kt` (신규) — 2-9의 7케이스
- [ ] `feature/chat/src/test/java/com/kikidan/chat/ChatViewModelTest.kt` (신규) — 2-9의 12케이스

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [ ] **[높음] C2 문서의 "delta가 증분인가 누적인가"가 이 설계의 전제다** — 본 설계는 **증분(append)** 을 가정한다. 서버가 매번 누적 전체 텍스트를 보내면 `typewriter()`가 텍스트를 이중으로 이어붙여 답변이 뭉개진다. 백엔드 확인 전까지 **F-chat 구현을 시작하지 말 것**을 권한다. 누적으로 판명되면 `transform`에서 `emit(event.content)` → `emit(event.content.removePrefix(누적본))` 한 줄 수정으로 흡수 가능하다.
- [ ] **[높음] `ChatStreamEvent.Start`의 conversationId 유실은 조용히 실패한다** — D 문서가 지적한 실패 모드가 그대로 상속된다. `ChatViewModelTest`의 "두 번째 send에 같은 id 전달" 케이스로 클라이언트 로직은 고정되지만, 서버가 id를 안 주면 매번 새 대화가 만들어진다. **dev 서버 스모크 테스트 필수.**
- [ ] **[중간] quota 낙관적 증가** — Done 이후 `used + 1`로 갱신한다. start 이벤트에 quota가 실려 오면(A 문서 리스크) 그 값을 쓰는 편이 정확하다. 그전까지 헤더 숫자가 서버와 어긋날 수 있다(다음 화면 진입 시 entry 재조회로 교정됨).
- [ ] **[중간] `TYPING_TICK_MS` / `CATCH_UP_DIVISOR` 실기기 튜닝 필요** — 서버 청크가 이미 크고 드문드문 오면 catch-up이 자주 발동해 "한 글자씩"이 아니라 "덩어리씩"으로 보일 수 있다. 실제 청크 크기를 측정한 뒤 두 상수를 조정한다. 조정만으로 부족하면 `CATCH_UP_DIVISOR`를 늘리고 `TYPING_TICK_MS`를 8ms로 낮춘다.
- [ ] **[중간] `orbit-test`와 `delay`의 상호작용** — `typewriter()`의 `delay`가 orbit-test의 `TestScope` 가상 시간에서 스킵된다는 전제다. 실제로 실시간 대기가 발생하면 `ChatViewModel` 생성자에 `typingTickMillis: Long = TYPING_TICK_MS`를 추가해 테스트에서 0으로 주입한다(폴백 준비됨).
- [ ] **[중간] `rules/30-presentation.md`의 "`by(Delegate)` 형태 권장" 문구 정정 필요** — 2-8 참조. Orbit 11 API로는 불가능한 표현이므로 규칙 문서를 팀이 정정할지 확인.
- [ ] **[낮음] `ChatStreamEvent.Action`을 무시한다** — 액션 카드 UI가 Figma에 확정되지 않았고 `Done.message.action`으로도 동일 정보가 오므로 State에는 이미 담긴다. 렌더링은 후속 이슈.
- [ ] **[낮음] `FakeChatRepository`가 `core:domain` 테스트와 중복** — `java-test-fixtures` 플러그인을 `core:domain`에 도입하면 공유할 수 있으나 다른 모듈의 gradle 설정을 건드리게 되어 이 단위 범위 밖으로 둔다. 세 번째 중복이 생기면 그때 도입한다.
- [ ] **[낮음] 진입 시 entry/detail 순차 호출** — 왕복 2회. 체감이 느리면 `coroutineScope { async }`로 병렬화(3줄).
- [ ] **[낮음] `AndroidManifest.xml`이 실제로 필요한지** — AGP 9 + namespace 지정 라이브러리 모듈은 빈 매니페스트 없이도 빌드될 수 있다. `feature:auth`가 갖고 있어 따랐다. 없어도 빌드되면 파일을 지운다.
