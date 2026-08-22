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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteNotificationDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteNotificationDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteNotificationDataSourceImpl(Lazy { client })
    }

    @Test
    fun `getNotifications가 정상 응답이면 NotificationSummary 도메인으로 반환된다`() =
        runTest {
            val body =
                """
                {"success":true,"code":"200","message":"ok","data":
                    {"unreadCount":1,"notifications":[
                        {"id":"n-1","type":"FORTUNE","title":"오늘의 운세가 도착했어요.",
                         "content":"내용","deepLink":"todakun://fortune","isRead":false,
                         "createdAt":"2026-07-24T00:00:00Z"}
                    ]}
                }
                """.trimIndent()
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getNotifications()

            assertEquals(1, result.unreadCount)
            assertEquals(1, result.notifications.size)
            assertEquals("n-1", result.notifications[0].id)
        }

    @Test
    fun `markAsRead는 read 엔드포인트로 PATCH 요청을 보낸다`() =
        runTest {
            var requestedMethod: HttpMethod? = null
            var requestedPath: String? = null
            val body = """{"success":true,"code":"200","message":"ok","data":null}"""
            val sut =
                buildSut { request ->
                    requestedMethod = request.method
                    requestedPath = request.url.encodedPath
                    respond(body, HttpStatusCode.OK, jsonHeaders)
                }

            sut.markAsRead("n-1")

            assertEquals(HttpMethod.Patch, requestedMethod)
            assertEquals("/api/v1/notifications/n-1/read", requestedPath)
        }
}
