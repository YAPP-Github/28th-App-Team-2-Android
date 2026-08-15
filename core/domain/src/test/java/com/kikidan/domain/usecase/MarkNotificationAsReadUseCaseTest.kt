package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeNotificationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarkNotificationAsReadUseCaseTest {
    private lateinit var fakeNotificationRepository: FakeNotificationRepository
    private lateinit var useCase: MarkNotificationAsReadUseCase

    @Before
    fun setUp() {
        fakeNotificationRepository = FakeNotificationRepository()
        useCase = MarkNotificationAsReadUseCase(fakeNotificationRepository)
    }

    @Test
    fun `성공 시 요청한 notificationId로 위임한다`() =
        runTest {
            val result = useCase("n-1")

            assertEquals("n-1", fakeNotificationRepository.lastMarkedAsReadId)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `실패 시 Result failure를 그대로 전파한다`() =
        runTest {
            val error = IllegalStateException("not found")
            fakeNotificationRepository.markAsReadResult = Result.failure(error)

            val result = useCase("n-1")

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
