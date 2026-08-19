package com.kikidan.mypage.setting.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppSettingRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsOfServiceClick: () -> Unit = {},
    onWithdrawalClick: () -> Unit = {},
) {
    AppSettingScreen(
        modifier = modifier,
        onBackClick = onNavigateBack,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onTermsOfServiceClick = onTermsOfServiceClick,
        onWithdrawalClick = onWithdrawalClick,
    )
}
