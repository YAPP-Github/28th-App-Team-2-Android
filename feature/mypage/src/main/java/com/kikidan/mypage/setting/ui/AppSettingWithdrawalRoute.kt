package com.kikidan.mypage.setting.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppSettingWithdrawalRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNextClick: (reason: String, detailReason: String) -> Unit = { _, _ -> },
) {
    AppSettingWithdrawalScreen(
        modifier = modifier,
        onBackClick = onNavigateBack,
        onNextClick = onNextClick,
    )
}
