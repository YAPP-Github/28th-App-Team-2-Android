package com.kikidan.chat

import androidx.lifecycle.ViewModel
import com.kikidan.chat.model.ChatSideEffect
import com.kikidan.chat.model.ChatState
import com.kikidan.chat.model.StreamingChatState
import com.kikidan.domain.model.chat.ChatAction
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import com.kikidan.domain.usecase.GetChatEntryUseCase
import com.kikidan.domain.usecase.GetConversationDetailUseCase
import com.kikidan.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.transform
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val getChatEntry: GetChatEntryUseCase,
        private val getConversationDetail: GetConversationDetailUseCase,
        private val sendChatMessage: SendChatMessageUseCase,
    ) : ViewModel(),
        ContainerHost<ChatState, ChatSideEffect> {
        override val container = container<ChatState, ChatSideEffect>(ChatState())

        /** 화면 진입 시 1회. conversationId가 있으면 과거 대화를 먼저 채운다. */
        fun load(conversationId: String?) =
            intent {
                reduce { state.copy(conversationId = conversationId, isLoading = true) }

                getChatEntry()
                    .onSuccess { entry ->
                        reduce {
                            state.copy(
                                greeting = entry.greeting,
                                suggestions = entry.suggestions.toPersistentList(),
                                quota = entry.quota,
                            )
                        }
                    }.onFailure { postSideEffect(ChatSideEffect.Error(it)) }

                if (conversationId != null) {
                    getConversationDetail(conversationId)
                        .onSuccess { reduce { state.copy(messages = it.messages.toPersistentList()) } }
                        .onFailure { postSideEffect(ChatSideEffect.Error(it)) }
                }

                reduce { state.copy(isLoading = false) }
            }

        fun onInputChange(value: String) =
            intent {
                reduce { state.copy(input = value.take(SendChatMessageUseCase.MAX_CONTENT_LENGTH)) }
            }

        fun onSendClick() =
            intent {
                val content = state.input
                reduce { state.copy(input = "") }
                send(content)
            }

        fun onSuggestionClick(seedPrompt: String) = intent { send(seedPrompt) }

        fun startNewConversation() =
            intent {
                reduce {
                    state.copy(
                        conversationId = null,
                        messages = persistentListOf(),
                        streamingChatState = StreamingChatState.Idle,
                        input = "",
                    )
                }
            }

        // 전송 진입점이 여러 개이므로 가드를 여기 한 곳에만 둔다 (설계 2-7).
        private suspend fun Syntax<ChatState, ChatSideEffect>.send(content: String) {
            if (state.streamingChatState !is StreamingChatState.Idle) return

            val conversationId = state.conversationId
            val placeholder = localUserMessage(content.trim())
            reduce {
                state.copy(
                    messages = state.messages.adding(placeholder),
                    streamingChatState = StreamingChatState.Thinking,
                )
            }

            var streamConversationId: String? = conversationId
            var assistantMessageId: String? = null
            var pendingAction: ChatAction? = null

            try {
                sendChatMessage(conversationId, content)
                    .transform { result ->
                        when (val event = result.getOrElse { throw it }) {
                            is ChatStreamEvent.Start -> {
                                val result = onStreamingStart(placeholder, event)
                                streamConversationId = result.first
                                assistantMessageId = result.second
                            }

                            is ChatStreamEvent.Delta -> {
                                emit(event.text)
                            }

                            is ChatStreamEvent.Action -> {
                                pendingAction = event.action
                            }

                            is ChatStreamEvent.Done -> {
                                assistantMessageId = event.assistantMessageId
                            }

                            is ChatStreamEvent.Error -> {
                                postSideEffect(ChatSideEffect.ShowStreamingErrorMessage(event.message))
                            }
                        }
                    }.typewriter()
                    .collect { shown ->
                        reduce {
                            state.copy(
                                streamingChatState = StreamingChatState.Typing(shown),
                            )
                        }
                    }

                onSteamingDone(
                    streamConversationId,
                    assistantMessageId,
                    pendingAction,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                reduce {
                    state.copy(
                        streamingChatState = StreamingChatState.Idle,
                    )
                }
                postSideEffect(ChatSideEffect.Error(e))
            }
        }

        private suspend fun Syntax<ChatState, ChatSideEffect>.onStreamingStart(
            placeholder: ChatMessage,
            event: ChatStreamEvent.Start,
        ): Pair<String, String> {
            reduce {
                state.copy(
                    // 낙관적 메시지의 로컬 id를 서버가 준 진짜 id로 교체.
                    messages =
                        state.messages
                            .map { msg ->
                                if (msg.id == placeholder.id) {
                                    msg.copy(
                                        id = event.userMessageId,
                                        status = MessageStatus.COMPLETED,
                                    )
                                } else {
                                    msg
                                }
                            }.toPersistentList(),
                    quota = event.quota,
                )
            }
            return event.conversationId to event.assistantMessageId
        }

        private suspend fun Syntax<ChatState, ChatSideEffect>.onSteamingDone(
            streamConversationId: String?,
            assistantMessageId: String?,
            pendingAction: ChatAction?,
        ) {
            reduce {
                val currentStreamingState = state.streamingChatState
                state.copy(
                    conversationId = streamConversationId,
                    messages =
                        if (currentStreamingState is StreamingChatState.Typing) {
                            state.messages.adding(
                                assistantMessage(
                                    id = assistantMessageId,
                                    content = currentStreamingState.streamingText,
                                    action = pendingAction,
                                ),
                            )
                        } else {
                            state.messages
                        },
                    streamingChatState = StreamingChatState.Idle,
                )
            }
        }
    }

private fun localUserMessage(content: String) =
    ChatMessage(
        id = "local-user-${System.currentTimeMillis()}",
        role = MessageRole.USER,
        content = content,
        status = MessageStatus.COMPLETED,
        action = null,
        createdAt = Instant.now(),
    )

/**
 * 서버는 done에 텍스트/시각을 싣지 않으므로(assistantMessageId만 전달) 최종 메시지는 여기서 조립한다.
 * id는 start/done이 준 값을 쓰되, start도 못 받고 스트림이 끝난 경우를 대비해 로컬 id로 폴백한다.
 */
private fun assistantMessage(
    id: String?,
    content: String,
    action: ChatAction?,
) = ChatMessage(
    id = id ?: "local-assistant-${System.currentTimeMillis()}",
    role = MessageRole.ASSISTANT,
    content = content,
    status = MessageStatus.COMPLETED,
    action = action,
    createdAt = Instant.now(),
)
