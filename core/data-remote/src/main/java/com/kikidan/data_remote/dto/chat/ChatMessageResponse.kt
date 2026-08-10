package com.kikidan.data_remote.dto.chat

import com.kikidan.data_remote.util.toInstantOrThrow
import com.kikidan.domain.model.chat.ChatAction
import com.kikidan.domain.model.chat.ChatActionType
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDate.parse

@Serializable
data class ChatMessageResponse(
    val id: String,
    val role: String,
    val content: String,
    val status: String,
    val action: ChatActionResponse? = null,
    val createdAt: String,
)

@Serializable
data class ChatActionResponse(
    val type: String,
    val label: String,
    val category: String,
    val date: String? = null,
)

internal fun ChatActionResponse.toDomain(): ChatAction =
    ChatAction(
        type = type.toChatActionType(),
        label = label,
        category = category,
        date = date?.let(LocalDate::parse),
    )

internal fun ChatMessageResponse.toDomain(): ChatMessage =
    ChatMessage(
        id = id,
        role = role.toMessageRole(),
        content = content,
        status = status.toMessageStatus(),
        action = action?.toDomain(),
        createdAt = createdAt.toInstantOrThrow(),
    )

private fun String.toMessageRole(): MessageRole = MessageRole.valueOf(uppercase())

private fun String.toMessageStatus(): MessageStatus = MessageStatus.valueOf(uppercase())

private fun String.toChatActionType(): ChatActionType = ChatActionType.valueOf(uppercase())
