package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class GetConversationDetailUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(conversationId: String): Result<Conversation> =
            chatRepository.getConversationDetail(conversationId)
    }
