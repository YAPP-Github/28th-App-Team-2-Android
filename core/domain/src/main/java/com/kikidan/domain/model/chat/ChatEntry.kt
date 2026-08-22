package com.kikidan.domain.model.chat

data class ChatEntry(
    val greeting: String,
    val suggestions: List<ChatSuggestion>,
    val quota: ChatQuota,
)

data class ChatSuggestion(
    val emoji: String,
    val label: String,
    val seedPrompt: String,
    val category: ChatCategory?,
)

data class ChatQuota(
    val used: Int,
    val limit: Int,
) {
    val remaining: Int get() = (limit - used).coerceAtLeast(0)
}

enum class ChatCategory {
    RELATIONSHIP,
    LOVE,
    ACHIEVEMENT,
    MONEY,
    HEALTH,
}
