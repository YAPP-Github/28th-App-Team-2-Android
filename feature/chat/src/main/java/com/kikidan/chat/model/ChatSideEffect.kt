package com.kikidan.chat.model

sealed interface ChatSideEffect {
    data class ShowStreamingErrorMessage(
        val message: String,
    ) : ChatSideEffect

    data class Error(
        val e: Throwable,
    ) : ChatSideEffect
}
