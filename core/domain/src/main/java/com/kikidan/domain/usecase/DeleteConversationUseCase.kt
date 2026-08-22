package com.kikidan.domain.usecase

import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class DeleteConversationUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(conversationId: String): Result<Unit> =
            chatRepository.deleteConversation(conversationId)
    }
