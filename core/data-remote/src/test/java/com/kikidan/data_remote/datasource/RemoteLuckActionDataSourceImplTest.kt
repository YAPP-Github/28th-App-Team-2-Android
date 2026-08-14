package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
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

class RemoteLuckActionDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteLuckActionDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteLuckActionDataSourceImpl(Lazy { client })
    }

    @Test
    fun `getTodayLuckActions가 정상 응답이면 LuckAction 목록으로 반환된다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    [{"id":"a-1","fortuneCategory":"LOVE","score":5,"title":"메시지 보내기","achieved":false}]
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getTodayLuckActions()

            assertEquals(1, result.size)
            assertEquals("a-1", result[0].id)
            assertTrue(!result[0].achieved)
        }

    @Test
    fun `patchAchievement가 정상 응답이면 achieved가 true인 LuckAction을 반환한다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    {"id":"a-1","fortuneCategory":"LOVE","score":5,"title":"메시지 보내기","content":"설명","achieved":true}
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.patchAchievement("a-1")

            assertEquals("a-1", result.id)
            assertTrue(result.achieved)
        }

    @Test
    fun `getLuckAction이 정상 응답이면 LuckActionDetail 도메인으로 반환된다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    {"id":"a-1","fortuneCategory":"LOVE","score":80,"title":"사랑 액션","content":"오늘 소중한 사람에게 연락해보세요","achieved":false}
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getLuckAction("a-1")

            assertEquals("a-1", result.id)
            assertEquals(80, result.score)
            assertEquals("사랑 액션", result.title)
            assertEquals("오늘 소중한 사람에게 연락해보세요", result.content)
            assertEquals(false, result.achieved)
        }
}
