package com.kikidan.domain.model.chat

import java.time.Instant

data class Conversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage>,
)

data class ConversationSummary(
    val id: String,
    val title: String,
    val lastMessageAt: Instant,
    val unread: Boolean,
)
