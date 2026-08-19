package com.kikidan.mypage.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.user.WithdrawalReason
import com.kikidan.domain.usecase.user.WithdrawUserUseCase
import com.kikidan.mypage.setting.model.AppSettingWithdrawalNoticeSideEffect
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
        ContainerHost<Unit, AppSettingWithdrawalNoticeSideEffect> {
        override val container: Container<Unit, AppSettingWithdrawalNoticeSideEffect> = container(Unit)

        fun withdraw(
            reason: WithdrawalReason,
            detail: String,
        ) = intent {
            withdrawUserUseCase(reason, detail.ifBlank { null })
                .onSuccess {
                    Log.e("moony", "Withdraw success")
                    postSideEffect(AppSettingWithdrawalNoticeSideEffect.WithdrawalSucceeded)
                }.onFailure { throwable ->
                    Log.e("moony", "Withdraw fail: $throwable")
                }
        }
    }
