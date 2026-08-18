package com.kikidan.data_remote.dto.notification

import com.kikidan.domain.model.notification.NotificationSetting
import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class NotificationSettingResponse(
    val morningReportEnabled: Boolean,
    val morningReportTime: LocalTimeResponse,
    val todakiEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
    val osPushPermission: Boolean? = null,
)

@Serializable
data class LocalTimeResponse(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val nano: Int = 0,
)

@Serializable
data class UpdateNotificationSettingRequest(
    val morningReportEnabled: Boolean,
    val morningReportTime: String,
    val todakiEnabled: Boolean,
    val luckyActionReminderEnabled: Boolean,
)

internal fun NotificationSettingResponse.toDomain(): NotificationSetting =
    NotificationSetting(
        morningReportEnabled = morningReportEnabled,
        morningReportTime =
            LocalTime.of(
                morningReportTime.hour,
                morningReportTime.minute,
                morningReportTime.second,
                morningReportTime.nano,
            ),
        todakiEnabled = todakiEnabled,
        luckyActionReminderEnabled = luckyActionReminderEnabled,
    )

internal fun NotificationSetting.toUpdateRequest(): UpdateNotificationSettingRequest =
    UpdateNotificationSettingRequest(
        morningReportEnabled = morningReportEnabled,
        morningReportTime = morningReportTime.format(DateTimeFormatter.ofPattern("HH:mm")),
        todakiEnabled = todakiEnabled,
        luckyActionReminderEnabled = luckyActionReminderEnabled,
    )
