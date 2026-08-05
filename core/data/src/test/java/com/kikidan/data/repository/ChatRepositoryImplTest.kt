package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteChatDataSource
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatStreamEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ChatRepositoryImplTest {
    private lateinit var fake: FakeRemoteChatDataSource
    private lateinit var sut: ChatRepositoryImpl

    private val delta1 = ChatStreamEvent.Delta("안")
    private val delta2 = ChatStreamEvent.Delta("녕")

    @Before
    fun setUp() {
        fake = FakeRemoteChatDataSource()
        sut = ChatRepositoryImpl(fake)
    }

    @Test
    fun `getChatEntry가_성공하면_Result_success로_반환된다`() =
        runTest {
            val expected = fake.chatEntry
            val result = sut.getChatEntry()
            assertEquals(Result.success(expected), result)
        }

    @Test
    fun `getChatEntry가_IOException을_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            fake.throwOnGetChatEntry = IOException("network")

            val result = sut.getChatEntry()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `getChatEntry가_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            fake.throwOnGetChatEntry = CancellationException("cancelled")
            sut.getChatEntry()
        }

    @Test
    fun `sendMessage가_이벤트_3개를_정상_방출하면_Result_success_3개가_수집된다`() =
        runTest {
            val start = ChatStreamEvent.Start("c-1", "u-1", "a-1", ChatQuota(1, 5))
            fake.streamEvents = listOf(start, delta1, delta2)

            val results = sut.sendMessage(null, "test").toList()

            assertEquals(3, results.size)
            assertTrue(results.all { it.isSuccess })
            assertEquals(start, results[0].getOrNull())
            assertEquals(delta1, results[1].getOrNull())
            assertEquals(delta2, results[2].getOrNull())
        }

    @Test
    fun `sendMessage가_이벤트_2개_후_IOException을_throw하면_success_2개_후_failure_1개가_방출된다`() =
        runTest {
            fake.streamEvents = listOf(delta1, delta2)
            fake.streamThrowable = IOException("disconnected")

            val results = sut.sendMessage(null, "test").toList()

            assertEquals(3, results.size)
            assertTrue(results[0].isSuccess)
            assertTrue(results[1].isSuccess)
            assertTrue(results[2].isFailure)
            assertTrue(results[2].exceptionOrNull() is IOException)
        }

    @Test
    fun `sendMessage_collect_취소시_예외_없이_종료되고_Result_failure가_방출되지_않는다`() =
        runTest {
            fake.streamEvents = listOf(delta1, delta2, ChatStreamEvent.Delta("하"))

            // take(1)은 첫 이벤트만 수집하고 업스트림을 취소한다.
            // CancellationException은 catch에서 재throw되므로 Result.failure가 방출되지 않는다.
            val results = sut.sendMessage(null, "test").take(1).toList()

            assertEquals(1, results.size)
            assertTrue(results[0].isSuccess)
        }

    @Test
    fun `deleteConversation이_실패하면_Result_failure로_반환된다`() =
        runTest {
            fake.throwOnDeleteConversation = IOException("forbidden")

            val result = sut.deleteConversation("c-1")

            assertTrue(result.isFailure)
        }
}
