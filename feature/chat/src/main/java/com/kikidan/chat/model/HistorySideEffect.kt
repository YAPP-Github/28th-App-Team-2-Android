package com.kikidan.chat.model

sealed interface HistorySideEffect {
    data class Error(
        val e: Throwable,
    ) : HistorySideEffect
}
