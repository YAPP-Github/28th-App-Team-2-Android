package com.kikidan.mypage.notification

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.notification.NotificationSetting
import com.kikidan.domain.usecase.notification.GetNotificationSettingUseCase
import com.kikidan.domain.usecase.notification.UpdateNotificationSettingUseCase
import com.kikidan.mypage.notification.model.NotificationSettingSideEffect
import com.kikidan.mypage.notification.model.NotificationSettingUiModel
import com.kikidan.mypage.notification.model.NotificationSettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel
    @Inject
    constructor(
        private val getNotificationSettingUseCase: GetNotificationSettingUseCase,
        private val updateNotificationSettingUseCase: UpdateNotificationSettingUseCase,
    ) : ViewModel(),
        ContainerHost<NotificationSettingUiState, NotificationSettingSideEffect> {
        override val container: Container<NotificationSettingUiState, NotificationSettingSideEffect> =
            container(NotificationSettingUiState.Loading) {
                loadNotificationSetting()
            }

        fun loadNotificationSetting() =
            intent {
                getNotificationSettingUseCase()
                    .onSuccess { setting ->
                        reduce { NotificationSettingUiState.Success(setting.toUiModel()) }
                    }.onFailure { throwable ->
                        reduce { NotificationSettingUiState.Fail(throwable) }
                    }
            }

        fun toggleMorningReport() = updateModel { copy(morningReportEnabled = !morningReportEnabled) }

        fun updateMorningReportTime(time: LocalTime) = updateModel { copy(morningReportTime = time) }

        fun toggleTodaki() = updateModel { copy(todakiEnabled = !todakiEnabled) }

        fun toggleLuckyActionReminder() = updateModel { copy(luckyActionReminderEnabled = !luckyActionReminderEnabled) }

        private fun updateModel(transform: NotificationSettingUiModel.() -> NotificationSettingUiModel) =
            intent {
                val currentState = state as? NotificationSettingUiState.Success ?: return@intent
                val updated = currentState.model.transform()
                reduce { currentState.copy(model = updated) }
                updateNotificationSettingUseCase(updated.toDomain())
            }
    }

private fun NotificationSetting.toUiModel() =
    NotificationSettingUiModel(
        morningReportEnabled = morningReportEnabled,
        morningReportTime = morningReportTime,
        todakiEnabled = todakiEnabled,
        luckyActionReminderEnabled = luckyActionReminderEnabled,
    )

private fun NotificationSettingUiModel.toDomain() =
    NotificationSetting(
        morningReportEnabled = morningReportEnabled,
        morningReportTime = morningReportTime,
        todakiEnabled = todakiEnabled,
        luckyActionReminderEnabled = luckyActionReminderEnabled,
    )
