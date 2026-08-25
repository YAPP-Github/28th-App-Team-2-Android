package com.kikidan.notification

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.notification.Notification
import com.kikidan.domain.usecase.GetNotificationsUseCase
import com.kikidan.domain.usecase.MarkNotificationAsReadUseCase
import com.kikidan.notification.model.NotificationSideEffect
import com.kikidan.notification.model.NotificationState
import com.kikidan.notification.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel
    @Inject
    constructor(
        private val getNotifications: GetNotificationsUseCase,
        private val markNotificationAsRead: MarkNotificationAsReadUseCase,
    ) : ViewModel(),
        ContainerHost<NotificationState, NotificationSideEffect> {
        override val container = container<NotificationState, NotificationSideEffect>(NotificationState.Loading)

        // 화면 표시용 NotificationUiModel에는 deepLink가 없어 클릭 시 원본 목록에서 조회한다.
        private var notifications: List<Notification> = emptyList()

        fun load() =
            intent {
                reduce { NotificationState.Loading }
                getNotifications()
                    .onSuccess { summary ->
                        notifications = summary.notifications
                        reduce {
                            NotificationState.Success(summary.notifications.map { it.toUiModel() }.toPersistentList())
                        }
                    }.onFailure {
                        reduce { NotificationState.Failure }
                        postSideEffect(NotificationSideEffect.Error(it))
                    }
            }

        fun onNotificationClick(notificationId: String) =
            intent {
                val current = (state as? NotificationState.Success)?.notifications ?: return@intent
                markNotificationAsRead(notificationId)
                    .onSuccess {
                        reduce {
                            NotificationState.Success(
                                current
                                    .map { if (it.id == notificationId) it.copy(isRead = true) else it }
                                    .toPersistentList(),
                            )
                        }
                        notifications
                            .firstOrNull { it.id == notificationId }
                            ?.deepLink
                            ?.let { deepLink -> postSideEffect(NotificationSideEffect.NavigateToDeepLink(deepLink)) }
                    }.onFailure { postSideEffect(NotificationSideEffect.Error(it)) }
            }
    }
