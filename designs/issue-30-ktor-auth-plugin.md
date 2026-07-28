# 설계 문서 — #30 [Feat] Ktor Auth 플러그인 설정

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 전 **사람이 검토·수정하는 게이트**다.
> 자유롭게 수정해도 되고, 수정 후 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: [#30 [Feat] Ktor Auth 플러그인 설정](https://github.com/YAPP-Github/28th-App-Team-2-Android/issues/30) (사실상 #31 Ktor/network 기본 세팅도 함께 해소)
- **작성**: 설계 에이전트 (Opus) / 2026-07-28
- **상태**: `승인됨(구현 가능)` — 사용자 승인 2026-07-29, R-1/R-7 실측 확인 완료(8절 참조)
- **브랜치 / 워크트리**: `feat/30-ktor-auth` @ `.claude/worktrees/feat-30-ktor-auth` (base: `origin/develop` 7cd5744)
- **관련 모듈**: `core:domain`, `core:data`, `core:data-remote`, `core:data-local`, `app`, `gradle/libs.versions.toml`

---

## 1. 문제 정의 / 목표

토닥운 앱은 서버 API 전체가 JWT Bearer 인증을 요구한다. 현재 저장소에는 Ktor 코드가 **한 줄도 없고**(`core:data-remote`, `core:data-local`, `core:data` 모두 `ExampleUnitTest`만 존재), 네트워크 permission조차 선언돼 있지 않다. 따라서 이 이슈는 "Auth 플러그인 하나 붙이기"가 아니라 **네트워크 레이어의 바닥을 까는 작업**이다.

목표는 세 가지다.
1. `core:data-remote`에 앱 전역에서 재사용할 **Ktor HttpClient 기본 세팅**(엔진/직렬화/로깅/타임아웃/BaseURL)을 구축한다.
2. Ktor `Auth` 플러그인 `bearer {}`를 붙여 **access token 자동 부착 → 401 감지 → refresh → 원 요청 재시도**를 클라이언트 레벨에서 투명하게 처리한다.
3. `core:data-local`에 **DataStore 기반 토큰 영속화**를 두고, 위 플러그인이 이를 읽고 쓴다.

### 완료 기준 (Acceptance Criteria)

- [ ] **AC-1** `@AuthenticatedClient HttpClient`가 Hilt `@Singleton`으로 주입 가능하고, ContentNegotiation(kotlinx.serialization) / Logging / HttpTimeout / DefaultRequest(BaseURL) / `expectSuccess = true`가 설치돼 있다.
- [ ] **AC-2** 저장된 access token이 있으면 요청에 `Authorization: Bearer <token>` 헤더가 자동으로 붙는다. 저장된 토큰이 없으면 헤더 없이 나간다.
- [ ] **AC-3** 401 응답을 받으면 `POST /api/v1/auth/refresh`가 호출되고, 새 access token으로 **원 요청이 자동 재시도**되어 호출부는 재시도를 인지하지 않는다.
- [ ] **AC-4** refresh 응답의 **새 refresh token이 반드시 저장**된다(서버가 rotation으로 구 토큰을 즉시 폐기하므로, 저장 실패 = 계정 로그아웃).
- [ ] **AC-5** refresh 실패(만료/폐기) 시 저장된 토큰이 **모두 삭제**되고, `TokenRepository.observeLoginState()`가 `false`를 방출하며, 원 요청 에러가 호출부로 전파된다.
- [ ] **AC-6** 동시에 N개 요청이 401을 받아도 refresh 요청은 **정확히 1회만** 나간다.
- [ ] **AC-7** release 빌드에서 토큰이 로그에 남지 않는다(Logging 비활성 + `Authorization` 헤더 sanitize).
- [ ] **AC-8** `./gradlew :core:data-remote:test :core:data-local:test :core:data:test` 통과, `./gradlew ktlintCheck` 통과, `./gradlew assembleDebug` 성공.
- [ ] **AC-9** `.claude/rules/00-architecture.md` 의존성 방향 위반(P1) 없음 — 특히 `core:data-remote → core:data-local` 직접 참조가 없다.

---

## 2. 범위

### 포함 (In scope)

| # | 항목 | 모듈 |
|---|------|------|
| 1 | Version catalog에 `ktor-client-auth`, `ktor-client-mock`, `kotlinx-coroutines-test` 추가 | `gradle/` |
| 2 | `AuthToken` 도메인 모델 + `TokenRepository` 인터페이스 | `core:domain` |
| 3 | `TokenDataSource` / `AuthRemoteDataSource` / `AuthTokenCacheInvalidator` **인터페이스**, `TokenRepositoryImpl` | `core:data` |
| 4 | DataStore(Preferences) 기반 `TokenLocalDataSource` + DI | `core:data-local` |
| 5 | Ktor HttpClient 2종(인증용/refresh 전용) + Auth 플러그인 설정 + DI | `core:data-remote` |
| 6 | `CommonResponse<T>` 제네릭 래퍼 DTO, `RefreshRequest`/`RefreshResponse`, `AuthRemoteDataSourceImpl`(refresh 엔드포인트 1개만) | `core:data-remote` |
| 7 | `INTERNET` permission 선언, 백업 제외 규칙 | `core:data-remote`, `app` |
| 8 | MockEngine 기반 유닛 테스트 | 3개 data 모듈 |

### 제외 (Non-goals) — 후속 이슈

| 항목 | 사유 / 후속 |
|------|-------------|
| `POST /auth/login`, `/auth/signup`, `/auth/logout` API 호출 | 로그인 feature 이슈. 단 refresh는 Auth 플러그인의 필수 구성요소라 포함 |
| `AuthRepository`(소셜 로그인 → 서버 토큰 교환) | 로그인 feature 이슈. #32(OAuth SDK)와 합류하는 지점 |
| `onboardingToken` / `newMember` 분기 처리 | 온보딩 feature 이슈 |
| 강제 로그아웃 시 **네비게이션 전이**(로그인 화면으로 이동) | `app` 레벨 이슈. 이번엔 `observeLoginState()` Flow를 **노출**하는 데까지만 |
| HTTP 에러 → 도메인 예외 매핑 체계(`CommonResponse.code` 기반) | 별도 이슈 권장. 이번엔 Ktor 기본 예외(`ClientRequestException` 등)를 `Result.failure`로 감싸는 수준 |
| 토큰 저장 암호화(Keystore AEAD) | 판단 포인트 7 참조. 권장안은 "지금은 평문 + 백업 제외", 필요 시 후속 이슈 |
| Retry/재시도 정책, 오프라인 캐시, Paging | 필요 시점에 별도 이슈 |

### #31과의 관계

이슈 #31(Ktor 및 network 레이어 기본 세팅)은 이 설계의 **포함 항목 1·5·6·7**과 완전히 겹친다. Auth 플러그인은 HttpClient·ContentNegotiation·Serialization 위에서만 성립하므로 둘을 분리하면 반쪽짜리 PR이 두 개 나온다.
→ **#30 PR에서 `close #30`, `close #31`을 함께 건다.** 문서·PR 본문에 이 사실을 명시한다.

---

## 3. 참고 스펙

### Swagger — Base URL `https://api-dev.todakun.com`, 인증 스킴 HTTP bearer(JWT)

| 메서드 + 경로 | 이번 이슈 | 요청 | 응답 |
|---|---|---|---|
| `POST /api/v1/auth/refresh` | **구현** | `RefreshRequest { refreshToken: String }` | `CommonResponse<RefreshResponse>`, `RefreshResponse { accessToken, refreshToken }` |
| `POST /api/v1/auth/login` | 제외(경로 상수만) | `LoginRequest { provider: "KAKAO"\|"GOOGLE", oauthAccessToken }` | `CommonResponse<LoginResponse>` |
| `POST /api/v1/auth/signup` | 제외(경로 상수만) | onboardingToken + 프로필 | `CommonResponse<...>` |
| `POST /api/v1/auth/logout` | 제외 | — | `CommonResponse<...>` |

공통 응답 래퍼:
```
CommonResponse<T> { success: Boolean, code: String, message: String, data: T,
                    reason: Map<String,String>, timestamp: String(date-time) }
```

> ⚠️ **refresh token rotation**: `/auth/refresh` 성공 시 서버가 기존 refresh token을 **즉시 폐기**한다. 새 refresh token을 저장하지 못하면 그 계정은 다음 refresh에서 영구 실패한다. → 설계상 "새 토큰 저장"을 refresh 성공 처리의 **선행 단계**로 둔다(4-3 참조).

### Figma
해당 없음(UI 없는 인프라 이슈).

### 검증된 라이브러리 사실 (Ktor 3.1.3, gradle 캐시에서 직접 확인)

```kotlin
class BearerTokens(val accessToken: String, val refreshToken: String?)   // refreshToken 은 nullable
BearerAuthConfig.loadTokens:       suspend () -> BearerTokens?           // 반환 nullable
BearerAuthConfig.refreshTokens:    suspend RefreshTokensParams.() -> BearerTokens?  // null 반환 = 재시도 포기
BearerAuthConfig.sendWithoutRequest: (HttpRequestBuilder) -> Boolean
class RefreshTokensParams(val client: HttpClient, val response: HttpResponse, val oldTokens: BearerTokens?) {
    fun HttpRequestBuilder.markAsRefreshTokenRequest()
}
class BearerAuthProvider { fun clearToken() }                            // public, non-suspend
fun <T : AuthProvider> HttpClient.authProvider(): T?
class AuthTokenHolder<T>(private val loadTokens: ...) { private val mutex: Mutex }  // ★ 내부 Mutex 존재
```
- `ktor-client-auth-jvm:3.1.3`, `ktor-client-mock-jvm:3.1.3` 모두 로컬 gradle 캐시에 이미 존재 → 오프라인 해석 가능.
- `AuthTokenHolder`가 `Mutex`를 들고 있다는 사실이 판단 포인트 3(동시성)의 근거다.

---

## 4. 설계 및 실행 계획

### 4-0. 출발점: 기존 모듈 의존성 그래프를 먼저 읽었다

`build.gradle.kts`들을 직접 확인한 결과, 이 저장소의 data 계열 의존성 화살표는 **일반적인 clean architecture 예제와 방향이 반대**다.

```
core:domain  (java-library, JVM only)
   ▲
   │
core:data ──────────────┐
   ▲          ▲         │
   │          │         └─ (domain 만 의존)
core:data-remote   core:data-local
   │                    │
   └────── app ─────────┘
```
- `core/data-remote/build.gradle.kts` → `projects.core.domain`, **`projects.core.data`**
- `core/data-local/build.gradle.kts` → **`projects.core.data`**
- `core/data/build.gradle.kts` → `projects.core.domain`

즉 `core:data`는 remote/local을 **참조할 수 없다**(순환). 그러면 `core:data`의 `RepositoryImpl`이 DataSource를 어떻게 쓰나? 답은 하나뿐이다.

> **`core:data` = 계약(인터페이스) + RepositoryImpl 계층, `core:data-remote`/`core:data-local` = 구현체 계층.**
> DataSource **인터페이스**를 `core:data`에 두고, Ktor/DataStore **구현체**를 각 모듈에 두고, Hilt `@Binds`로 연결한다.

이 해석이 기존 gradle 화살표와 100% 일치하며, **이번 이슈가 이 프로젝트의 첫 data 레이어 코드**이므로 여기서 정한 패턴이 이후 모든 feature의 템플릿이 된다. 설계의 대부분이 이 한 가지 관찰에서 파생된다.

---

### 4-1. 판단 포인트 ① 모듈 배치 — 토큰 저장소 인터페이스를 어디에 둘 것인가

**결정: `TokenDataSource` 인터페이스를 `core:data`에 둔다. 구현체 `TokenLocalDataSource`는 `core:data-local`. `core:data-remote`는 인터페이스만 주입받는다.**

| 대안 | 판정 | 근거 |
|---|---|---|
| (A) `core:data-remote` → `core:data-local` 직접 의존 | ❌ | data 계열 형제 모듈 간 구현 의존. 지금은 컴파일되지만 data-local이 언젠가 remote를 필요로 하면 즉시 순환. `.claude/rules/00-architecture.md`의 "역방향/횡단 참조 금지" 정신 위반이며 리뷰에서 P1 소지 |
| (B) `core:domain`에 `TokenRepository`만 두고 data-remote가 그걸 주입 | ❌ | Auth 플러그인은 `Result<T>`가 아니라 raw suspend read가 필요하다. Repository 규칙(`Result<T>` 반환 강제, `.claude/rules/10-domain.md`)과 플러그인 코드가 서로 어색해지고, `loadTokens`마다 `Result` 언랩 보일러플레이트가 생긴다 |
| **(C) `core:data`에 `TokenDataSource` 인터페이스** | ✅ **채택** | 기존 gradle 화살표(remote→data, local→data)와 정확히 일치. data-remote는 `core:data`만 보면 되고 DataStore의 존재를 모른다. 테스트에서 fake로 갈아끼우기 자명 |

같은 논리로 `AuthRemoteDataSource`, `AuthTokenCacheInvalidator` 인터페이스도 `core:data`에 둔다. `AuthToken` 모델만 `core:domain`에 둔다(양쪽 모두 domain을 보므로 접근 가능하고, `.claude/rules/20-data.md`의 "DataSource는 Domain Model을 반환한다"를 만족).

**시그니처**

```kotlin
// core:domain — com.kikidan.domain.model.auth.AuthToken
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
)

// core:domain — com.kikidan.domain.repository.TokenRepository
interface TokenRepository {
    fun observeLoginState(): Flow<Result<Boolean>>       // rules/10-domain: Flow<Result<T>>
    suspend fun getToken(): Result<AuthToken?>
    suspend fun saveToken(token: AuthToken): Result<Unit>
    suspend fun clearToken(): Result<Unit>
}

// core:data — com.kikidan.data.datasource.TokenDataSource
//   rules/20-data: DataSource 는 try-catch 하지 않고 그대로 throw 한다
interface TokenDataSource {
    fun observeToken(): Flow<AuthToken?>
    suspend fun getToken(): AuthToken?
    suspend fun saveToken(token: AuthToken)
    suspend fun clearToken()
}

// core:data — com.kikidan.data.datasource.AuthRemoteDataSource
//   rules/20-data: REST 엔드포인트와 1:1, 함수 접두사는 HTTP METHOD 연관 동사
interface AuthRemoteDataSource {
    suspend fun postRefresh(refreshToken: String): AuthToken
}

// core:data — com.kikidan.data.auth.AuthTokenCacheInvalidator
//   Ktor Auth 플러그인의 인메모리 토큰 캐시를 무효화하는 훅 (4-2 하단 참조)
interface AuthTokenCacheInvalidator {
    fun invalidate()
}
```

> **`AuthTokenCacheInvalidator`가 왜 필요한가 (놓치면 로그인 직후 전부 401 나는 버그)**
> Ktor의 `AuthTokenHolder`는 `loadTokens`를 **최초 1회만 호출하고 결과를 인메모리 캐시**한다. 앱이 로그아웃 상태로 콜드 스타트하면 `loadTokens`가 `null`을 캐시하고, 이후 로그인해서 DataStore에 토큰을 써도 캐시된 `null`이 유지된다 → 모든 요청이 헤더 없이 나가고 401 → `refreshTokens`의 `oldTokens`도 null이라 복구 불가.
> 해결: `TokenRepositoryImpl.saveToken()/clearToken()` 직후 `invalidate()`를 호출해 `BearerAuthProvider.clearToken()`으로 캐시를 비운다. Ktor 3.1.3에 `cacheTokens = false` 옵션은 **없다**(`BearerAuthConfig` javap로 확인). 이 훅이 유일한 정공법이다.
> 순환 없음: HttpClient는 `TokenDataSource`만 필요하고 `TokenRepository`를 모른다. 다만 초기화 순서 안전을 위해 invalidator는 `dagger.Lazy<HttpClient>`로 주입한다.

---

### 4-2. 판단 포인트 ② refreshTokens 순환 의존 — refresh는 어느 클라이언트로 부르는가

**결정: Auth 플러그인이 없는 별도의 plain `@TokenRefreshClient HttpClient`를 만들고, 그 위에 `AuthRemoteDataSourceImpl`을 얹는다. 엔진(`HttpClientEngine`)은 두 클라이언트가 공유한다.**

| 대안 | 판정 | 근거 |
|---|---|---|
| (A) `RefreshTokensParams.client` + `markAsRefreshTokenRequest()` | ❌ | Ktor 공식 권장이고 재귀는 막힌다. 그러나 **refresh HTTP 호출 코드가 Hilt DI 모듈 람다 안에 인라인으로 박힌다.** `.claude/rules/20-data.md`("Remote DataSource 함수는 REST 엔드포인트와 1:1") 위반이고, refresh 호출만 따로 유닛 테스트할 수 없다 |
| **(B) 별도 plain HttpClient + `AuthRemoteDataSource`** | ✅ **채택** | ① refresh가 정식 DataSource가 되어 규칙 준수 + 독립 테스트 가능 ② 재귀가 "플래그로 막히는 것"이 아니라 **구조적으로 불가능**(그 클라이언트엔 Auth 플러그인 자체가 없음) ③ DI 순환 없음: `TokenRefreshClient`는 `AuthRemoteDataSource`를 모른다 |

(B)의 유일한 비용은 클라이언트가 2개라는 것이다. → **`HttpClientEngine`을 `@Singleton`으로 1개만 provide 하고 두 클라이언트에 넘긴다.** `HttpClient(engine)` 형태(팩토리가 아닌 인스턴스)로 생성하면 클라이언트가 엔진을 소유하지 않아 커넥션 풀이 1개로 유지되고, 한쪽 close가 엔진을 닫지 않는다. 두 클라이언트 모두 `@Singleton`이라 실제 close도 발생하지 않는다.

**Hilt 그래프**

```
@Qualifier annotation class AuthenticatedClient   // 앱 전역에서 쓰는 인증 클라이언트
@Qualifier annotation class TokenRefreshClient    // refresh 전용 plain 클라이언트

@Singleton HttpClientEngine  (OkHttp.create { ... })
        │
        ├─→ @TokenRefreshClient HttpClient   (Auth 플러그인 없음)
        │            │
        │            └─→ AuthRemoteDataSourceImpl  ──@Binds──▶ AuthRemoteDataSource
        │                                                            │
        └─→ @AuthenticatedClient HttpClient ◀───────────────────────┘
                     ▲          (loadTokens/refreshTokens 가 TokenDataSource + AuthRemoteDataSource 사용)
                     │
             Lazy<HttpClient>
                     │
        BearerTokenCacheInvalidator ──@Binds──▶ AuthTokenCacheInvalidator
                                                        ▲
                                              TokenRepositoryImpl (core:data)

TokenLocalDataSource(core:data-local) ──@Binds──▶ TokenDataSource
```
순환 없음. `app`이 4개 모듈을 모두 `implementation` 하므로(`app/build.gradle.kts` 확인 완료) 바인딩이 한 그래프로 합쳐진다.

---

### 4-3. 판단 포인트 ③ 동시성 — 401 폭주 시 refresh 중복 호출

**결정: 별도 `Mutex`를 두지 않는다. `@Singleton` HttpClient 1개 인스턴스 유지로 충분하다.**

근거(추측 아님, jar 디컴파일로 확인):
```
io.ktor.client.plugins.auth.providers.AuthTokenHolder<T>
  private final kotlinx.coroutines.sync.Mutex mutex;
  private volatile T value;
  public setToken$ktor_client_auth(...)   // mutex 로 직렬화 + 이미 갱신됐으면 재실행 안 함
```
`BearerAuthProvider`는 이 `AuthTokenHolder`를 통해서만 토큰을 갱신하고, 홀더가 `Mutex`로 직렬화하며 "먼저 들어온 코루틴이 이미 갱신했으면 뒤따라온 코루틴은 갱신을 건너뛰고 새 값을 받는다". 즉 **동일 클라이언트 인스턴스 안에서 refresh 중복 제거는 Ktor가 보장한다.** 추가 Mutex는 중복 방어일 뿐이고 오히려 데드락 표면만 넓힌다.

따라서 우리가 지켜야 할 **불변식은 단 하나: `@AuthenticatedClient HttpClient`는 반드시 `@Singleton`이다.** 매 호출마다 `HttpClient {}`를 만드는 코드가 들어오면 이 보장이 통째로 깨진다. → 테스트 T6(동시 401 → refresh 정확히 1회)로 회귀 방어하고, `NetworkModule`에 주석으로 명시한다.

DataStore 쓰기 측도 동일 파일에 대한 `edit {}`가 내부적으로 직렬화되므로 별도 잠금이 필요 없다.

---

### 4-4. 판단 포인트 ④ refresh 실패 시나리오 — 어디까지 이번 이슈인가

**결정: `토큰 클리어 → DataStore가 빈 값 방출 → TokenRepository.observeLoginState()가 false 방출`까지 구현한다. 이벤트 버스도, 네비게이션 전이도 만들지 않는다.**

```
refreshTokens 블록
  ├─ oldTokens?.refreshToken == null  ──▶ clearToken() ; return null
  └─ runCatching { authRemoteDataSource.postRefresh(old) }
        ├─ 성공 ─▶ tokenDataSource.saveToken(new)   ← ★ 저장 먼저 (rotation, AC-4)
        │          invalidator 불필요(플러그인이 반환값을 자기 캐시에 직접 반영)
        │          return BearerTokens(new.access, new.refresh)
        └─ 실패 ─▶ tokenDataSource.clearToken() ; return null
                     └─ Ktor: null 이면 재시도 포기 → 원래 401 이 호출부로 전파
                        (expectSuccess=true 이므로 ClientRequestException)
                     └─ DataStore 가 빈 Preferences 방출
                        → TokenDataSource.observeToken() = null
                        → TokenRepository.observeLoginState() = Result.success(false)
```

왜 이벤트 버스를 안 만드나: 강제 로그아웃 신호로 필요한 정보는 "토큰이 없어졌다" 하나뿐이고, 그건 이미 DataStore Flow가 **자연스럽게 방출하는 상태**다. `SharedFlow<AuthEvent>` 같은 별도 채널을 만들면 진실의 원천이 둘로 갈라진다(토큰은 지웠는데 이벤트 방출을 깜빡하는 버그 클래스).
한계는 명시해 둔다: 이 방식으로는 **"세션 만료로 튕김"과 "사용자가 직접 로그아웃"을 구분할 수 없다.** 로그인 화면에서 "세션이 만료되었습니다" 토스트를 띄우려면 그때 이벤트 채널을 추가한다 → **후속 이슈**.

`app`에서 `observeLoginState()`를 구독해 로그인 화면으로 보내는 것은 네비게이션 그래프가 세워진 뒤의 일이므로 **범위 밖**이다.

---

### 4-5. 판단 포인트 ⑤ BASE_URL 주입

**결정: `core:data-remote` 모듈의 `buildConfigField`로 주입한다. `local.properties`도, `app` 경유도 쓰지 않는다.**

| 대안 | 판정 | 근거 |
|---|---|---|
| `local.properties` + buildConfigField (main 저장소 `core:auth`가 쓰는 방식) | ❌ | 그 방식은 **시크릿(카카오 네이티브 키, 구글 클라이언트 ID)**이라 정당했다. BASE_URL은 Swagger 문서에 공개된 값이다. 신규 팀원이 `local.properties`를 안 만들면 빈 문자열 BASE_URL로 조용히 깨진다 — 이득 없는 마찰 |
| `app`의 BuildConfig를 data-remote에 넘김 | ❌ | 역방향 의존. `app`이 네트워크 세부를 알게 됨 |
| **`core:data-remote`의 buildConfigField + buildType 분기** | ✅ **채택** | 라이브러리 모듈도 buildType별 variant를 가지므로 app의 debug/release가 자동 선택. 네트워크 지식이 네트워크 모듈에 갇힌다 |

```kotlin
// core/data-remote/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"https://api-dev.todakun.com/\"")
    }
    buildTypes {
        release {
            // TODO(#후속): prod 서버 개설 시 교체
            buildConfigField("String", "BASE_URL", "\"https://api-dev.todakun.com/\"")
        }
    }
    buildFeatures { buildConfig = true }
}
```

> ⚠️ **트레일링 슬래시 규약**: `BASE_URL`은 반드시 `/`로 끝내고, 엔드포인트 상수는 **선행 슬래시 없이** 쓴다(`"api/v1/auth/refresh"`). Ktor `DefaultRequest`의 URL 병합은 선행 슬래시 유무에 따라 base path를 날려먹는 고전적 함정이 있다. 상수를 한 파일(`AuthApi.kt`)에 모아 규약을 강제한다.

---

### 4-6. 판단 포인트 ⑥ Logging — release에서 토큰 유출 차단

**결정: `LogLevel`을 `BuildConfig.DEBUG`로 게이팅하고, debug에서도 `Authorization` 헤더는 sanitize 한다.**

```kotlin
install(Logging) {
    logger = Logger.ANDROID
    level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
    sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
}
```
- `LogLevel.NONE`(release)이 1차 방어선이다. sanitize만으로는 부족하다 — release에서 로깅 자체를 끄지 않으면 `/auth/refresh` **응답 바디**에 access/refresh token이 그대로 찍힌다. 헤더 sanitize는 바디를 건드리지 않는다.
- debug에서 `LogLevel.ALL`을 유지하는 것은 **의도적 트레이드오프**다: 개발 서버 토큰만 노출되고, 리버스 엔지니어링 대상은 release APK이며, 디버깅 생산성이 실제로 크다. 대신 `Authorization` 헤더는 매 요청마다 반복 노출되므로 sanitize로 로그 노이즈와 어깨너머 유출을 줄인다.
- `BuildConfig`는 `com.kikidan.data_remote.BuildConfig`(모듈 자신의 것)를 참조한다. 4-5에서 `buildConfig = true`를 켜는 것이 이 조건의 전제다.
- 부수 효과: `proguard-rules.pro`가 아직 비어 있고 `app`의 release는 `isMinifyEnabled = false`이므로 난독화 관련 추가 작업은 이번 범위 밖이다.

---

### 4-7. 판단 포인트 ⑦ DataStore 토큰 저장 보안 수준

**결정: 평문 Preferences DataStore를 쓴다. 대신 `allowBackup` 경로에서 토큰 파일을 제외하는 방어를 반드시 추가한다.**

근거:
1. **`androidx.security:security-crypto`(EncryptedSharedPreferences)는 deprecated다.** 여기에 새로 의존성을 다는 것은 이미 사라지는 길로 걸어 들어가는 선택이다. DataStore용 공식 암호화 대응물도 없다.
2. 남는 정공법은 **Android Keystore(AES-GCM) + 커스텀 `Serializer<T>`**인데, 비용이 코드 100~200줄에 그치지 않는다. 잠금화면/생체 변경 시 키가 무효화되어 복호화가 터지고, 그 예외를 "조용한 강제 로그아웃"으로 처리하는 경로를 또 만들어야 한다. 8주 부트캠프 일정에서 이 엣지케이스를 감당할 이유가 약하다.
3. 실제 위협 모델: 비루팅 단말에서 앱 샌드박스(`/data/data/<pkg>/files/datastore/`)는 타 앱이 읽을 수 없다. 루팅 단말이면 Keystore를 써도 앱 프로세스 메모리에서 복호화된 토큰을 뜯을 수 있어 방어 이득이 제한적이다.
4. 서버가 **refresh token rotation**을 하므로 탈취된 refresh token의 수명이 짧다.
5. 실제로 남는 현실적 구멍은 **`android:allowBackup="true"`**(현 매니페스트 그대로)로 토큰이 클라우드 백업/기기 이전에 실려 나가는 것이다. 이건 XML 몇 줄로 정확히 막을 수 있다.

**따라서 이번에 하는 것:**
```xml
<!-- app/src/main/res/xml/backup_rules.xml -->
<full-backup-content>
    <exclude domain="file" path="datastore/auth_token.preferences_pb"/>
</full-backup-content>

<!-- app/src/main/res/xml/data_extraction_rules.xml -->
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="file" path="datastore/auth_token.preferences_pb"/>
    </cloud-backup>
    <device-transfer>
        <exclude domain="file" path="datastore/auth_token.preferences_pb"/>
    </device-transfer>
</data-extraction-rules>
```
(두 파일 모두 현재 주석만 있는 템플릿 상태임을 확인했다. DataStore 이름을 `auth_token`으로 고정해야 경로가 맞는다.)

**기록해 두는 대안**: 보안 리뷰에서 요구가 나오면 `Serializer<TokenPreferences>` + Keystore AES-GCM으로 교체한다. `TokenDataSource` 인터페이스가 경계를 이미 만들어 두었으므로 **`core:data-local` 내부 교체만으로 끝나고 다른 모듈은 손대지 않는다.** 이게 4-1 결정이 주는 실질적 이득이다. → 후속 이슈 제안.

---

### 4-8. 그 밖의 기본 세팅 결정

| 항목 | 값 | 근거 |
|---|---|---|
| 엔진 | `OkHttp` | 카탈로그에 이미 있음. Android 실전 표준, 인터셉터/프록시 디버깅 자산 재사용 |
| `expectSuccess` | `true` (두 클라이언트 모두) | 401/5xx가 예외가 되어 `RepositoryImpl`의 `runCatching`이 잡는다(`.claude/rules/20-data.md`). Auth 플러그인은 `Send` 단계에서 401을 먼저 처리하므로, 재시도가 성공하면 검증기는 200만 본다 — 충돌 없음 |
| Json | `ignoreUnknownKeys=true`, `isLenient=true`, `explicitNulls=false`, `coerceInputValues=true` | 서버 필드 추가에 클라이언트가 죽지 않게. `CommonResponse.data`가 null로 오는 케이스 대응 |
| HttpTimeout | request 15s / connect 10s / socket 15s | 모바일 네트워크 기준 통상값 |
| `sendWithoutRequest` | **`api/v1/auth/refresh`, `api/v1/auth/login`, `api/v1/auth/signup`, `api/v1/terms` 4개 경로를 제외**한 전 요청에 선제 부착 (R-7 실측 확정) | 매 요청 401 왕복 1회를 없앤다. refresh는 어차피 별도 클라이언트라 이중 방어. ⚠️ `auth/logout`은 **인증이 필요하므로 제외 목록에 넣지 않는다** |
| `DefaultRequest` | `url(BuildConfig.BASE_URL)`, `contentType(Application.Json)` | 4-5 트레일링 슬래시 규약과 세트 |
| `INTERNET` permission | `core:data-remote`의 `AndroidManifest.xml`에 선언 | **현재 저장소 어디에도 없다(확인 완료).** 없으면 모든 요청이 런타임에 터진다. 네트워크를 쓰는 모듈이 스스로 선언하고 매니페스트 머저가 app으로 올린다 |

**테스트 가능성을 위한 구조 결정 (중요)**
클라이언트 설정을 Hilt `@Provides` 람다 안에 직접 쓰면 **유닛 테스트에서 재현할 수 없다.** 설정을 순수 확장 함수로 뽑아 DI 모듈과 테스트가 같은 코드를 쓰게 한다.

```kotlin
// core:data-remote — com.kikidan.data_remote.client.KtorClientConfig
internal fun HttpClientConfig<*>.installTodakunDefaults(json: Json, baseUrl: String)

internal fun HttpClientConfig<*>.installBearerAuth(
    tokenDataSource: TokenDataSource,
    authRemoteDataSource: AuthRemoteDataSource,
)
```
`NetworkModule`은 이 두 함수를 호출할 뿐이고, 테스트는 `HttpClient(MockEngine) { installTodakunDefaults(...); installBearerAuth(fakeToken, fakeAuth) }`로 **프로덕션과 동일한 설정**을 검증한다.

---

## 5. 데이터 흐름

### 5-1. 정상 요청 (토큰 유효)

```
호출부 → @AuthenticatedClient HttpClient
          → [Auth/bearer] sendWithoutRequest == true
              → AuthTokenHolder 캐시 hit? ─ no → loadTokens { tokenDataSource.getToken() }
                                                     → DataStore(auth_token.preferences_pb) 읽기
              → Authorization: Bearer <access>
          → [DefaultRequest] BASE_URL 병합, Content-Type: application/json
          → [Logging] debug 만, Authorization sanitize
          → [HttpTimeout] → OkHttp 엔진 → 서버
          ← 200 CommonResponse<T> → [ContentNegotiation] kotlinx.serialization 역직렬화
          ← DataSource 가 Domain Model 로 매핑 (rules/20-data)
          ← RepositoryImpl runCatching → Result<T>
          ← UseCase → ViewModel
```

### 5-2. 401 → refresh → 재시도 (핵심 경로)

```
요청 A ──────────────▶ 401
   │
   └─ [Auth] isUnauthorizedResponse → BearerAuthProvider.refreshToken()
          → AuthTokenHolder.setToken { ... }        ← Mutex 로 직렬화 (동시 401 이 여기서 1개로 접힘)
              → refreshTokens {
                    oldTokens?.refreshToken ?: return@refreshTokens null.also { clearToken() }
                    val new = authRemoteDataSource.postRefresh(old)      // @TokenRefreshClient (plain)
                         └─ POST api/v1/auth/refresh {refreshToken}
                            ← CommonResponse<RefreshResponse>{accessToken, refreshToken}
                            └─ RefreshResponse.toDomain() → AuthToken
                    tokenDataSource.saveToken(new)   // ★ rotation: 먼저 저장 (AC-4)
                    BearerTokens(new.accessToken, new.refreshToken)
                }
          → 새 Authorization 헤더로 요청 A 자동 재실행
요청 A ──────────────▶ 200   (호출부는 아무것도 몰랐다)

요청 B,C,D (동시 401) → 같은 Mutex 대기 → 갱신된 토큰을 그대로 받아 각자 재시도. refresh HTTP 는 1회.
```

### 5-3. refresh 실패 (세션 종료)

```
refreshTokens → postRefresh 실패(401/네트워크)
    → tokenDataSource.clearToken()      → DataStore edit { clear() }
    → return null                        → Ktor 재시도 포기
    → 원 401 이 expectSuccess 로 ClientRequestException 화
    → RepositoryImpl runCatching → Result.failure(...) → 호출부

동시에 (반응형 경로):
    DataStore Flow 방출 → TokenDataSource.observeToken() = null
                       → TokenRepository.observeLoginState() = Result.success(false)
                       → [범위 밖] app 이 구독해서 로그인 화면으로 전이
```

### 5-4. 로그인 성공 후 토큰 저장 (다음 이슈가 붙는 지점)

```
[다음 이슈] AuthRepository.login(...) 성공
    → TokenRepository.saveToken(AuthToken)
         → tokenDataSource.saveToken()          (DataStore 기록)
         → authTokenCacheInvalidator.invalidate()  ← ★ 이번 이슈가 미리 깔아두는 훅
              → BearerTokenCacheInvalidator: client.authProvider<BearerAuthProvider>()?.clearToken()
              → 다음 요청 때 loadTokens 가 재실행되어 새 토큰을 읽는다
```

---

## 6. 파일 변경 계획 (구현 체크리스트)

> 경로는 워크트리 루트(`.claude/worktrees/feat-30-ktor-auth/`) 기준. 🆕 = 신규 생성, ✏️ = 기존 수정.

### 6-1. 빌드 / 카탈로그

- [ ] ✏️ `gradle/libs.versions.toml` — `[libraries]`에 3개 추가
  - `ktor-client-auth = { group = "io.ktor", name = "ktor-client-auth", version.ref = "ktor" }`
  - `ktor-client-mock = { group = "io.ktor", name = "ktor-client-mock", version.ref = "ktor" }`
  - `kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }`
  - (`ktor-client-auth-jvm:3.1.3`, `ktor-client-mock-jvm:3.1.3`은 로컬 gradle 캐시에 이미 존재)
- [ ] ✏️ `core/domain/build.gradle.kts` — `api(libs.kotlinx.coroutines.core)` 추가 (`TokenRepository`가 `Flow` 노출. `java-library`이므로 `api`)
- [ ] ✏️ `core/data/build.gradle.kts` — `implementation(libs.kotlinx.coroutines.core)`, `testImplementation(libs.kotlinx.coroutines.test)`
- [ ] ✏️ `core/data-local/build.gradle.kts` — `implementation(projects.core.domain)` 추가(현재 `core:data`만 참조), `implementation(libs.kotlinx.coroutines.core)`, `testImplementation(libs.kotlinx.coroutines.test)`, `testOptions { unitTests.isReturnDefaultValues = true }`
- [ ] ✏️ `core/data-remote/build.gradle.kts` —
  - `implementation(libs.ktor.client.auth)`
  - `buildFeatures { buildConfig = true }`
  - `defaultConfig`/`buildTypes.release`에 `buildConfigField("String", "BASE_URL", ...)` (4-5)
  - `testImplementation(libs.ktor.client.mock)`, `testImplementation(libs.kotlinx.coroutines.test)`, `testImplementation(libs.kotlinx.serialization.json)`

### 6-2. `core:domain`

- [ ] 🆕 `core/domain/src/main/java/com/kikidan/domain/model/auth/AuthToken.kt` — `data class AuthToken(val accessToken: String, val refreshToken: String)` (전 필드 `val`, rules/10-domain)
- [ ] 🆕 `core/domain/src/main/java/com/kikidan/domain/repository/TokenRepository.kt` — `observeLoginState(): Flow<Result<Boolean>>`, `getToken/saveToken/clearToken` 모두 `Result` 반환
- [ ] ✏️ `core/domain/src/main/java/com/kikidan/domain/MyClass.kt` — 플레이스홀더 삭제 여부는 사람 판단(리스크 R-5)

### 6-3. `core:data` — 계약 + Repository 구현

- [ ] 🆕 `core/data/src/main/java/com/kikidan/data/datasource/TokenDataSource.kt` — 인터페이스(예외 그대로 throw)
- [ ] 🆕 `core/data/src/main/java/com/kikidan/data/datasource/AuthRemoteDataSource.kt` — `suspend fun postRefresh(refreshToken: String): AuthToken`
- [ ] 🆕 `core/data/src/main/java/com/kikidan/data/auth/AuthTokenCacheInvalidator.kt` — `fun invalidate()`
- [ ] 🆕 `core/data/src/main/java/com/kikidan/data/repository/TokenRepositoryImpl.kt` — 모든 메서드 `runCatching` + `Result`, `saveToken`/`clearToken` 후 `invalidator.invalidate()`
- [ ] 🆕 `core/data/src/main/java/com/kikidan/data/di/RepositoryModule.kt` — `@Module @InstallIn(SingletonComponent::class) @Binds TokenRepository`

### 6-4. `core:data-local` — DataStore 구현체

- [ ] 🆕 `core/data-local/src/main/java/com/kikidan/data_local/datastore/TokenPreferencesKeys.kt` — `ACCESS_TOKEN`, `REFRESH_TOKEN` `stringPreferencesKey`
- [ ] 🆕 `core/data-local/src/main/java/com/kikidan/data_local/datasource/TokenLocalDataSource.kt` — `TokenDataSource` 구현. `observeToken()`은 두 키가 모두 있을 때만 `AuthToken`, 아니면 `null`
- [ ] 🆕 `core/data-local/src/main/java/com/kikidan/data_local/di/DataStoreModule.kt` — `@Provides @Singleton DataStore<Preferences>`, **파일명 `auth_token`** (백업 제외 규칙과 반드시 일치, 4-7)
- [ ] 🆕 `core/data-local/src/main/java/com/kikidan/data_local/di/LocalDataSourceModule.kt` — `@Binds TokenDataSource ← TokenLocalDataSource`

### 6-5. `core:data-remote` — Ktor

- [ ] ✏️ `core/data-remote/src/main/AndroidManifest.xml` — `<uses-permission android:name="android.permission.INTERNET" />` **(현재 저장소 전체에 없음. 누락 시 런타임 전면 실패)**
- [ ] 🆕 `.../data_remote/api/AuthApi.kt` — 경로 상수(**선행 슬래시 없음**): `REFRESH = "api/v1/auth/refresh"`, `LOGIN`, `SIGNUP`, `LOGOUT`, `TERMS = "api/v1/terms"`. 비인증 경로 집합 `NO_AUTH_PATHS = setOf(REFRESH, LOGIN, SIGNUP, TERMS)`를 함께 노출해 `sendWithoutRequest`가 이 집합만 참조하게 한다(R-7)
- [ ] 🆕 `.../data_remote/dto/CommonResponse.kt` — `@Serializable data class CommonResponse<T>(success, code, message, data: T? = null, reason: Map<String,String>? = null, timestamp: String? = null)`
- [ ] 🆕 `.../data_remote/dto/auth/RefreshRequest.kt` — `@Serializable data class RefreshRequest(val refreshToken: String)`
- [ ] 🆕 `.../data_remote/dto/auth/RefreshResponse.kt` — `@Serializable data class RefreshResponse(val accessToken: String, val refreshToken: String)`
- [ ] 🆕 `.../data_remote/mapper/AuthMapper.kt` — `fun RefreshResponse.toDomain(): AuthToken`
- [ ] 🆕 `.../data_remote/datasource/AuthRemoteDataSourceImpl.kt` — `@TokenRefreshClient HttpClient` 주입, `postRefresh` 구현. **try-catch 금지, 그대로 throw**(rules/20-data)
- [ ] 🆕 `.../data_remote/client/TodakunJson.kt` — 공용 `Json` 인스턴스
- [ ] 🆕 `.../data_remote/client/KtorClientConfig.kt` — `installTodakunDefaults()`, `installBearerAuth()` (4-8, 테스트 공유용)
- [ ] 🆕 `.../data_remote/auth/BearerTokenCacheInvalidator.kt` — `Lazy<HttpClient>` 주입, `authProvider<BearerAuthProvider>()?.clearToken()`
- [ ] 🆕 `.../data_remote/di/NetworkQualifiers.kt` — `@AuthenticatedClient`, `@TokenRefreshClient`
- [ ] 🆕 `.../data_remote/di/NetworkModule.kt` — `Json`, `HttpClientEngine`(공유), 두 `HttpClient`(둘 다 `@Singleton`) provide. **"@Singleton 유지가 refresh 중복 방지의 전제"** 주석 필수(4-3)
- [ ] 🆕 `.../data_remote/di/RemoteDataSourceModule.kt` — `@Binds AuthRemoteDataSource`, `@Binds AuthTokenCacheInvalidator`

### 6-6. `app`

- [ ] ✏️ `app/src/main/res/xml/backup_rules.xml` — `<exclude domain="file" path="datastore/auth_token.preferences_pb"/>` (현재 주석만 있는 템플릿)
- [ ] ✏️ `app/src/main/res/xml/data_extraction_rules.xml` — `cloud-backup` + `device-transfer` 양쪽에 동일 exclude

### 6-7. 테스트 파일

- [ ] 🆕 `core/data-remote/src/test/java/com/kikidan/data_remote/client/BearerAuthIntegrationTest.kt` (T1~T6)
- [ ] 🆕 `core/data-remote/src/test/java/com/kikidan/data_remote/datasource/AuthRemoteDataSourceImplTest.kt` (T7~T8)
- [ ] 🆕 `core/data-remote/src/test/java/com/kikidan/data_remote/fake/FakeTokenDataSource.kt`
- [ ] 🆕 `core/data-local/src/test/java/com/kikidan/data_local/datasource/TokenLocalDataSourceTest.kt` (T9~T11)
- [ ] 🆕 `core/data/src/test/java/com/kikidan/data/repository/TokenRepositoryImplTest.kt` (T12~T14)
- [ ] ✏️ 각 모듈의 `ExampleUnitTest.kt` — 실제 테스트가 들어오면 삭제

---

## 7. 테스트 계획

### 7-1. `:core:data-remote` — MockEngine 기반 (핵심)

프로덕션과 **동일한** `installTodakunDefaults()` / `installBearerAuth()`를 호출해 클라이언트를 조립하고, 엔진만 `MockEngine`으로 바꾼다. Auth 클라이언트용 MockEngine과 refresh 클라이언트용 MockEngine을 각각 둔다.

| ID | 시나리오 | 검증 |
|---|---|---|
| **T1** | 저장된 토큰이 있을 때 요청 | 요청에 `Authorization: Bearer <access>` 존재 (**AC-2**) |
| **T2** | 저장된 토큰이 없을 때 요청 | `Authorization` 헤더 없음, refresh 호출 없음 |
| **T3** | 첫 응답 401 → 두 번째 200 | refresh MockEngine 1회 호출, **재시도 요청 헤더가 새 access token**, 최종 결과 200 (**AC-3**) |
| **T4** | T3와 동일 상황 | `FakeTokenDataSource.saved`가 새 **access AND refresh** 둘 다 보유 = rotation 저장 (**AC-4**) |
| **T5** | refresh가 401 반환 | `clearToken()` 호출됨 + 호출부에 `ClientRequestException` 전파 (**AC-5**) |
| **T6** | 5개 요청 동시 발사, 전부 첫 응답 401 | refresh MockEngine 핸들러 호출 횟수 `AtomicInteger == 1`, 5개 모두 최종 성공 (**AC-6**) |
| **T7** | `sendWithoutRequest` | `api/v1/auth/login` 요청에는 `Authorization` 미부착 |
| **T8** | `oldTokens == null` 상태에서 401 | refresh HTTP 호출 없이 즉시 실패 + 토큰 클리어(무한 루프 방지) |

`kotlinx-coroutines-test`의 `runTest` + `StandardTestDispatcher`를 쓴다. T6는 `async` 5개를 띄우고 `awaitAll`.

### 7-2. `:core:data-remote` — DataSource / DTO

| ID | 시나리오 | 검증 |
|---|---|---|
| **T9** | `postRefresh` 정상 | `POST api/v1/auth/refresh`, 바디 `{"refreshToken": ...}`, 반환이 `AuthToken` 도메인 모델 |
| **T10** | `CommonResponse`에 서버가 모르는 필드 추가 | `ignoreUnknownKeys`로 파싱 성공 |
| **T11** | 4xx 응답 | `expectSuccess=true`로 예외 throw, DataSource가 **삼키지 않음**(rules/20-data) |

### 7-3. `:core:data-local` — DataStore

**방식 결정: JVM 유닛 테스트 + `TemporaryFolder`에 실제 파일 DataStore를 만든다.** fake는 "정말 저장되는가"를 검증하지 못하고, `androidTest`는 CI 에뮬레이터 비용이 크다. `PreferenceDataStoreFactory.create { tmpFolder.newFile("test.preferences_pb") }`는 순수 JVM 아티팩트(`datastore-preferences-core`)로 동작한다.

| ID | 시나리오 | 검증 |
|---|---|---|
| **T12** | `saveToken` → `getToken` | 왕복 일치 |
| **T13** | `clearToken` 후 | `getToken() == null`, `observeToken()`이 `null` 방출 |
| **T14** | access만 있고 refresh 없는 손상 상태 | `observeToken()`이 `null` 반환(부분 토큰을 유효로 오인하지 않음) |

> 폴백: android.jar stub 관련 `RuntimeException: not mocked`가 나면 `testOptions { unitTests.isReturnDefaultValues = true }`를 켠다. 그래도 실패하면 T12~T14만 `androidTest`로 이관하고 문서에 기록한다.

### 7-4. `:core:data` — Repository

| ID | 시나리오 | 검증 |
|---|---|---|
| **T15** | DataSource가 `IOException` throw | `Result.isFailure`, 예외 누수 없음(rules/20-data) |
| **T16** | `saveToken` 성공 | `AuthTokenCacheInvalidator.invalidate()`가 호출됨 (4-1의 캐시 무효화 계약) |
| **T17** | `observeLoginState()` | 토큰 있음 → `Result.success(true)`, `clearToken` 후 → `Result.success(false)` |

### 7-5. 실행 커맨드

```bash
# 유닛 테스트 (변경 모듈만)
./gradlew :core:data-remote:test :core:data-local:test :core:data:test :core:domain:test

# 린트/포맷
./gradlew ktlintCheck        # 실패 시 ./gradlew ktlintFormat

# 컴파일 회귀 (Hilt 그래프 순환 여부는 여기서 드러난다)
./gradlew assembleDebug
```

> `assembleDebug`는 선택이 아니다. Hilt 순환 의존이나 `@Qualifier` 누락은 **KSP 코드 생성 시점**에만 잡히고 유닛 테스트로는 절대 안 잡힌다.

---

## 8. 리스크 / 미해결 질문 (사람 확인 필요)

| ID | 항목 | 영향 | 대응 / 질문 |
|---|---|---|---|
| **R-1** | ~~토큰 만료 시 서버가 정말 HTTP 401을 주는가?~~ | ~~치명~~ | ✅ **해소(2026-07-29, dev 서버 실측)**. `GET /api/v1/members/me` + 잘못된 Bearer 토큰 → **HTTP 401**, 바디 `{"success":false,"code":"AUTH-401","message":"유효하지 않은 액세스 토큰입니다.","timestamp":...}`. 실제 HTTP 상태 코드로 내려오므로 Ktor Auth 플러그인이 정상 트리거된다. `reAuthorizeOnResponse` 전환 불필요 — **설계 원안 그대로 진행** |
| **R-2** | refresh token 자체의 유효기간과, 만료 시 응답 코드 | 4-4 실패 경로 분기 정확도 | 백엔드 확인. 단 4-4는 "refresh 호출이 어떤 이유로든 실패하면 토큰 클리어"라 코드 분기와 무관하게 동작한다 — 구현 차단 요소 아님 |
| **R-3** | `core:domain`이 `java-library`(순수 JVM)라 Android 타입을 못 쓴다 | 설계 반영 완료 | `AuthToken`/`TokenRepository`는 Flow와 Kotlin 표준만 사용 — 문제 없음 |
| **R-4** | `app`의 release가 `isMinifyEnabled = false` | R8 적용 시 kotlinx.serialization/Ktor 규칙 필요 | 이번 범위 밖. minify를 켜는 이슈에서 `proguard-rules.pro` 정비 |
| **R-5** | `core/domain/.../MyClass.kt` 플레이스홀더 잔존 | 없음(청소 이슈) | 이번 PR에서 지울지 사람이 결정 |
| **R-6** | DataStore JVM 유닛 테스트 실행 가능 여부 | T12~T14 실행 방식 | 7-3 폴백 경로 준비됨. 구현 중 확인 |
| **R-7** | ~~`sendWithoutRequest` 제외 경로 목록~~ | — | ✅ **해소(2026-07-29, `/v3/api-docs` 전수 확인)**. 전역 `security: [{"JWT ": []}]` 이고 `security: []`로 **오버라이드해 인증을 면제한 경로는 정확히 4개**: `POST /api/v1/auth/refresh`, `POST /api/v1/auth/login`, `POST /api/v1/auth/signup`, **`GET /api/v1/terms`**. ⚠️ `POST /api/v1/auth/logout`은 **인증 필요**(security 미선언 = 전역 상속)이므로 제외 목록에 넣으면 안 된다. → 설계 4-8의 제외 목록에 **`api/v1/terms` 추가**, `logout`은 제외하지 않음 |
| **R-8** | prod BASE_URL 미정 | release가 dev 서버를 본다 | 4-5에 `TODO` 명시. prod 개설 시 한 줄 교체 |
| **R-9** | 평문 토큰 저장 채택 | 보안 리뷰에서 반려될 수 있음 | 4-7에 근거·대안·교체 비용(= `core:data-local` 내부 한정)까지 기록. **팀/멘토 승인 필요** |

### 후속 이슈 제안

1. **`AuthRepository` + 소셜 로그인 토큰 교환** — `POST /auth/login`, #32(OAuth Provider)와 합류. 이 이슈의 `TokenRepository.saveToken()`이 진입점
2. **온보딩/회원가입** — `onboardingToken`, `newMember` 분기, `POST /auth/signup`
3. **로그아웃** — `POST /auth/logout` + `TokenRepository.clearToken()`
4. **강제 로그아웃 네비게이션 전이** — `app`이 `observeLoginState()` 구독. 필요 시 "만료 vs 사용자 로그아웃" 구분 이벤트 채널 추가(4-4 한계)
5. **HTTP 에러 → 도메인 예외 매핑** — `CommonResponse.code` 기반 공통 에러 체계
6. **토큰 저장 암호화(Keystore AES-GCM)** — R-9가 반려되면 즉시 승격
7. **R8/minify 활성화 + ProGuard 규칙** — R-4

### 승인 체크리스트 (사람)

- [x] R-1(401 vs 200) 확인 완료 — dev 서버 실측으로 **HTTP 401 확정**
- [x] R-7(비인증 경로) 확인 완료 — `/v3/api-docs` 전수 확인, 4개 경로 확정
- [ ] R-9(평문 토큰 저장) 팀 합의 — **미해결.** 사용자 승인 하에 4-7 권장안(평문 + 백업 제외)으로 진행하되, 보안 리뷰 반려 시 `core:data-local` 내부만 교체하면 되도록 경계를 유지한다
- [x] 4-2(별도 refresh 클라이언트) 방식 동의
- [x] 4-1(`core:data` = 인터페이스 계층) 패턴을 프로젝트 표준으로 채택하는 데 동의
- [x] #30 PR에서 #31도 함께 close 하는 데 동의
- [x] 문서 상태를 `승인됨(구현 가능)`으로 변경


