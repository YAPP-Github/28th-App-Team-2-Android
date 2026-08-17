package com.kikidan.domain.model.chat

import java.time.LocalDate

data class ChatAction(
    val type: ChatActionType,
    val label: String,
    val category: String,
    val date: LocalDate?,
)

enum class ChatActionType {
    CALENDAR_ADD,
}
