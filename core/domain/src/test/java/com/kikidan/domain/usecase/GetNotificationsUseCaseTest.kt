package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeNotificationRepository
import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.model.notification.NotificationType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetNotificationsUseCaseTest {
    private lateinit var fakeNotificationRepository: FakeNotificationRepository
    private lateinit var useCase: GetNotificationsUseCase

    @Before
    fun setUp() {
        fakeNotificationRepository = FakeNotificationRepository()
        useCase = GetNotificationsUseCase(fakeNotificationRepository)
    }

    @Test
    fun `성공 시 NotificationSummary를 그대로 전달한다`() =
        runTest {
            val expected =
                NotificationSummary(
                    unreadCount = 1,
                    notifications =
                        listOf(
                            Notification(
                                id = "n-1",
                                type = NotificationType.FORTUNE,
                                title = "오늘의 운세가 도착했어요.",
                                content = "내용",
                                deepLink = "todakun://fortune",
                                isRead = false,
                                createdAt = Instant.parse("2026-07-24T00:00:00Z"),
                            ),
                        ),
                )
            fakeNotificationRepository.notificationsResult = Result.success(expected)

            val result = useCase()

            assertTrue(result.isSuccess)
            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `실패 시 Result failure를 그대로 전파한다`() =
        runTest {
            val error = IllegalStateException("network error")
            fakeNotificationRepository.notificationsResult = Result.failure(error)

            val result = useCase()

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
