package com.kikidan.chat

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
    val phase: ChatPhase = ChatPhase.IDLE,
    // 스트리밍 중인 답변. messages에 넣지 않는 이유는 설계 2-6 참조(틱마다 List 복사 회피).
    val streamingText: String = "",
)

enum class ChatPhase {
    IDLE,
    THINKING, // 전송했고 첫 delta 전
    TYPING, // delta 수신 중
}

sealed interface ChatSideEffect {
    data class ShowStreamingErrorMessage(
        val message: String,
    ) : ChatSideEffect

    data class Error(
        val e: Throwable,
    ) : ChatSideEffect
}
