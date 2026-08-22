package com.kikidan.notification.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class NotificationState(
    val isLoading: Boolean = true,
    val notifications: PersistentList<NotificationUiModel> = persistentListOf(),
)
