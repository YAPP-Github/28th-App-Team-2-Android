package com.kikidan.domain.fake

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeChatRepository : ChatRepository {
    var lastSentConversationId: String? = null
    var lastSentContent: String? = null
    var sendMessageCallCount: Int = 0
    var streamEvents: List<Result<ChatStreamEvent>> = emptyList()

    override suspend fun getChatEntry(): Result<ChatEntry> = Result.failure(NotImplementedError())

    override fun sendMessage(
        conversationId: String?,
        content: String,
    ): Flow<Result<ChatStreamEvent>> {
        lastSentConversationId = conversationId
        lastSentContent = content
        sendMessageCallCount++
        return streamEvents.asFlow()
    }

    override suspend fun getConversations(): Result<List<ConversationSummary>> = Result.failure(NotImplementedError())

    override suspend fun getConversationDetail(conversationId: String): Result<Conversation> =
        Result.failure(NotImplementedError())

    override suspend fun deleteConversation(conversationId: String): Result<Unit> =
        Result.failure(NotImplementedError())
}
