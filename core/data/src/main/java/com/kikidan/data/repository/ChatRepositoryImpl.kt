package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteChatDataSource
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.repository.ChatRepository
import com.kikidan.domain.util.runCatchingCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ChatRepositoryImpl
    @Inject
    constructor(
        private val remoteChatDataSource: RemoteChatDataSource,
    ) : ChatRepository {
        override suspend fun getChatEntry(): Result<ChatEntry> =
            runCatchingCancellable { remoteChatDataSource.getChatEntry() }

        override fun sendMessage(
            conversationId: String?,
            content: String,
        ): Flow<Result<ChatStreamEvent>> =
            remoteChatDataSource
                .postChatMessage(conversationId, content)
                .map { Result.success(it) }
                // catch는 업스트림만 잡는다. map 뒤에 두어야 collect 블록의 예외를 삼키지 않는다.
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(Result.failure(throwable))
                }

        override suspend fun getConversations(): Result<List<ConversationSummary>> =
            runCatchingCancellable { remoteChatDataSource.getConversations() }

        override suspend fun getConversationDetail(conversationId: String): Result<Conversation> =
            runCatchingCancellable { remoteChatDataSource.getConversation(conversationId) }

        override suspend fun deleteConversation(conversationId: String): Result<Unit> =
            runCatchingCancellable { remoteChatDataSource.deleteConversation(conversationId) }
    }
