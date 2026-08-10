package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDetailResponse(
    val id: String,
    val title: String,
    val messages: List<ChatMessageResponse> = emptyList(),
)
