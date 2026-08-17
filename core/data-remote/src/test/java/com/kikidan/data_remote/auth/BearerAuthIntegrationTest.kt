package com.kikidan.data_remote.auth

import com.kikidan.data_remote.datasource.RemoteAuthDataSourceImpl
import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installBearerAuth
import com.kikidan.data_remote.di.installTodakunDefaults
import com.kikidan.data_remote.fake.FakeLocalTokenDataSource
import com.kikidan.domain.model.auth.AuthToken
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

class BearerAuthIntegrationTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"

    private val initialToken = AuthToken("access-1", "refresh-1")
    private val newToken = AuthToken("access-2", "refresh-2")

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val newTokenRefreshJson =
        """
        {"success":true,"code":"200","message":"ok","data":{"accessToken":"access-2","refreshToken":"refresh-2"}}
        """.trimIndent()
    private val successJson = """{"success":true,"code":"200","message":"ok"}"""

    // 테스트용 단일 클라이언트 조립: 프로덕션과 동일한 installTodakunDefaults/installBearerAuth를 사용한다.
    // AuthRemoteDataSourceImpl이 Lazy<HttpClient>로 자신이 속한 클라이언트를 되받아 쓰므로
    // client(lateinit) → dataSource(Lazy로 client 참조) → client 초기화 순서로 조립하고,
    // MockEngine 1개가 request 경로로 REFRESH와 나머지를 분기해 각 handler로 위임한다.
    private fun buildClient(
        fakeTokenDataSource: FakeLocalTokenDataSource,
        authHandler: MockRequestHandler,
        refreshHandler: MockRequestHandler,
    ): HttpClient {
        lateinit var client: HttpClient
        val authRemoteDataSource = RemoteAuthDataSourceImpl(Lazy { client })

        val engine =
            MockEngine { request ->
                val path = request.url.encodedPath.trimStart('/')
                if (path == "api/v1/auth/refresh") {
                    refreshHandler(request)
                } else {
                    authHandler(request)
                }
            }

        client =
            HttpClient(engine) {
                installTodakunDefaults(json, baseUrl)
                installBearerAuth(fakeTokenDataSource, authRemoteDataSource)
            }
        return client
    }

    @Test
    fun `저장된_토큰이_있으면_요청에_Authorization_헤더가_부착된다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }
            var capturedAuthHeader: String? = null

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { request ->
                        capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                        respond(successJson, HttpStatusCode.OK, jsonHeaders)
                    },
                    refreshHandler = {
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // when
            authClient.get("api/test")

            // then
            assertEquals("Bearer access-1", capturedAuthHeader)
        }

    @Test
    fun `저장된_토큰이_없으면_Authorization_헤더가_붙지_않고_refresh도_호출되지_않는다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource()
            var capturedAuthHeader: String? = "sentinel"
            var refreshCalled = false

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { request ->
                        capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                        respond(successJson, HttpStatusCode.OK, jsonHeaders)
                    },
                    refreshHandler = {
                        refreshCalled = true
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // when
            authClient.get("api/test")

            // then
            assertNull(capturedAuthHeader)
            assertFalse(refreshCalled)
        }

    @Test
    fun `첫_응답이_401이고_refresh가_성공하면_원_요청이_새_토큰으로_재시도되어_성공한다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }
            var refreshCalled = false
            val authCallCount = AtomicInteger(0)
            var retryAuthHeader: String? = null

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { request ->
                        val n = authCallCount.incrementAndGet()
                        if (n == 1) {
                            respond("", HttpStatusCode.Unauthorized)
                        } else {
                            retryAuthHeader = request.headers[HttpHeaders.Authorization]
                            respond(successJson, HttpStatusCode.OK, jsonHeaders)
                        }
                    },
                    refreshHandler = {
                        refreshCalled = true
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // when
            authClient.get("api/test") // 예외 없이 200으로 완료되어야 한다

            // then
            assertTrue(refreshCalled)
            assertEquals(2, authCallCount.get())
            assertEquals("Bearer access-2", retryAuthHeader)
        }

    @Test
    fun `401로_refresh가_트리거되면_새_access와_refresh_토큰이_모두_저장된다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }
            val authCallCount = AtomicInteger(0)

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = {
                        if (authCallCount.incrementAndGet() == 1) {
                            respond("", HttpStatusCode.Unauthorized)
                        } else {
                            respond(successJson, HttpStatusCode.OK, jsonHeaders)
                        }
                    },
                    refreshHandler = {
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // when
            authClient.get("api/test")

            // then
            assertEquals(newToken, fakeTokenDs.savedToken)
        }

    @Test
    fun `refresh_요청도_401을_반환하면_저장된_토큰이_클리어되고_예외가_호출부로_전파된다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { respond("", HttpStatusCode.Unauthorized) },
                    refreshHandler = { respond("", HttpStatusCode.Unauthorized) },
                )

            // when
            val result = runCatching { authClient.get("api/test") }

            // then
            assertTrue(result.isFailure)
            assertTrue(fakeTokenDs.clearTokenCalled)
            assertNull(fakeTokenDs.savedToken)
        }

    @Test
    fun `5개_요청이_동시에_401을_받으면_refresh는_정확히_1회만_호출되고_5개_모두_성공한다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }
            val gate = CompletableDeferred<Unit>()
            val authInitialCount = AtomicInteger(0)
            val refreshCallCount = AtomicInteger(0)

            lateinit var client: HttpClient
            val authRemoteDataSource = RemoteAuthDataSourceImpl(Lazy { client })

            // 5번째 초기 요청이 들어와야 gate 열림 → refresh가 그 전에 응답하지 않도록 보장
            val engine =
                MockEngine { request ->
                    val path = request.url.encodedPath.trimStart('/')
                    if (path == "api/v1/auth/refresh") {
                        gate.await()
                        refreshCallCount.incrementAndGet()
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    } else {
                        val n = authInitialCount.incrementAndGet()
                        if (n <= 5) {
                            if (n == 5) gate.complete(Unit)
                            respond("", HttpStatusCode.Unauthorized)
                        } else {
                            respond(successJson, HttpStatusCode.OK, jsonHeaders)
                        }
                    }
                }
            client =
                HttpClient(engine) {
                    installTodakunDefaults(json, baseUrl)
                    installBearerAuth(fakeTokenDs, authRemoteDataSource)
                }

            // when
            val results =
                (1..5)
                    .map {
                        async(Dispatchers.IO) { runCatching { client.get("api/test") } }
                    }.awaitAll()

            // then
            assertEquals(1, refreshCallCount.get())
            assertTrue("모든 요청이 성공해야 함", results.all { it.isSuccess })
        }

    @Test
    fun `NO_AUTH_PATHS에_포함된_login_경로로_요청하면_sendWithoutRequest에_의해_Authorization_헤더가_부착되지_않는다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }
            var capturedHeader: String? = "sentinel"

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { request ->
                        capturedHeader = request.headers[HttpHeaders.Authorization]
                        respond(successJson, HttpStatusCode.OK, jsonHeaders)
                    },
                    refreshHandler = {
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // when
            authClient.get("api/v1/auth/login")

            // then
            assertNull(capturedHeader)
        }

    @Test
    fun `oldTokens가_null인_상태에서_401을_받으면_refresh_호출_없이_즉시_실패한다`() =
        runTest {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource()
            var refreshHttpCalled = false

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { respond("", HttpStatusCode.Unauthorized) },
                    refreshHandler = {
                        refreshHttpCalled = true
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // when
            val result = runCatching { authClient.get("api/test") }

            // then
            assertTrue(result.isFailure)
            assertFalse("oldTokens=null이면 refresh HTTP 미호출", refreshHttpCalled)
        }

    // 회귀 가드: AuthCircuitBreaker(attributes.put(AuthCircuitBreaker, Unit))가 없으면 refresh
    // 응답이 401일 때 Ktor Auth의 Send 단계 인터셉터가 이 응답도 401로 감지해, sendWithoutRequest
    // 설정에 따라 AuthTokenHolder의 Mutex 재진입으로 데드락이 나거나 executeWithNewToken의 맹목적
    // 재시도로 refresh HTTP가 2회 나간다. 이 테스트가 타임아웃이나 refresh 2회로 깨지면
    // AuthCircuitBreaker 배선이 빠진 것이다.
    @Test
    fun `refresh_응답도_401이면_AuthCircuitBreaker가_refresh_호출을_1회로_제한하고_토큰_클리어_후_예외가_전파된다`() =
        runTest(timeout = 10.seconds) {
            // given
            val fakeTokenDs = FakeLocalTokenDataSource().apply { saveToken(initialToken) }
            val refreshCallCount = AtomicInteger(0)

            val authClient =
                buildClient(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { respond("", HttpStatusCode.Unauthorized) },
                    refreshHandler = {
                        refreshCallCount.incrementAndGet()
                        respond("", HttpStatusCode.Unauthorized)
                    },
                )

            // when
            val result = runCatching { authClient.get("api/test") }

            // then
            assertEquals(1, refreshCallCount.get())
            assertTrue(fakeTokenDs.clearTokenCalled)
            assertTrue(result.isFailure)
        }
}
