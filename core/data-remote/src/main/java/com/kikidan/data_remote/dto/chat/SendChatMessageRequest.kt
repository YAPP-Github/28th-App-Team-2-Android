package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class SendChatMessageRequest(
    val conversationId: String? = null,
    val content: String,
)
