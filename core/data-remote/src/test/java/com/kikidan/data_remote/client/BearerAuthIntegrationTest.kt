package com.kikidan.data_remote.client

import com.kikidan.data_remote.datasource.AuthRemoteDataSourceImpl
import com.kikidan.data_remote.fake.FakeTokenDataSource
import com.kikidan.domain.model.auth.AuthToken
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

    /** 테스트용 클라이언트 쌍 조립: 프로덕션과 동일한 확장 함수 사용 */
    private fun buildClients(
        fakeTokenDataSource: FakeTokenDataSource,
        authHandler: MockRequestHandler,
        refreshHandler: MockRequestHandler,
    ): Pair<HttpClient, HttpClient> {
        val refreshEngine = MockEngine(refreshHandler)
        val refreshClient = HttpClient(refreshEngine) { installTodakunDefaults(json, baseUrl) }
        val authRemoteDataSource = AuthRemoteDataSourceImpl(refreshClient)

        val authEngine = MockEngine(authHandler)
        val authClient =
            HttpClient(authEngine) {
                installTodakunDefaults(json, baseUrl)
                installBearerAuth(fakeTokenDataSource, authRemoteDataSource)
            }
        return authClient to refreshClient
    }

    /** T1: 저장된 토큰이 있으면 Authorization: Bearer <token> 헤더가 부착된다 (AC-2) */
    @Test
    fun `T1 - 저장된 토큰이 있으면 Authorization 헤더 포함`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource().apply { saveToken(initialToken) }
            var capturedAuthHeader: String? = null

            val (authClient) =
                buildClients(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { request ->
                        capturedAuthHeader = request.headers[HttpHeaders.Authorization]
                        respond(successJson, HttpStatusCode.OK, jsonHeaders)
                    },
                    refreshHandler = {
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            authClient.get("api/test")

            assertEquals("Bearer access-1", capturedAuthHeader)
        }

    /** T2: 저장된 토큰이 없으면 Authorization 헤더 없이 나가고 refresh 호출 없음 */
    @Test
    fun `T2 - 저장된 토큰 없으면 Authorization 헤더 없음, refresh 미호출`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource() // 토큰 없음
            var capturedAuthHeader: String? = "sentinel"
            var refreshCalled = false

            val (authClient) =
                buildClients(
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

            authClient.get("api/test")

            assertNull(capturedAuthHeader)
            assertFalse(refreshCalled)
        }

    /** T3: 401 → refresh → 재시도하여 200 반환. 호출부는 재시도를 인지하지 않는다 (AC-3) */
    @Test
    fun `T3 - 401 응답 시 refresh 후 원 요청 자동 재시도`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource().apply { saveToken(initialToken) }
            var refreshCalled = false
            val authCallCount = AtomicInteger(0)
            var retryAuthHeader: String? = null

            val (authClient) =
                buildClients(
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

            authClient.get("api/test") // 예외 없이 200으로 완료되어야 한다

            assertTrue(refreshCalled)
            assertEquals(2, authCallCount.get())
            assertEquals("Bearer access-2", retryAuthHeader)
        }

    /** T4: refresh 성공 시 새 access AND refresh 토큰이 저장된다 (rotation, AC-4) */
    @Test
    fun `T4 - refresh 성공 시 새 access·refresh 토큰 저장`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource().apply { saveToken(initialToken) }
            val authCallCount = AtomicInteger(0)

            val (authClient) =
                buildClients(
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

            authClient.get("api/test")

            assertEquals(newToken, fakeTokenDs.savedToken)
        }

    /** T5: refresh 실패 시 저장된 토큰 삭제 + 원 요청 에러 전파 (AC-5) */
    @Test
    fun `T5 - refresh 실패 시 토큰 클리어 및 예외 전파`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource().apply { saveToken(initialToken) }

            val (authClient) =
                buildClients(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { respond("", HttpStatusCode.Unauthorized) },
                    refreshHandler = { respond("", HttpStatusCode.Unauthorized) },
                )

            val result = runCatching { authClient.get("api/test") }

            assertTrue(result.isFailure)
            assertTrue(fakeTokenDs.clearTokenCalled)
            assertNull(fakeTokenDs.savedToken)
        }

    /** T6: 5개 동시 401 → refresh 정확히 1회 (동시성 보장, AC-6) */
    @Test
    fun `T6 - 5개 동시 401 시 refresh 정확히 1회`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource().apply { saveToken(initialToken) }
            val gate = CompletableDeferred<Unit>()
            val authInitialCount = AtomicInteger(0)
            val refreshCallCount = AtomicInteger(0)

            // 5번째 초기 요청이 들어와야 gate 열림 → refresh가 그 전에 응답하지 않도록 보장
            val refreshEngine =
                MockEngine { _ ->
                    gate.await()
                    refreshCallCount.incrementAndGet()
                    respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                }
            val refreshClient = HttpClient(refreshEngine) { installTodakunDefaults(json, baseUrl) }
            val authRemoteDataSource = AuthRemoteDataSourceImpl(refreshClient)

            val authEngine =
                MockEngine { _ ->
                    val n = authInitialCount.incrementAndGet()
                    if (n <= 5) {
                        if (n == 5) gate.complete(Unit)
                        respond("", HttpStatusCode.Unauthorized)
                    } else {
                        respond(successJson, HttpStatusCode.OK, jsonHeaders)
                    }
                }
            val authClient =
                HttpClient(authEngine) {
                    installTodakunDefaults(json, baseUrl)
                    installBearerAuth(fakeTokenDs, authRemoteDataSource)
                }

            val results =
                (1..5)
                    .map {
                        async(Dispatchers.IO) { runCatching { authClient.get("api/test") } }
                    }.awaitAll()

            assertEquals(1, refreshCallCount.get())
            assertTrue("모든 요청이 성공해야 함", results.all { it.isSuccess })
        }

    /** T7: sendWithoutRequest — NO_AUTH_PATHS 경로에는 Authorization 헤더 미부착 */
    @Test
    fun `T7 - sendWithoutRequest - 비인증 경로에는 Authorization 헤더 없음`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource().apply { saveToken(initialToken) }
            var capturedHeader: String? = "sentinel"

            val (authClient) =
                buildClients(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { request ->
                        capturedHeader = request.headers[HttpHeaders.Authorization]
                        respond(successJson, HttpStatusCode.OK, jsonHeaders)
                    },
                    refreshHandler = {
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            // LOGIN 경로는 NO_AUTH_PATHS에 포함 → sendWithoutRequest = false → 헤더 미부착
            authClient.get("api/v1/auth/login")

            assertNull(capturedHeader)
        }

    /** T8: oldTokens == null 상태에서 401 → refresh HTTP 없이 즉시 실패, 토큰 클리어 */
    @Test
    fun `T8 - oldTokens null 상태 401 시 무한 루프 없이 실패`() =
        runTest {
            val fakeTokenDs = FakeTokenDataSource() // 토큰 없음 → oldTokens = null
            var refreshHttpCalled = false

            val (authClient) =
                buildClients(
                    fakeTokenDataSource = fakeTokenDs,
                    authHandler = { respond("", HttpStatusCode.Unauthorized) },
                    refreshHandler = {
                        refreshHttpCalled = true
                        respond(newTokenRefreshJson, HttpStatusCode.OK, jsonHeaders)
                    },
                )

            val result = runCatching { authClient.get("api/test") }

            assertTrue(result.isFailure)
            assertFalse("oldTokens=null이면 refresh HTTP 미호출", refreshHttpCalled)
        }
}
