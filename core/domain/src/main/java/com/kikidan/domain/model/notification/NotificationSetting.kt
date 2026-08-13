package com.kikidan.domain.model.notification

import java.time.LocalTime

data class NotificationSetting(
    val morningReportEnabled: Boolean,
    val morningReportTime: LocalTime,
    val todakiEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
)
