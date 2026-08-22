package com.kikidan.chat.model

import android.text.format.DateUtils
import com.kikidan.domain.model.chat.ConversationSummary

data class HistoryConversationUiModel(
    val id: String,
    val title: String,
    val relativeTime: String,
    val isUnread: Boolean,
)

fun ConversationSummary.toUiModel(): HistoryConversationUiModel =
    HistoryConversationUiModel(
        id = id,
        title = title,
        relativeTime =
            DateUtils
                .getRelativeTimeSpanString(
                    lastMessageAt.toEpochMilli(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                ).toString(),
        isUnread = unread,
    )
