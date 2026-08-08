package com.kikidan.chat.model

import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class HistoryState(
    val isLoading: Boolean = true,
    val conversations: PersistentList<ConversationSummary> = persistentListOf(),
)
