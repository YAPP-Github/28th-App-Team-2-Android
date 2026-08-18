package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteNotificationDataSource
import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.model.notification.NotificationType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant

class NotificationRepositoryImplTest {
    private lateinit var fake: FakeRemoteNotificationDataSource
    private lateinit var sut: NotificationRepositoryImpl

    @Before
    fun setUp() {
        fake = FakeRemoteNotificationDataSource()
        sut = NotificationRepositoryImpl(fake)
    }

    @Test
    fun `getNotifications가 성공하면 Result success로 반환된다`() =
        runTest {
            val summary =
                NotificationSummary(
                    unreadCount = 1,
                    notifications =
                        listOf(
                            Notification(
                                id = "n-1",
                                type = NotificationType.FORTUNE,
                                title = "제목",
                                content = "내용",
                                deepLink = "",
                                isRead = false,
                                createdAt = Instant.now(),
                            ),
                        ),
                )
            fake.notifications = summary

            val result = sut.getNotifications()

            assertEquals(Result.success(summary), result)
        }

    @Test
    fun `getNotifications가 IOException을 throw하면 Result failure로 반환된다`() =
        runTest {
            fake.throwOnGetNotifications = IOException("network")

            val result = sut.getNotifications()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `getNotifications가 CancellationException을 throw하면 그대로 전파된다`() =
        runTest {
            fake.throwOnGetNotifications = CancellationException("cancelled")
            sut.getNotifications()
        }

    @Test
    fun `markAsRead가 성공하면 요청한 id로 위임하고 Result success를 반환한다`() =
        runTest {
            val result = sut.markAsRead("n-1")

            assertEquals("n-1", fake.lastMarkedAsReadId)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `markAsRead가 IOException을 throw하면 Result failure로 반환된다`() =
        runTest {
            fake.throwOnMarkAsRead = IOException("network")

            val result = sut.markAsRead("n-1")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }
}
