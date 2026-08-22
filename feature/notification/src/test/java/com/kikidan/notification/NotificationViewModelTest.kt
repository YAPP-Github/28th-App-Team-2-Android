package com.kikidan.notification

import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.model.notification.NotificationSummary
import com.kikidan.domain.model.notification.NotificationType
import com.kikidan.domain.usecase.GetNotificationsUseCase
import com.kikidan.domain.usecase.MarkNotificationAsReadUseCase
import com.kikidan.notification.model.NotificationSideEffect
import com.kikidan.notification.model.NotificationState
import com.kikidan.notification.model.NotificationUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.test
import java.time.Instant

class NotificationViewModelTest {
    @Test
    fun `load 성공 시 notifications가 채워지고 isLoading이 false가 된다`() =
        runTest {
            val fakeNotificationRepository =
                FakeNotificationRepository().apply {
                    notificationsResult =
                        Result.success(
                            NotificationSummary(
                                unreadCount = 1,
                                notifications =
                                    listOf(
                                        Notification(
                                            id = "n-1",
                                            type = NotificationType.FORTUNE,
                                            title = "오늘의 운세가 도착했어요.",
                                            content = "내용",
                                            deepLink = "",
                                            isRead = false,
                                            createdAt = Instant.now(),
                                        ),
                                    ),
                            ),
                        )
                }
            val vm = viewModel(fakeNotificationRepository)

            vm.test(this) {
                containerHost.load()
                val success = awaitState()
                assertFalse(success.isLoading)
                assertEquals(1, success.notifications.size)
                assertEquals("n-1", success.notifications[0].id)
                assertFalse(success.notifications[0].isRead)
            }
        }

    @Test
    fun `load 실패 시 isLoading이 false가 되고 Error 사이드이펙트가 발행된다`() =
        runTest {
            val fakeNotificationRepository =
                FakeNotificationRepository().apply {
                    notificationsResult = Result.failure(IllegalStateException("network error"))
                }
            val vm = viewModel(fakeNotificationRepository)

            vm.test(this) {
                containerHost.load()
                val failure = awaitState()
                assertFalse(failure.isLoading)
                val se = awaitSideEffect()
                assertTrue(se is NotificationSideEffect.Error)
            }
        }

    @Test
    fun `onNotificationClick 성공 시 해당 알림만 isRead true로 바뀐다`() =
        runTest {
            val fakeNotificationRepository = FakeNotificationRepository()
            val vm = viewModel(fakeNotificationRepository)
            val initial =
                NotificationState(
                    isLoading = false,
                    notifications =
                        persistentListOf(
                            NotificationUiModel("n-1", NotificationType.FORTUNE, "제목1", "30분 전", false),
                            NotificationUiModel("n-2", NotificationType.LUCKY_ACTION, "제목2", "3시간 전", false),
                        ),
                )

            vm.test(this, initialState = initial) {
                containerHost.onNotificationClick("n-1")
                val settled = awaitState()
                assertEquals("n-1", fakeNotificationRepository.lastMarkedAsReadId)
                assertTrue(settled.notifications.first { it.id == "n-1" }.isRead)
                assertFalse(settled.notifications.first { it.id == "n-2" }.isRead)
            }
        }

    @Test
    fun `onNotificationClick 성공 시 해당 알림의 deepLink로 NavigateToDeepLink가 발행된다`() =
        runTest {
            val fakeNotificationRepository =
                FakeNotificationRepository().apply {
                    notificationsResult =
                        Result.success(
                            NotificationSummary(
                                unreadCount = 1,
                                notifications =
                                    listOf(
                                        Notification(
                                            id = "n-1",
                                            type = NotificationType.FORTUNE,
                                            title = "오늘의 운세가 도착했어요.",
                                            content = "내용",
                                            deepLink = "todakun://fortune/today",
                                            isRead = false,
                                            createdAt = Instant.now(),
                                        ),
                                    ),
                            ),
                        )
                }
            val vm = viewModel(fakeNotificationRepository)

            vm.test(this) {
                containerHost.load()
                awaitState()

                containerHost.onNotificationClick("n-1")
                awaitState()
                val sideEffect = awaitSideEffect()
                assertTrue(sideEffect is NotificationSideEffect.NavigateToDeepLink)
                assertEquals(
                    "todakun://fortune/today",
                    (sideEffect as NotificationSideEffect.NavigateToDeepLink).deepLink,
                )
            }
        }

    private fun viewModel(fakeNotificationRepository: FakeNotificationRepository): NotificationViewModel =
        NotificationViewModel(
            getNotifications = GetNotificationsUseCase(fakeNotificationRepository),
            markNotificationAsRead = MarkNotificationAsReadUseCase(fakeNotificationRepository),
        )
}
