package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SendChatMessageUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        operator fun invoke(
            conversationId: String?,
            content: String,
        ): Flow<Result<ChatStreamEvent>> {
            val trimmed = content.trim()
            if (trimmed.isEmpty() || trimmed.length > MAX_CONTENT_LENGTH) {
                return flowOf(
                    Result.failure(
                        IllegalArgumentException("메시지는 1자 이상 ${MAX_CONTENT_LENGTH}자 이하여야 합니다."),
                    ),
                )
            }
            return chatRepository.sendMessage(conversationId, trimmed)
        }

        companion object {
            // presentation의 입력창 maxLength도 이 상수를 참조한다.
            const val MAX_CONTENT_LENGTH = 500
        }
    }
