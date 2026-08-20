package com.kikidan.notification.model

import kotlinx.collections.immutable.PersistentList

sealed interface NotificationState {
    data object Loading : NotificationState

    data class Success(
        val notifications: PersistentList<NotificationUiModel>,
    ) : NotificationState

    data object Failure : NotificationState
}
