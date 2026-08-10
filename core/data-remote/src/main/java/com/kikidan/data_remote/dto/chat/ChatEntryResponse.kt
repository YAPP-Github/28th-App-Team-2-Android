package com.kikidan.data_remote.dto.chat

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
    val category: String,
)

@Serializable
data class ChatQuotaResponse(
    val used: Int,
    val limit: Int,
)
