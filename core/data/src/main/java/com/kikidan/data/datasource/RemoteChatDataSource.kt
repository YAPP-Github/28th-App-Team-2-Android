package com.kikidan.data.datasource

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface RemoteChatDataSource {
    suspend fun getChatEntry(): ChatEntry

    fun postChatMessage(
        conversationId: String?,
        content: String,
    ): Flow<ChatStreamEvent>

    suspend fun getConversations(): List<ConversationSummary>

    suspend fun getConversation(conversationId: String): Conversation

    suspend fun deleteConversation(conversationId: String)
}
