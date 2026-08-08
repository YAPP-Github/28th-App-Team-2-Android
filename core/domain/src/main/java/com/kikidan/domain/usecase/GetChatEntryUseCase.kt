package com.kikidan.domain.usecase

import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatEntryUseCase
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) {
        suspend operator fun invoke(): Result<ChatEntry> = chatRepository.getChatEntry()
    }
