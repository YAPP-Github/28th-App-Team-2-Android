package com.kikidan.mypage.notification.model

data class NotificationSettingUiModel(
    val morningReportEnabled: Boolean,
    val morningReportHour: Int,
    val morningReportMinute: Int,
    val todakAlarmEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
)
