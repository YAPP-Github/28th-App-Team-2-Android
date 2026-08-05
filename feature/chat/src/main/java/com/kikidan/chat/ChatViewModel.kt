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
                                suggestions = entry.suggestions,
                                quota = entry.quota,
                            )
                        }
                    }.onFailure { postSideEffect(ChatSideEffect.Error(it)) }

                if (conversationId != null) {
                    getConversationDetail(conversationId)
                        .onSuccess { reduce { state.copy(messages = it.messages) } }
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
                        messages = emptyList(),
                        streamingChatState = StreamingChatState.Idle,
                        input = "",
                    )
                }
            }

        // 전송 진입점이 여러 개이므로 가드를 여기 한 곳에만 둔다 (설계 2-7).
        private suspend fun Syntax<ChatState, ChatSideEffect>.send(content: String) {
            if (state.streamingChatState is StreamingChatState.Idle) return
            if (state.quota?.let { it.remaining <= 0 } == true) {
                postSideEffect(ChatSideEffect.ShowStreamingErrorMessage(QUOTA_EXHAUSTED_MESSAGE))
                return
            }

            val conversationId = state.conversationId
            val placeholder = localUserMessage(content)
            reduce {
                state.copy(
                    messages = state.messages + placeholder,
                    streamingChatState = StreamingChatState.Thinking,
                )
            }

            var streamConversationId: String? = conversationId
            var assistantMessageId: String? = null
            var pendingAction: ChatAction? = null

            try {
                sendChatMessage(conversationId, content)
                    .transform { result ->
                        // 모든 실패를 예외 한 채널로 되돌린다 (설계 2-3).
                        when (val event = result.getOrElse { throw it }) {
                            is ChatStreamEvent.Start -> {
                                streamConversationId = event.conversationId
                                assistantMessageId = event.assistantMessageId
                                // transform 람다의 수신자는 FlowCollector이므로,
                                // reduce/state는 외부 SimpleSyntax 수신자로 해석된다 (설계 2, 주의 1).
                                reduce {
                                    state.copy(
                                        // 낙관적 메시지의 로컬 id를 서버가 준 진짜 id로 교체 (설계 2-6).
                                        messages =
                                            state.messages.map { msg ->
                                                if (msg.id == placeholder.id) {
                                                    msg.copy(
                                                        id = event.userMessageId,
                                                        status = MessageStatus.COMPLETED,
                                                    )
                                                } else {
                                                    msg
                                                }
                                            },
                                        quota = event.quota,
                                    )
                                }
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

                // typewriter가 완료된 = 버퍼가 다 비워진 시점 (설계 2-2).
                reduce {
                    val currentStreamingState = state.streamingChatState
                    state.copy(
                        conversationId = streamConversationId,
                        messages =
                            if (currentStreamingState is StreamingChatState.Typing) {
                                state.messages +
                                    assistantMessage(
                                        id = assistantMessageId,
                                        content = currentStreamingState.streamingText,
                                        action = pendingAction,
                                    )
                            } else {
                                state.messages
                            },
                        streamingChatState = StreamingChatState.Idle,
                    )
                }
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

        private companion object {
            const val QUOTA_EXHAUSTED_MESSAGE = "오늘 무료 채팅을 모두 사용했어요."
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
