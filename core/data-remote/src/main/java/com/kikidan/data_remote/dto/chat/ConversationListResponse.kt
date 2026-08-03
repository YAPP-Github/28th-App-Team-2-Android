package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

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
