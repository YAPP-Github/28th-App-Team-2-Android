package com.kikidan.chat.model

import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatSuggestion

data class ChatState(
    val conversationId: String? = null,
    val isLoading: Boolean = true,
    val greeting: String = "",
    val suggestions: List<ChatSuggestion> = emptyList(),
    val quota: ChatQuota? = null,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val streamingChatState: StreamingChatState = StreamingChatState.Idle
)

sealed interface StreamingChatState {
    data object Idle : StreamingChatState

    data object Thinking : StreamingChatState

    data class Typing(
        val streamingText: String = ""
    ) : StreamingChatState
}

