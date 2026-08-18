package com.kikidan.notification

import androidx.lifecycle.ViewModel
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
        override val container = container<NotificationState, NotificationSideEffect>(NotificationState())

        fun load() =
            intent {
                reduce { state.copy(isLoading = true) }
                getNotifications()
                    .onSuccess { summary ->
                        reduce {
                            state.copy(
                                isLoading = false,
                                notifications = summary.notifications.map { it.toUiModel() }.toPersistentList(),
                            )
                        }
                    }.onFailure {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(NotificationSideEffect.Error(it))
                    }
            }

        fun onNotificationClick(notificationId: String) =
            intent {
                markNotificationAsRead(notificationId)
                    .onSuccess {
                        reduce {
                            state.copy(
                                notifications =
                                    state.notifications
                                        .map { if (it.id == notificationId) it.copy(isRead = true) else it }
                                        .toPersistentList(),
                            )
                        }
                    }.onFailure { postSideEffect(NotificationSideEffect.Error(it)) }
            }
    }
