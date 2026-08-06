package com.kikidan.chat

import com.kikidan.chat.model.ChatSideEffect
import com.kikidan.chat.model.ChatState
import com.kikidan.chat.model.StreamingChatState
import com.kikidan.domain.model.chat.ChatAction
import com.kikidan.domain.model.chat.ChatActionType
import com.kikidan.domain.model.chat.ChatCategory
import com.kikidan.domain.model.chat.ChatEntry
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatStreamEvent
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.Conversation
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import com.kikidan.domain.usecase.GetChatEntryUseCase
import com.kikidan.domain.usecase.GetConversationDetailUseCase
import com.kikidan.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.Instant

@Suppress("ktlint:standard:max-line-length")
class ChatViewModelTest {
    @Test
    fun `load null + entry 성공 시 suggestions quota greeting 반영된다`() =
        runTest {
            val fakeRepo = FakeChatRepository().apply { chatEntryResult = Result.success(defaultEntry) }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.load(null)
                // 첫 reduce: copy(conversationId=null, isLoading=true) → 초기 상태와 동일, 미방출
                // getChatEntry 성공 → copy(greeting, suggestions, quota) 변경
                val s1 = awaitState()
                assertEquals(defaultEntry.greeting, s1.greeting)
                assertEquals(defaultEntry.suggestions, s1.suggestions)
                assertEquals(defaultEntry.quota, s1.quota)
                assertTrue(s1.isLoading)
                // 최종 reduce: isLoading=false
                val s2 = awaitState()
                assertFalse(s2.isLoading)
                assertNull(s2.conversationId)
            }
        }

    @Test
    fun `load null + entry 실패 시 Error 사이드이펙트, isLoading = false`() =
        runTest {
            val fakeRepo = FakeChatRepository()
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.load(null)
                // onFailure → postSideEffect(SE), then reduce isLoading=false
                val se = awaitSideEffect()
                assertTrue(se is ChatSideEffect.Error)
                val s = awaitState()
                assertFalse(s.isLoading)
            }
        }

    @Test
    fun `load 기존 conversationId 시 detail 메시지가 messages에 채워진다`() =
        runTest {
            val msgs = listOf(message("m1", "hi"), message("m2", "hello", MessageRole.ASSISTANT))
            val fakeRepo =
                FakeChatRepository().apply {
                    chatEntryResult = Result.success(defaultEntry)
                    conversationDetailResult = Result.success(conversation("c-1", msgs))
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.load("c-1")
                // 1: conversationId="c-1" 설정 (변경)
                val s1 = awaitState()
                assertEquals("c-1", s1.conversationId)
                // 2: getChatEntry 성공
                awaitState()
                // 3: getConversationDetail 성공 → messages
                awaitState()
                // 4: isLoading=false
                val s4 = awaitState()
                assertFalse(s4.isLoading)
                assertEquals("c-1", s4.conversationId)
                assertEquals(msgs, s4.messages)
            }
        }

    @Test
    fun `send 후 사용자 메시지가 즉시 messages에 추가된다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c1", "u1", "a1", defaultEntry.quota)),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("안녕")
                // 첫 상태: THINKING + user msg Completed
                val s1 = awaitState()
                assertEquals(StreamingChatState.Thinking, s1.streamingChatState)
                assertTrue(s1.messages.isNotEmpty())
                assertEquals("안녕", s1.messages.first().content)
                assertEquals(MessageRole.USER, s1.messages.first().role)
                assertEquals(MessageStatus.COMPLETED, s1.messages.first().status)
                cancelAndIgnoreRemainingItems()
            }
        }

    @Test
    fun `Delta 수신 시, streamingText 가 단조 증가한다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c1", "u1", "a1", defaultEntry.quota)),
                            Result.success(ChatStreamEvent.Delta("안녕하세요")),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("질문")
                val typingStates = mutableListOf<ChatState>()
                var s = awaitState()
                while (s.streamingChatState != StreamingChatState.Idle) {
                    if (s.streamingChatState is StreamingChatState.Typing) typingStates.add(s)
                    s = awaitState()
                }
                assertTrue("TYPING 상태가 존재해야 함", typingStates.isNotEmpty())
                for (i in 1 until typingStates.size) {
                    val chatState = typingStates[i - 1].streamingChatState as StreamingChatState.Typing
                    val currentChatState = typingStates[i].streamingChatState as StreamingChatState.Typing
                    assertTrue(
                        "streamingText 단조 증가 실패: '$chatState' → '$chatState'",
                        currentChatState.streamingText.startsWith(chatState.streamingText),
                    )
                }
            }
        }

    @Test
    fun `Done 수신 시 messages 마지막이 assistantMessageId로 COMPLETED 상태의 ASSISTANT 메시지가 된다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(
                                ChatStreamEvent.Start("c1", "u1", "a1", defaultEntry.quota),
                            ),
                            Result.success(ChatStreamEvent.Delta("응답 텍스트")),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("질문")
                var s = awaitState()
                while (s.streamingChatState != StreamingChatState.Idle) s = awaitState()
                assertEquals(StreamingChatState.Idle, s.streamingChatState)
                val assistant = s.messages.last()
                assertEquals("a1", assistant.id)
                assertEquals(MessageRole.ASSISTANT, assistant.role)
                assertEquals(MessageStatus.COMPLETED, assistant.status)
                assertEquals("응답 텍스트", assistant.content)
            }
        }

    @Test
    fun `Start의 conversationId가 저장되어 두 번째 send 시 Fake에 전달된다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c-from-server", "u1", "a1", defaultEntry.quota)),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("첫 번째")
                var s = awaitState()
                while (s.streamingChatState != StreamingChatState.Idle) s = awaitState()
                assertEquals("c-from-server", s.conversationId)

                fakeRepo.streamEvents =
                    listOf(
                        Result.success(ChatStreamEvent.Start("c-from-server", "u2", "a2", defaultEntry.quota)),
                        Result.success(ChatStreamEvent.Done("a2")),
                    )

                containerHost.onSuggestionClick("두 번째")
                s = awaitState()
                while (s.streamingChatState !is StreamingChatState.Idle) s = awaitState()

                assertEquals("c-from-server", fakeRepo.lastSentConversationId)
                assertEquals(2, fakeRepo.sendCallCount)
            }
        }

    @Test
    fun `Start의 userMessageId로 낙관적 사용자 메시지 id 가 교체된다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c1", "real-user-id", "a1", defaultEntry.quota)),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("안녕")
                val s1 = awaitState()
                assertEquals(MessageStatus.COMPLETED, s1.messages.first().status)
                // S2: Start 수신 → id가 real-user-id로 교체, COMPLETED
                val s2 = awaitState()
                assertTrue(s2.messages.any { it.id == "real-user-id" && it.status == MessageStatus.COMPLETED })
                cancelAndIgnoreRemainingItems()
            }
        }

    @Test
    fun `Start 수신 직후 quota 가 즉시 반영된다`() =
        runTest {
            val updatedQuota = ChatQuota(used = 2, limit = 10)
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c1", "u1", "a1", updatedQuota)),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("안녕")
                // S1: THINKING/PENDING
                awaitState()
                // S2: Start 수신 → quota 즉시 반영
                val s2 = awaitState()
                assertEquals(updatedQuota, s2.quota)
                cancelAndIgnoreRemainingItems()
            }
        }

    @Test
    fun `Action 이벤트의 action 이 최종 어시스턴트 메시지에 포함된다`() =
        runTest {
            val action =
                ChatAction(type = ChatActionType.CALENDAR_ADD, label = "일기 쓰기", category = "record", date = null)
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c1", "u1", "a1", defaultEntry.quota)),
                            Result.success(ChatStreamEvent.Delta("텍스트")),
                            Result.success(ChatStreamEvent.Action(action)),
                            Result.success(ChatStreamEvent.Done("a1")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("안녕")
                var s = awaitState()
                while (s.streamingChatState !is StreamingChatState.Idle) s = awaitState()
                val assistant = s.messages.last()
                assertEquals(action, assistant.action)
            }
        }

    @Test
    fun `Start 없이 Delta만 오고 스트림 종료 시 local-assistant 폴백 id 로 어시스턴트 메시지가 추가된다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Delta("텍스트만")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("안녕")
                var s = awaitState()
                while (s.streamingChatState !is StreamingChatState.Idle) s = awaitState()
                val assistant = s.messages.find { it.role == MessageRole.ASSISTANT }
                assertNotNull(assistant)
                assertTrue("폴백 id 사용: ${assistant!!.id}", assistant.id.startsWith("local-assistant-"))
                assertEquals("텍스트만", assistant.content)
            }
        }

    @Test
    fun `스트림이 Result failure 방출 시 ShowMessage 이벤트가 방출된다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Start("c1", "u1", "a1", defaultEntry.quota)),
                            Result.failure(IllegalStateException()),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("안녕")
                awaitState() // S1: THINKING/PENDING
                awaitState() // S2: Start 수신 후 THINKING/COMPLETED
                // catch 블록: reduce(IDLE) → SE 순서
                val s = awaitState()
                assertEquals(StreamingChatState.Idle, s.streamingChatState)
                val se = awaitSideEffect()
                assertEquals(IllegalStateException::class, (se as ChatSideEffect.Error).e::class)
            }
        }

    @Test
    fun `스트리밍 중 send 재호출은 무시되며 Fake 호출 횟수가 1을 유지한다`() =
        runTest {
            val fakeRepo =
                FakeChatRepository().apply {
                    streamEvents =
                        listOf(
                            Result.success(ChatStreamEvent.Delta("텍스트")),
                        )
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onSuggestionClick("첫 번째")
                // 첫 상태: THINKING (phase != IDLE)
                val s1 = awaitState()
                assertTrue("THINKING 상태여야 함", s1.streamingChatState != StreamingChatState.Idle)

                // 스트리밍 중 두 번째 send → phase != IDLE이므로 send() 가드에서 즉시 return
                containerHost.onSuggestionClick("두 번째")

                // 나머지 상태 소비
                var s = awaitState()
                while (s.streamingChatState !is StreamingChatState.Idle) s = awaitState()

                // sendChatMessage 호출은 첫 번째 1회뿐
                assertEquals(1, fakeRepo.sendCallCount)
            }
        }

    @Test
    fun `onInputChange에 501자 입력 시 input length가 500으로 제한된다`() =
        runTest {
            val fakeRepo = FakeChatRepository()
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.onInputChange("a".repeat(501))
                val s = awaitState()
                assertEquals(500, s.input.length)
            }
        }

    @Test
    fun `startNewConversation 호출 시 conversationId = null, messages 비워짐, suggestions 유지`() =
        runTest {
            val msgs = listOf(message("m1", "hi"))
            val fakeRepo =
                FakeChatRepository().apply {
                    chatEntryResult = Result.success(defaultEntry)
                    conversationDetailResult = Result.success(conversation("c-1", msgs))
                }
            val vm = viewModel(fakeRepo)

            vm.test(this) {
                containerHost.load("c-1")
                awaitState() // conversationId 설정
                awaitState() // entry 로드
                awaitState() // messages 로드
                val afterLoad = awaitState() // isLoading=false
                assertEquals("c-1", afterLoad.conversationId)
                assertEquals(msgs, afterLoad.messages)
                assertEquals(defaultEntry.suggestions, afterLoad.suggestions)

                containerHost.startNewConversation()
                val afterNew = awaitState()
                assertNull(afterNew.conversationId)
                assertTrue(afterNew.messages.isEmpty())
                assertEquals(StreamingChatState.Idle, afterNew.streamingChatState)
                assertEquals(defaultEntry.suggestions, afterNew.suggestions)
            }
        }

    private fun viewModel(fakeRepo: FakeChatRepository): ChatViewModel =
        ChatViewModel(
            getChatEntry = GetChatEntryUseCase(fakeRepo),
            getConversationDetail = GetConversationDetailUseCase(fakeRepo),
            sendChatMessage = SendChatMessageUseCase(fakeRepo),
        )

    private val defaultEntry =
        ChatEntry(
            greeting = "안녕하세요",
            suggestions = listOf(ChatSuggestion("😊", "label", "seed", ChatCategory.LOVE)),
            quota = ChatQuota(used = 1, limit = 10),
        )

    private fun conversation(
        id: String,
        messages: List<ChatMessage>,
    ) = Conversation(id = id, title = "대화", messages = messages)

    private fun message(
        id: String,
        content: String,
        role: MessageRole = MessageRole.USER,
    ) = ChatMessage(
        id = id,
        role = role,
        content = content,
        status = MessageStatus.COMPLETED,
        action = null,
        createdAt = Instant.now(),
    )
}
