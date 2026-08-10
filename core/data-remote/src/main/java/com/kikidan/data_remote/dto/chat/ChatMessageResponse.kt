package com.kikidan.data_remote.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageResponse(
    val id: String,
    val role: String,
    val content: String,
    val status: String,
    val action: ChatActionResponse? = null,
    val createdAt: String,
)

@Serializable
data class ChatActionResponse(
    val type: String,
    val label: String,
    val category: String,
    val date: String? = null,
)
