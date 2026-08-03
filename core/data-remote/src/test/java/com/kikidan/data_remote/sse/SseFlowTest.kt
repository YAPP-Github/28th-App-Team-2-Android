package com.kikidan.data_remote.sse

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SseFlowTest {
    private val baseUrl = "https://test.example.com/"
    private val sseHeaders = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())

    private fun buildClient(handler: MockRequestHandler): HttpClient {
        val engine = MockEngine(handler)
        return HttpClient(engine) { installTodakunDefaults(TodakunJson, baseUrl) }
    }

    @Test
    fun `named_event_3개가_순서대로_방출된다`() =
        runTest {
            val client =
                buildClient {
                    respond(
                        content = "event: start\ndata: {\"id\":\"1\"}\n\nevent: start\ndata: {\"id\":\"2\"}\n\nevent: start\ndata: {\"id\":\"3\"}\n\n",
                        status = HttpStatusCode.OK,
                        headers = sseHeaders,
                    )
                }

            val events = client.serverSentEvents(baseUrl).toList()

            assertEquals(3, events.size)
            events.forEach { assertEquals("start", it.event) }
            assertEquals("{\"id\":\"1\"}", events[0].data)
            assertEquals("{\"id\":\"2\"}", events[1].data)
            assertEquals("{\"id\":\"3\"}", events[2].data)
        }

    @Test
    fun `event_필드_없는_data만_있는_이벤트는_event가_null이다`() =
        runTest {
            val client =
                buildClient {
                    respond(
                        content = "data: hello\n\n",
                        status = HttpStatusCode.OK,
                        headers = sseHeaders,
                    )
                }

            val events = client.serverSentEvents(baseUrl).toList()

            assertEquals(1, events.size)
            assertNull(events[0].event)
            assertEquals("hello", events[0].data)
        }

    @Test
    fun `서버가_스트림을_정상_종료하면_Flow가_정상_완료된다`() =
        runTest {
            val client =
                buildClient {
                    respond(
                        content = "data: done\n\n",
                        status = HttpStatusCode.OK,
                        headers = sseHeaders,
                    )
                }

            val result = runCatching { client.serverSentEvents(baseUrl).toList() }

            assertTrue(result.isSuccess)
        }

    @Test
    fun `404_응답은_collect_시점에_ClientRequestException이_throw된다`() =
        runTest {
            val client = buildClient { respond("Not Found", HttpStatusCode.NotFound) }

            val result = runCatching { client.serverSentEvents(baseUrl).toList() }

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ClientRequestException)
        }

    @Test
    fun `collect_취소시_세션이_닫히고_예외_없이_종료된다`() =
        runTest {
            val client =
                buildClient {
                    respond(
                        content = "data: first\n\ndata: second\n\ndata: third\n\n",
                        status = HttpStatusCode.OK,
                        headers = sseHeaders,
                    )
                }

            val result = runCatching { client.serverSentEvents(baseUrl).take(1).toList() }

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.size)
        }
}
