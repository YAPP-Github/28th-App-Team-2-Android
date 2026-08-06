package com.kikidan.chat.model

import com.kikidan.domain.model.chat.ConversationSummary

data class HistoryState(
    val isLoading: Boolean = true,
    val conversations: List<ConversationSummary> = emptyList(),
)
