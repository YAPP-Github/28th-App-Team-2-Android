package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteChatDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.chat.ChatEntryResponse
import com.kikidan.data_remote.dto.chat.ConversationDetailResponse
import com.kikidan.data_remote.dto.chat.ConversationListResponse
import com.kikidan.data_remote.dto.chat.SendChatMessageRequest
import com.kikidan.data_remote.dto.chat.toChatStreamEventOrNull
import com.kikidan.data_remote.dto.chat.toDomain
import com.kikidan.data_remote.sse.serverSentEvents
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class RemoteChatDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteChatDataSource {
        override suspend fun getChatEntry(): ChatEntry =
            client
                .get()
                .get(CHAT_ENTRY_URL)
                .bodyNotNull<ChatEntryResponse>()
                .toDomain()

        override fun postChatMessage(
            conversationId: String?,
            content: String,
        ): Flow<ChatStreamEvent> =
            client
                .get()
                .serverSentEvents(CHAT_MESSAGES_URL) {
                    method = HttpMethod.Post
                    setBody(SendChatMessageRequest(conversationId, content))
                }.mapNotNull { it.toChatStreamEventOrNull() }

        override suspend fun getConversations(): List<ConversationSummary> =
            client
                .get()
                .get(CONVERSATIONS_URL)
                .bodyNotNull<ConversationListResponse>()
                .toDomain()

        override suspend fun getConversation(conversationId: String): Conversation =
            client
                .get()
                .get(conversationUrl(conversationId))
                .bodyNotNull<ConversationDetailResponse>()
                .toDomain()

        override suspend fun deleteConversation(conversationId: String) {
            client
                .get()
                .delete(conversationUrl(conversationId))
                .body<CommonResponse<Unit>>()
        }

        companion object {
            private const val CHAT_ENTRY_URL = "api/v1/chat/entry"
            private const val CHAT_MESSAGES_URL = "api/v1/chat/messages"
            private const val CONVERSATIONS_URL = "api/v1/chat/conversations"

            private fun conversationUrl(conversationId: String): String = "$CONVERSATIONS_URL/$conversationId"
        }
    }
