package com.kikidan.data_remote.dto.chat

import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.ChatStreamException
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import io.ktor.sse.ServerSentEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ChatMapperTest {
    @Test
    fun `ChatEntryResponse가_ChatEntry_도메인으로_변환된다`() {
        val response =
            ChatEntryResponse(
                greeting = "안녕하세요",
                suggestions = listOf(ChatSuggestionResponse("🌟", "테스트", "seed", "CAREER")),
                quota = ChatQuotaResponse(used = 2, limit = 5),
            )

        val domain = response.toDomain()

        assertEquals("안녕하세요", domain.greeting)
        assertEquals(1, domain.suggestions.size)
        assertEquals("🌟", domain.suggestions[0].emoji)
        assertEquals(2, domain.quota.used)
        assertEquals(5, domain.quota.limit)
        assertEquals(3, domain.quota.remaining)
    }

    @Test
    fun `ChatMessageResponse_role이_ASSISTANT이면_MessageRole_ASSISTANT로_변환된다`() {
        val result = buildMessageResponse(role = "ASSISTANT").toDomain()
        assertEquals(MessageRole.ASSISTANT, result.role)
    }

    @Test
    fun `ChatMessageResponse_role이_소문자_assistant이면_MessageRole_ASSISTANT로_변환된다`() {
        val result = buildMessageResponse(role = "assistant").toDomain()
        assertEquals(MessageRole.ASSISTANT, result.role)
    }

    @Test
    fun `ChatMessageResponse_status가_알_수_없는_값이면_MessageStatus_UNKNOWN으로_변환된다`() {
        val result = buildMessageResponse(status = "WEIRD_NEW_VALUE").toDomain()
        assertEquals(MessageStatus.UNKNOWN, result.status)
    }

    @Test
    fun `createdAt이_오프셋_포함_ISO_8601이면_Instant로_파싱된다`() {
        val result = buildMessageResponse(createdAt = "2026-08-03T12:00:00Z").toDomain()
        assertEquals(Instant.parse("2026-08-03T12:00:00Z"), result.createdAt)
    }

    @Test
    fun `createdAt이_오프셋_없는_LocalDateTime이면_KST_기준_Instant로_파싱된다`() {
        val result = buildMessageResponse(createdAt = "2026-08-03T12:00:00").toDomain()
        val expected =
            LocalDateTime.parse("2026-08-03T12:00:00")
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
        assertEquals(expected, result.createdAt)
    }

    @Test
    fun `SSE_start_이벤트가_ChatStreamEvent_Start로_변환된다`() {
        val event =
            ServerSentEvent(
                event = "start",
                data = """{"conversationId":"c-1","userMessageId":"u-1","assistantMessageId":"a-1","quotaUsed":1,"quotaLimit":5}""",
            )

        val result = event.toChatStreamEventOrNull() as ChatStreamEvent.Start

        assertEquals("c-1", result.conversationId)
        assertEquals("u-1", result.userMessageId)
        assertEquals("a-1", result.assistantMessageId)
        assertEquals(1, result.quota.used)
        assertEquals(5, result.quota.limit)
        assertEquals(4, result.quota.remaining)
    }

    @Test
    fun `SSE_delta_이벤트가_ChatStreamEvent_Delta로_변환된다`() {
        val event = ServerSentEvent(event = "delta", data = """{"text":"안"}""")

        val result = event.toChatStreamEventOrNull()

        assertEquals(ChatStreamEvent.Delta("안"), result)
    }

    @Test
    fun `SSE_error_이벤트가_ChatStreamException을_throw한다`() {
        val event = ServerSentEvent(event = "error", data = """{"code":"QUOTA","message":"초과"}""")

        val thrown = runCatching { event.toChatStreamEventOrNull() }.exceptionOrNull()

        assertTrue(thrown is ChatStreamException)
        val ex = thrown as ChatStreamException
        assertEquals("QUOTA", ex.code)
        assertEquals("초과", ex.message)
    }

    @Test
    fun `SSE_done_이벤트가_ChatStreamEvent_Done으로_변환된다`() {
        val event = ServerSentEvent(event = "done", data = """{"assistantMessageId":"m-1"}""")

        val result = event.toChatStreamEventOrNull()

        assertEquals(ChatStreamEvent.Done("m-1"), result)
    }

    @Test
    fun `SSE_action_이벤트가_ChatStreamEvent_Action으로_변환된다`() {
        val event =
            ServerSentEvent(
                event = "action",
                data = """{"type":"CALENDAR","label":"일정 추가","category":"SCHEDULE","date":"2026-08-10"}""",
            )

        val result = event.toChatStreamEventOrNull() as ChatStreamEvent.Action

        assertEquals("CALENDAR", result.action.type)
        assertEquals("일정 추가", result.action.label)
        assertEquals("SCHEDULE", result.action.category)
        assertEquals(LocalDate.of(2026, 8, 10), result.action.date)
    }

    @Test
    fun `알_수_없는_SSE_이벤트_이름은_null을_반환한다`() {
        val event = ServerSentEvent(event = "unknown_event", data = """{"foo":"bar"}""")
        assertNull(event.toChatStreamEventOrNull())
    }

    @Test
    fun `SSE_event가_null인_heartbeat는_null을_반환한다`() {
        val event = ServerSentEvent(event = null, data = "heartbeat")
        assertNull(event.toChatStreamEventOrNull())
    }

    private fun buildMessageResponse(
        id: String = "m-1",
        role: String = "USER",
        content: String = "테스트",
        status: String = "COMPLETED",
        createdAt: String = "2026-08-03T12:00:00Z",
    ) = ChatMessageResponse(id = id, role = role, content = content, status = status, createdAt = createdAt)
}
