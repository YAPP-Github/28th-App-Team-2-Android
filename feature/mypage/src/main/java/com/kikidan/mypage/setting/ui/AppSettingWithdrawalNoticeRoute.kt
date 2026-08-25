package com.kikidan.mypage.setting.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.mypage.setting.AppSettingWithdrawalNoticeViewModel
import com.kikidan.mypage.setting.model.AppSettingWithdrawalNoticeSideEffect
import com.kikidan.mypage.setting.model.AppSettingWithdrawalNoticeUiState
import com.kikidan.mypage.setting.model.toWithdrawalReason
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AppSettingWithdrawalNoticeRoute(
    onNavigateBack: () -> Unit,
    reason: String,
    detailReason: String,
    modifier: Modifier = Modifier,
    onWithdrawalSuccess: () -> Unit = {},
    viewModel: AppSettingWithdrawalNoticeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()
    val reasonItems = stringArrayResource(R.array.wheel_picker_withdrawal_reasons)
    val withdrawalReason = reasonItems.toList().toWithdrawalReason(reason)

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AppSettingWithdrawalNoticeSideEffect.WithdrawalSucceeded -> onWithdrawalSuccess()
        }
    }

    AppSettingWithdrawalNoticeScreen(
        isWithdrawing = uiState is AppSettingWithdrawalNoticeUiState.Loading,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onWithdrawConfirm = {
            if (withdrawalReason != null) {
                viewModel.withdraw(withdrawalReason, detailReason)
            }
        },
    )
}
