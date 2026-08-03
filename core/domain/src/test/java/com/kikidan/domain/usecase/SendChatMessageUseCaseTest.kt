package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeChatRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SendChatMessageUseCaseTest {
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var useCase: SendChatMessageUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeChatRepository()
        useCase = SendChatMessageUseCase(fakeRepository)
    }

    @Test
    fun `정상 content 전송 시 Repository Flow가 그대로 전달된다`() = runTest {
        val results = useCase(conversationId = null, content = "안녕하세요").toList()
        assertEquals(1, fakeRepository.sendMessageCallCount)
        assertEquals("안녕하세요", fakeRepository.lastSentContent)
    }

    @Test
    fun `content가 빈 문자열이면 Repository 호출 없이 failure를 방출한다`() = runTest {
        val results = useCase(conversationId = null, content = "").toList()
        assertEquals(0, fakeRepository.sendMessageCallCount)
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertTrue(results[0].exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `content가 공백만이면 Repository 호출 없이 failure를 방출한다`() = runTest {
        val results = useCase(conversationId = null, content = "   ").toList()
        assertEquals(0, fakeRepository.sendMessageCallCount)
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertTrue(results[0].exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `content가 501자이면 Repository 호출 없이 failure를 방출한다`() = runTest {
        val content = "a".repeat(SendChatMessageUseCase.MAX_CONTENT_LENGTH + 1)
        val results = useCase(conversationId = null, content = content).toList()
        assertEquals(0, fakeRepository.sendMessageCallCount)
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertTrue(results[0].exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `content 앞뒤 공백은 trim되어 Repository에 전달된다`() = runTest {
        useCase(conversationId = null, content = " 안녕 ").toList()
        assertEquals(1, fakeRepository.sendMessageCallCount)
        assertEquals("안녕", fakeRepository.lastSentContent)
    }

    @Test
    fun `content가 정확히 500자이면 정상 통과한다`() = runTest {
        val content = "a".repeat(SendChatMessageUseCase.MAX_CONTENT_LENGTH)
        useCase(conversationId = null, content = content).toList()
        assertEquals(1, fakeRepository.sendMessageCallCount)
        assertEquals(content, fakeRepository.lastSentContent)
    }
}
