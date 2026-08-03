package com.kikidan.data_remote.dto.chat

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.domain.model.chat.ChatAction
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.ChatStreamException
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import io.ktor.sse.ServerSentEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

internal fun ChatEntryResponse.toDomain(): ChatEntry =
    ChatEntry(
        greeting = greeting,
        suggestions = suggestions.map { it.toDomain() },
        quota = quota.toDomain(),
    )

internal fun ChatSuggestionResponse.toDomain(): ChatSuggestion =
    ChatSuggestion(emoji = emoji, label = label, seedPrompt = seedPrompt, category = category)

internal fun ChatQuotaResponse.toDomain(): ChatQuota = ChatQuota(used = used, limit = limit)

internal fun ChatMessageResponse.toDomain(): ChatMessage =
    ChatMessage(
        id = id,
        role = role.toMessageRole(),
        content = content,
        status = status.toMessageStatus(),
        action = action?.toDomain(),
        createdAt = createdAt.toInstantOrThrow(),
    )

internal fun ChatActionResponse.toDomain(): ChatAction =
    ChatAction(
        type = type,
        label = label,
        category = category,
        date = date?.let(LocalDate::parse),
    )

internal fun ConversationListResponse.toDomain(): List<ConversationSummary> =
    conversations.map { it.toDomain() }

internal fun ConversationSummaryResponse.toDomain(): ConversationSummary =
    ConversationSummary(
        id = id,
        title = title,
        lastMessageAt = lastMessageAt.toInstantOrThrow(),
        unread = unread,
    )

internal fun ConversationDetailResponse.toDomain(): Conversation =
    Conversation(id = id, title = title, messages = messages.map { it.toDomain() })

/**
 * SSE 이벤트를 도메인 이벤트로 변환한다.
 * 알 수 없는 이벤트(heartbeat, comment 등)는 null을 반환해 무시한다.
 * 서버 error 이벤트는 ChatStreamException을 throw해 Flow를 종료시킨다.
 */
internal fun ServerSentEvent.toChatStreamEventOrNull(): ChatStreamEvent? {
    val payload = data ?: return null
    return when (event) {
        EVENT_START -> {
            val dto = TodakunJson.decodeFromString<ChatStreamStartResponse>(payload)
            ChatStreamEvent.Start(
                conversationId = dto.conversationId,
                userMessageId = dto.userMessageId,
                assistantMessageId = dto.assistantMessageId,
                quota = ChatQuota(used = dto.quotaUsed, limit = dto.quotaLimit),
            )
        }

        EVENT_DELTA ->
            ChatStreamEvent.Delta(
                TodakunJson.decodeFromString<ChatStreamDeltaResponse>(payload).text,
            )

        EVENT_ACTION ->
            ChatStreamEvent.Action(
                TodakunJson.decodeFromString<ChatActionResponse>(payload).toDomain(),
            )

        EVENT_DONE ->
            ChatStreamEvent.Done(
                TodakunJson.decodeFromString<ChatStreamDoneResponse>(payload).assistantMessageId,
            )

        EVENT_ERROR -> {
            val dto = TodakunJson.decodeFromString<ChatStreamErrorResponse>(payload)
            throw ChatStreamException(dto.code, dto.message ?: "답변 생성에 실패했어요.")
        }

        else -> null
    }
}

private fun String.toMessageRole(): MessageRole =
    runCatching { MessageRole.valueOf(uppercase()) }.getOrDefault(MessageRole.UNKNOWN)

private fun String.toMessageStatus(): MessageStatus =
    runCatching { MessageStatus.valueOf(uppercase()) }.getOrDefault(MessageStatus.UNKNOWN)

// 서버 date-time에 오프셋이 포함되는지 미확정. 없으면 KST로 간주한다.
private fun String.toInstantOrThrow(): Instant =
    runCatching { Instant.parse(this) }
        .getOrElse { LocalDateTime.parse(this).atZone(KST).toInstant() }

private val KST: ZoneId = ZoneId.of("Asia/Seoul")

private const val EVENT_START = "start"
private const val EVENT_DELTA = "delta"
private const val EVENT_ACTION = "action"
private const val EVENT_DONE = "done"
private const val EVENT_ERROR = "error"
