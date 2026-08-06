package com.kikidan.data_remote.dto.chat

import com.kikidan.domain.model.chat.ChatCategory
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatSuggestion
import kotlinx.serialization.Serializable

@Serializable
data class ChatEntryResponse(
    val greeting: String,
    val suggestions: List<ChatSuggestionResponse> = emptyList(),
    val quota: ChatQuotaResponse,
)

@Serializable
data class ChatSuggestionResponse(
    val emoji: String,
    val label: String,
    val seedPrompt: String,
    val category: String?,
)

@Serializable
data class ChatQuotaResponse(
    val used: Int,
    val limit: Int,
)

internal fun ChatEntryResponse.toDomain(): ChatEntry =
    ChatEntry(
        greeting = greeting,
        suggestions = suggestions.map { it.toDomain() },
        quota = quota.toDomain(),
    )

internal fun ChatSuggestionResponse.toDomain(): ChatSuggestion =
    ChatSuggestion(
        emoji = emoji,
        label = label,
        seedPrompt = seedPrompt,
        category = if (category == null) null else ChatCategory.valueOf(category),
    )

internal fun ChatQuotaResponse.toDomain(): ChatQuota = ChatQuota(used = used, limit = limit)
