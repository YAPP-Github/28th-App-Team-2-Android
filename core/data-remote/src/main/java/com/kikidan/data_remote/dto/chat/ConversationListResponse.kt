package com.kikidan.data_remote.dto.chat

import com.kikidan.data_remote.util.toInstantOrThrow
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Serializable
data class ConversationListResponse(
    val conversations: List<ConversationSummaryResponse> = emptyList(),
)

@Serializable
data class ConversationSummaryResponse(
    val id: String,
    val title: String,
    val lastMessageAt: String,
    val unread: Boolean = false,
)

internal fun ConversationListResponse.toDomain(): List<ConversationSummary> = conversations.map { it.toDomain() }

internal fun ConversationSummaryResponse.toDomain(): ConversationSummary =
    ConversationSummary(
        id = id,
        title = title,
        lastMessageAt = lastMessageAt.toInstantOrThrow(),
        unread = unread,
    )
