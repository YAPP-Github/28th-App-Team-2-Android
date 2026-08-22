package com.kikidan.data_remote.dto.chat

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatStreamEvent
import io.ktor.sse.ServerSentEvent
import kotlinx.serialization.Serializable

// SSE 이벤트 data payload. 백엔드 이벤트 DTO와 1:1이며 전 필드 non-null이다.

@Serializable
data class ChatStreamStartResponse(
    val conversationId: String,
    val userMessageId: String,
    val assistantMessageId: String,
    val quotaUsed: Int,
    val quotaLimit: Int,
)

@Serializable
data class ChatStreamDeltaResponse(
    val text: String,
)

@Serializable
data class ChatStreamDoneResponse(
    val assistantMessageId: String,
)

@Serializable
data class ChatStreamErrorResponse(
    val code: String,
    val message: String,
)

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

        EVENT_DELTA -> {
            ChatStreamEvent.Delta(
                TodakunJson.decodeFromString<ChatStreamDeltaResponse>(payload).text,
            )
        }

        EVENT_ACTION -> {
            ChatStreamEvent.Action(
                TodakunJson.decodeFromString<ChatActionResponse>(payload).toDomain(),
            )
        }

        EVENT_DONE -> {
            ChatStreamEvent.Done(
                TodakunJson.decodeFromString<ChatStreamDoneResponse>(payload).assistantMessageId,
            )
        }

        EVENT_ERROR -> {
            val dto = TodakunJson.decodeFromString<ChatStreamErrorResponse>(payload)
            ChatStreamEvent.Error(
                dto.code,
                dto.message,
            )
        }

        else -> {
            null
        }
    }
}

private const val EVENT_START = "start"
private const val EVENT_DELTA = "delta"
private const val EVENT_ACTION = "action"
private const val EVENT_DONE = "done"
private const val EVENT_ERROR = "error"
