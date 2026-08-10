package com.kikidan.data_remote.dto.chat

import com.kikidan.domain.model.chat.Conversation
import kotlinx.serialization.Serializable

@Serializable
data class ConversationDetailResponse(
    val id: String,
    val title: String,
    val messages: List<ChatMessageResponse> = emptyList(),
)

internal fun ConversationDetailResponse.toDomain(): Conversation =
    Conversation(id = id, title = title, messages = messages.map { it.toDomain() })
