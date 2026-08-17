package com.kikidan.chat

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class FakeChatRepository : ChatRepository {
    var chatEntryResult: Result<ChatEntry> = Result.failure(NotImplementedError("chatEntryResult 미설정"))
    var conversationDetailResult: Result<Conversation> =
        Result.failure(
            NotImplementedError("conversationDetailResult 미설정"),
        )
    var streamEvents: List<Result<ChatStreamEvent>> = emptyList()

    var lastSentConversationId: String? = null
    var lastSentContent: String? = null
    var sendCallCount: Int = 0

    override suspend fun getChatEntry(): Result<ChatEntry> = chatEntryResult

    override fun sendMessage(
        conversationId: String?,
        content: String,
    ): Flow<Result<ChatStreamEvent>> {
        lastSentConversationId = conversationId
        lastSentContent = content
        sendCallCount++
        return streamEvents.asFlow()
    }

    override suspend fun getConversations(): Result<List<ConversationSummary>> = Result.failure(NotImplementedError())

    override suspend fun getConversationDetail(conversationId: String): Result<Conversation> = conversationDetailResult

    override suspend fun deleteConversation(conversationId: String): Result<Unit> =
        Result.failure(NotImplementedError())
}
