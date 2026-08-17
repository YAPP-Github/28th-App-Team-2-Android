package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.ConversationSummary
import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class GetConversationsUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(): Result<List<ConversationSummary>> = chatRepository.getConversations()
    }
