package com.kikidan.chat.model

import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.collections.immutable.PersistentList

sealed interface HistoryState {
    data object Loading : HistoryState

    data class Success(
        val conversations: PersistentList<ConversationSummary>,
    ) : HistoryState

    data object Failure : HistoryState
}
