package com.kikidan.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kikidan.chat.model.ChatSideEffect
import com.kikidan.chat.model.ChatState
import com.kikidan.chat.model.StreamingChatState
import com.kikidan.domain.model.chat.ChatAction
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import com.kikidan.domain.usecase.GetChatEntryUseCase
import com.kikidan.domain.usecase.GetConversationDetailUseCase
import com.kikidan.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
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
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel(),
        ContainerHost<ChatState, ChatSideEffect> {
        override val container =
            container<ChatState, ChatSideEffect>(
                ChatState(
                    conversationId = savedStateHandle[KEY_CONVERSATION_ID],
                    input = savedStateHandle[KEY_INPUT] ?: "",
                ),
            )

        /** 화면 진입 시 1회. conversationId가 있으면 과거 대화를 먼저 채운다. */
        fun load(conversationId: String?) =
            intent {
                savedStateHandle[KEY_CONVERSATION_ID] = conversationId
                reduce { state.copy(conversationId = conversationId, isLoading = true) }
                val startedAt = System.currentTimeMillis()

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

                // 로딩 화면이 너무 빨리 깜빡이지 않도록 최소 노출 시간을 보장한다.
                val elapsed = System.currentTimeMillis() - startedAt
                if (elapsed < MIN_LOADING_DURATION_MILLIS) {
                    delay(MIN_LOADING_DURATION_MILLIS - elapsed)
                }
                reduce { state.copy(isLoading = false) }
            }

        fun onInputChange(value: String) =
            intent {
                val trimmedToLimit = value.take(SendChatMessageUseCase.MAX_CONTENT_LENGTH)
                savedStateHandle[KEY_INPUT] = trimmedToLimit
                reduce { state.copy(input = trimmedToLimit) }
            }

        fun onSendClick() = intent { send(state.input) }

        // 카테고리 선택용 chip이므로 실제 AI 호출 없이 질문/카테고리별 고정 답변을 즉시 보여준다.
        fun onSuggestionClick(
            suggestion: ChatSuggestion,
            answer: String,
        ) = intent {
            reduce {
                state.copy(
                    messages =
                        state.messages
                            .adding(localUserMessage(suggestion.seedPrompt).copy(status = MessageStatus.COMPLETED))
                            .adding(assistantMessage(id = null, content = answer, action = null)),
                )
            }
        }

        fun startNewConversation() =
            intent {
                savedStateHandle[KEY_CONVERSATION_ID] = null
                savedStateHandle[KEY_INPUT] = ""
                reduce {
                    state.copy(
                        conversationId = null,
                        messages = persistentListOf(),
                        streamingChatState = StreamingChatState.Idle,
                        input = "",
                    )
                }
            }

        // 전송 진입점이 여러 개이므로 가드와 정규화(trim/공백 체크)를 여기 한 곳에만 둔다 (설계 2-7).
        private suspend fun Syntax<ChatState, ChatSideEffect>.send(content: String) {
            if (state.streamingChatState !is StreamingChatState.Idle) return
            val trimmed = content.trim()
            if (trimmed.isBlank()) return

            val conversationId = state.conversationId
            val placeholder = localUserMessage(trimmed)
            savedStateHandle[KEY_INPUT] = ""
            reduce {
                state.copy(
                    messages = state.messages.adding(placeholder),
                    streamingChatState = StreamingChatState.Thinking,
                    input = "",
                )
            }

            var streamConversationId: String? = conversationId
            var assistantMessageId: String? = null
            var userMessageId: String = placeholder.id
            var pendingAction: ChatAction? = null

            try {
                sendChatMessage(conversationId, trimmed)
                    .transform { result ->
                        when (val event = result.getOrElse { throw it }) {
                            is ChatStreamEvent.Start -> {
                                val result = onStreamingStart(placeholder, event)
                                streamConversationId = result.first
                                assistantMessageId = result.second
                                userMessageId = event.userMessageId
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
                        messages =
                            state.messages
                                .map { msg ->
                                    if (msg.id == userMessageId) msg.copy(status = MessageStatus.FAILED) else msg
                                }.toPersistentList(),
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
            savedStateHandle[KEY_CONVERSATION_ID] = streamConversationId
        }

        companion object {
            private const val KEY_CONVERSATION_ID = "conversationId"
            private const val KEY_INPUT = "input"
            private const val MIN_LOADING_DURATION_MILLIS = 1_500L
        }
    }

private fun localUserMessage(content: String) =
    ChatMessage(
        id = "local-user-${System.currentTimeMillis()}",
        role = MessageRole.USER,
        content = content,
        status = MessageStatus.GENERATING,
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
