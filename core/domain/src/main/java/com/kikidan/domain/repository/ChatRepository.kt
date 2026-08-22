package com.kikidan.domain.repository

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatEntry(): Result<ChatEntry>

    // conversationId가 null이면 새 대화를 시작한다. 새 대화의 id는 첫 Start 이벤트로 내려온다.
    fun sendMessage(
        conversationId: String?,
        content: String,
    ): Flow<Result<ChatStreamEvent>>

    suspend fun getConversations(): Result<List<ConversationSummary>>

    suspend fun getConversationDetail(conversationId: String): Result<Conversation>

    suspend fun deleteConversation(conversationId: String): Result<Unit>
}
