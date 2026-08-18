package com.kikidan.data_remote.dto.notification

import com.kikidan.domain.model.notification.NotificationSetting
import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class NotificationSettingResponse(
    val morningReportEnabled: Boolean,
    val morningReportTime: String,
    val todakiEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
    val osPushPermission: Boolean? = null,
)

@Serializable
data class UpdateNotificationSettingRequest(
    val morningReportEnabled: Boolean,
    val morningReportTime: String,
    val todakiEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
)

private val MorningReportTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun NotificationSettingResponse.toDomain(): NotificationSetting =
    NotificationSetting(
        morningReportEnabled = morningReportEnabled,
        morningReportTime = LocalTime.parse(morningReportTime, MorningReportTimeFormatter),
        todakiEnabled = todakiEnabled,
        luckyActionReminderEnabled = luckyActionReminderEnabled,
    )

internal fun NotificationSetting.toUpdateRequest(): UpdateNotificationSettingRequest =
    UpdateNotificationSettingRequest(
        morningReportEnabled = morningReportEnabled,
        morningReportTime = morningReportTime.format(MorningReportTimeFormatter),
        todakiEnabled = todakiEnabled,
        luckyActionReminderEnabled = luckyActionReminderEnabled,
    )
