package com.kikidan.chat.model

import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatSuggestion
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class ChatState(
    val conversationId: String? = null,
    val entryState: ChatEntryState = ChatEntryState.Loading,
    val greeting: String = "",
    val suggestions: PersistentList<ChatSuggestion> = persistentListOf(),
    val quota: ChatQuota? = null,
    val messages: PersistentList<ChatMessage> = persistentListOf(),
    val input: String = "",
    val streamingChatState: StreamingChatState = StreamingChatState.Idle,
)

sealed interface ChatEntryState {
    data object Loading : ChatEntryState

    data object Success : ChatEntryState

    data object Failure : ChatEntryState
}

sealed interface StreamingChatState {
    data object Idle : StreamingChatState

    data object Thinking : StreamingChatState

    data class Typing(
        val streamingText: String = "",
    ) : StreamingChatState
}
