package com.kikidan.domain.model.notification

data class NotificationSetting(
    val morningReportEnabled: Boolean,
    val morningReportHour: Int,
    val morningReportMinute: Int,
    val todakAlarmEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
)
