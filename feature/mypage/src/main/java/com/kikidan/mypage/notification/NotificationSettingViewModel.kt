package com.kikidan.mypage.notification

import androidx.lifecycle.ViewModel
import com.kikidan.mypage.notification.model.NotificationSettingSideEffect
import com.kikidan.mypage.notification.model.NotificationSettingUiModel
import com.kikidan.mypage.notification.model.NotificationSettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

/**
 * 알림 설정 저장/조회 UseCase·API가 아직 없어 목업 상태를 그대로 사용한다.
 * 실제 연동은 Swagger 확인 후 별도 작업 필요.
 */
@HiltViewModel
class NotificationSettingViewModel
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<NotificationSettingUiState, NotificationSettingSideEffect> {
        override val container: Container<NotificationSettingUiState, NotificationSettingSideEffect> =
            container(NotificationSettingUiState.Success(MockNotificationSetting))

        fun toggleMorningReport() = updateModel { copy(morningReportEnabled = !morningReportEnabled) }

        fun updateMorningReportTime(
            hour: Int,
            minute: Int,
        ) = updateModel { copy(morningReportHour = hour, morningReportMinute = minute) }

        fun toggleTodakAlarm() = updateModel { copy(todakAlarmEnabled = !todakAlarmEnabled) }

        fun toggleLuckyActionReminder() = updateModel { copy(luckyActionReminderEnabled = !luckyActionReminderEnabled) }

        private fun updateModel(transform: NotificationSettingUiModel.() -> NotificationSettingUiModel) =
            intent {
                val currentState = state as? NotificationSettingUiState.Success ?: return@intent
                reduce { currentState.copy(model = currentState.model.transform()) }
            }
    }

private val MockNotificationSetting =
    NotificationSettingUiModel(
        morningReportEnabled = true,
        morningReportHour = 8,
        morningReportMinute = 0,
        todakAlarmEnabled = true,
        luckyActionReminderEnabled = true,
    )
