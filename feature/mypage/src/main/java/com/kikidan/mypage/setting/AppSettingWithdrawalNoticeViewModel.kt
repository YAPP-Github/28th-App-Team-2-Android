package com.kikidan.mypage.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.user.WithdrawalReason
import com.kikidan.domain.usecase.user.WithdrawUserUseCase
import com.kikidan.mypage.setting.model.AppSettingWithdrawalNoticeSideEffect
import com.kikidan.mypage.setting.model.AppSettingWithdrawalNoticeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AppSettingWithdrawalNoticeViewModel
    @Inject
    constructor(
        private val withdrawUserUseCase: WithdrawUserUseCase,
    ) : ViewModel(),
        ContainerHost<AppSettingWithdrawalNoticeUiState, AppSettingWithdrawalNoticeSideEffect> {
        override val container: Container<AppSettingWithdrawalNoticeUiState, AppSettingWithdrawalNoticeSideEffect> =
            container(AppSettingWithdrawalNoticeUiState.Idle)

        fun withdraw(
            reason: WithdrawalReason,
            detail: String,
        ) = intent {
            if (state is AppSettingWithdrawalNoticeUiState.Loading) return@intent
            reduce { AppSettingWithdrawalNoticeUiState.Loading }
            withdrawUserUseCase(reason, detail.ifBlank { null })
                .onSuccess {
                    Log.e("moony", "Withdraw success")
                    postSideEffect(AppSettingWithdrawalNoticeSideEffect.WithdrawalSucceeded)
                }.onFailure { throwable ->
                    Log.e("moony", "Withdraw fail: $throwable")
                    reduce { AppSettingWithdrawalNoticeUiState.Failure }
                }
        }
    }
