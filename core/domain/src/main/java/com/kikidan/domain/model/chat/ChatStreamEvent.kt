package com.kikidan.domain.model.chat

sealed interface ChatStreamEvent {
    data class Start(
        val conversationId: String,
        val userMessageId: String,
        val assistantMessageId: String,
        val quota: ChatQuota,
    ) : ChatStreamEvent

    data class Delta(
        val text: String,
    ) : ChatStreamEvent

    data class Action(
        val action: ChatAction,
    ) : ChatStreamEvent

    data class Done(
        val assistantMessageId: String,
    ) : ChatStreamEvent
}

class ChatStreamException(
    val code: String?,
    override val message: String,
) : Exception(message)
