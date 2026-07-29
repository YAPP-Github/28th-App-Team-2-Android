package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import com.kikidan.domain.model.auth.AuthToken
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRemoteDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteAuthDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteAuthDataSourceImpl(Lazy { client })
    }

    /** T9: postRefresh 정상 — AuthToken 도메인 모델 반환 */
    @Test
    fun `T9 - postRefresh 성공 시 AuthToken 반환`() =
        runTest {
            val body =
                """{"success":true,"code":"200","message":"ok",""" +
                    """"data":{"accessToken":"a-1","refreshToken":"r-1"}}"""
            val sut =
                buildSut {
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            val result = sut.postRefresh("old-refresh")

            assertEquals(AuthToken("a-1", "r-1"), result)
        }

    /** T10: ignoreUnknownKeys — 서버가 모르는 필드를 추가해도 파싱 성공 */
    @Test
    fun `T10 - 미지 필드 있어도 파싱 성공`() =
        runTest {
            val sut =
                buildSut {
                    val body =
                        """{"success":true,"code":"200","message":"ok",""" +
                            """"data":{"accessToken":"a-1","refreshToken":"r-1","unknownField":"v"},""" +
                            """"extraTopLevel":123}"""
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            val result = runCatching { sut.postRefresh("old-refresh") }

            assertTrue(result.isSuccess)
        }

    /** T11: 4xx 응답 시 DataSource가 예외를 삼키지 않고 throw (rules/20-data) */
    @Test
    fun `T11 - 4xx 응답 시 예외 throw, 삼키지 않음`() =
        runTest {
            val sut =
                buildSut {
                    respond("", HttpStatusCode.Unauthorized)
                }

            val result = runCatching { sut.postRefresh("old-refresh") }

            assertTrue(result.isFailure)
        }
}
