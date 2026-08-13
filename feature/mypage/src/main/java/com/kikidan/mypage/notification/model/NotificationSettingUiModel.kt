package com.kikidan.mypage.notification.model

import java.time.LocalTime

data class NotificationSettingUiModel(
    val morningReportEnabled: Boolean,
    val morningReportTime: LocalTime,
    val todakiEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
)
