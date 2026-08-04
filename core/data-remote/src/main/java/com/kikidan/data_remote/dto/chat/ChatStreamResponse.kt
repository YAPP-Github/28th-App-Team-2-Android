package com.kikidan.data_remote.dto.chat

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
