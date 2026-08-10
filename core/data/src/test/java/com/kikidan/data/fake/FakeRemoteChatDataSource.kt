package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteChatDataSource
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRemoteChatDataSource : RemoteChatDataSource {
    var chatEntry: ChatEntry = ChatEntry(greeting = "안녕", suggestions = emptyList(), quota = ChatQuota(0, 5))
    var throwOnGetChatEntry: Throwable? = null

    var throwOnDeleteConversation: Throwable? = null

    // emit streamEvents in order, then throw streamThrowable if non-null
    var streamEvents: List<ChatStreamEvent> = emptyList()
    var streamThrowable: Throwable? = null

    override suspend fun getChatEntry(): ChatEntry {
        throwOnGetChatEntry?.let { throw it }
        return chatEntry
    }

    override fun postChatMessage(
        conversationId: String?,
        content: String,
    ): Flow<ChatStreamEvent> =
        flow {
            streamEvents.forEach { emit(it) }
            streamThrowable?.let { throw it }
        }

    override suspend fun getConversations(): List<ConversationSummary> = error("not used")

    override suspend fun getConversation(conversationId: String): Conversation = error("not used")

    override suspend fun deleteConversation(conversationId: String) {
        throwOnDeleteConversation?.let { throw it }
    }
}
