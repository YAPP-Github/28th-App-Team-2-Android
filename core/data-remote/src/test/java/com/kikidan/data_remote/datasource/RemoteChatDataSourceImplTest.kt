package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import com.kikidan.data_remote.dto.chat.SendChatMessageRequest
import com.kikidan.domain.model.chat.ChatStreamEvent
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteChatDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val sseHeaders = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteChatDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteChatDataSourceImpl(Lazy { client })
    }

    @Test
    fun `getChatEntry가_정상_응답이면_ChatEntry_도메인으로_반환된다`() =
        runTest {
            val body =
                """{"success":true,"code":"200","message":"ok","data":{"greeting":"안녕","suggestions":[],"quota":{"used":1,"limit":5}}}"""
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getChatEntry()

            assertEquals("안녕", result.greeting)
            assertEquals(1, result.quota.used)
            assertEquals(5, result.quota.limit)
        }

    @Test
    fun `getChatEntry가_data_null_응답이면_IllegalArgumentException이_throw된다`() =
        runTest {
            val body = """{"success":false,"code":"500","message":"error","data":null}"""
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = runCatching { sut.getChatEntry() }

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }

    @Test
    fun `getConversations가_정상_응답이면_ConversationSummary_목록을_반환한다`() =
        runTest {
            val body =
                """{"success":true,"code":"200","message":"ok","data":{"conversations":[{"id":"c-1","title":"대화1","lastMessageAt":"2026-08-03T12:00:00Z","unread":false}]}}"""
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = sut.getConversations()

            assertEquals(1, result.size)
            assertEquals("c-1", result[0].id)
            assertEquals("대화1", result[0].title)
        }

    @Test
    fun `deleteConversation_200이면_예외_없이_반환된다`() =
        runTest {
            val body = """{"success":true,"code":"200","message":"ok","data":null}"""
            val sut = buildSut { respond(body, HttpStatusCode.OK, jsonHeaders) }

            val result = runCatching { sut.deleteConversation("c-1") }

            assertTrue(result.isSuccess)
        }

    @Test
    fun `deleteConversation_403이면_ClientRequestException이_그대로_throw된다`() =
        runTest {
            val sut = buildSut { respond("Forbidden", HttpStatusCode.Forbidden) }

            val result = runCatching { sut.deleteConversation("c-1") }

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is ClientRequestException)
        }

    @Test
    fun `postChatMessage가_SSE_start_delta_delta_done_시퀀스를_ChatStreamEvent_4개로_방출한다`() =
        runTest {
            val sseBody =
                "event: start\ndata: {\"conversationId\":\"c-1\",\"userMessageId\":\"u-1\",\"assistantMessageId\":\"a-1\",\"quotaUsed\":1,\"quotaLimit\":5}\n\n" +
                    "event: delta\ndata: {\"text\":\"안\"}\n\n" +
                    "event: delta\ndata: {\"text\":\"녕\"}\n\n" +
                    "event: done\ndata: {\"assistantMessageId\":\"a-1\"}\n\n"
            val sut = buildSut { respond(sseBody, HttpStatusCode.OK, sseHeaders) }

            val events = sut.postChatMessage(null, "테스트").toList()

            assertEquals(4, events.size)
            assertTrue(events[0] is ChatStreamEvent.Start)
            assertTrue(events[1] is ChatStreamEvent.Delta)
            assertEquals("안", (events[1] as ChatStreamEvent.Delta).text)
            assertTrue(events[2] is ChatStreamEvent.Delta)
            assertEquals("녕", (events[2] as ChatStreamEvent.Delta).text)
            assertTrue(events[3] is ChatStreamEvent.Done)
        }

    @Test
    fun `postChatMessage_conversationId가_null이면_직렬화_시_JSON에서_키가_생략된다`() {
        // explicitNulls = false 설정으로 null 필드는 JSON에서 제외됨을 직렬화 레벨에서 검증한다.
        val serialized = json.encodeToString(SendChatMessageRequest(conversationId = null, content = "안녕"))

        assertFalse("null conversationId는 JSON 키 자체가 없어야 한다", serialized.contains("conversationId"))
        assertTrue("content는 포함돼야 한다", serialized.contains("안녕"))
    }

    @Test
    fun `postChatMessage_conversationId가_있으면_직렬화_시_JSON에_포함된다`() {
        val serialized = json.encodeToString(SendChatMessageRequest(conversationId = "c-42", content = "안녕"))

        assertTrue("conversationId는 JSON에 포함돼야 한다", serialized.contains("c-42"))
        assertTrue("content는 포함돼야 한다", serialized.contains("안녕"))
    }
}
