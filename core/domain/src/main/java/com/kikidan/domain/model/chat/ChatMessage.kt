package com.kikidan.domain.model.chat

import java.time.Instant

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val status: MessageStatus,
    val action: ChatAction?,
    val createdAt: Instant,
)

enum class MessageRole {
    USER,
    ASSISTANT,
}

enum class MessageStatus {
    GENERATING,
    COMPLETED,
    FAILED,
}
