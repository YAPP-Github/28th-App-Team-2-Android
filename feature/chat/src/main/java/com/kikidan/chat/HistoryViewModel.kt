package com.kikidan.chat

import androidx.lifecycle.ViewModel
import com.kikidan.chat.model.HistorySideEffect
import com.kikidan.chat.model.HistoryState
import com.kikidan.domain.usecase.DeleteConversationUseCase
import com.kikidan.domain.usecase.GetConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val getConversations: GetConversationsUseCase,
        private val deleteConversation: DeleteConversationUseCase,
    ) : ViewModel(),
        ContainerHost<HistoryState, HistorySideEffect> {
        override val container = container<HistoryState, HistorySideEffect>(HistoryState())

        fun load() =
            intent {
                reduce { state.copy(isLoading = true) }
                getConversations()
                    .onSuccess { conversations ->
                        reduce { state.copy(conversations = conversations.toPersistentList(), isLoading = false) }
                    }.onFailure {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(HistorySideEffect.Error(it))
                    }
            }

        fun onDeleteClick(conversationId: String) =
            intent {
                deleteConversation(conversationId)
                    .onSuccess {
                        reduce {
                            state.copy(
                                conversations =
                                    state.conversations
                                        .filterNot { it.id == conversationId }
                                        .toPersistentList(),
                            )
                        }
                    }.onFailure { postSideEffect(HistorySideEffect.Error(it)) }
            }
    }
