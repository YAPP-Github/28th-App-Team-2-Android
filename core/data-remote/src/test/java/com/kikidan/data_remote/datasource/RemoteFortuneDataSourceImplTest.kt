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
import org.junit.Test
import java.time.LocalDate

class RemoteFortuneDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteFortuneDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteFortuneDataSourceImpl(Lazy { client })
    }

    @Test
    fun `getTodayFortuneScores가 정상 응답이면 FortuneScore 목록으로 반환된다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    {"id":"f-1","fortuneDate":"2026-07-24","score":60,"title":"오늘의 운세",
                     "luckActionScores":[{"id":"s-1","fortuneCategory":"LOVE","score":21}]}
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getTodayFortuneScores()

            assertEquals(1, result.size)
            assertEquals(21, result[0].score)
        }

    @Test
    fun `getFortuneHistory가 정상 응답이면 DailyFortuneHistoryEntry 목록으로 반환된다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    [{"id":"f-1","fortuneDate":"2026-07-24",
                      "luckActions":[{"id":"a-1","fortuneCategory":"LOVE","title":"메시지 보내기","achieved":true}]}]
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getFortuneHistory(LocalDate.of(2026, 7, 24))

            assertEquals(1, result.size)
            assertEquals("f-1", result[0].id)
            assertEquals(LocalDate.of(2026, 7, 24), result[0].fortuneDate)
            assertEquals(1, result[0].actions.size)
        }

    @Test
    fun `getFortuneDetailScores가 정상 응답이면 FortuneScore 목록으로 반환된다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    {"id":"f-1","luckActionScores":[{"id":"s-1","fortuneCategory":"MONEY","score":93}]}
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getFortuneDetailScores("f-1")

            assertEquals(1, result.size)
            assertEquals(93, result[0].score)
        }
}
