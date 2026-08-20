package com.kikidan.mypage.setting.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.kikidan.domain.model.onboarding.OnboardingTerm

@Composable
fun AppSettingRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onWithdrawalClick: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    AppSettingScreen(
        modifier = modifier,
        onBackClick = onNavigateBack,
        onPrivacyPolicyClick = { uriHandler.openUri(OnboardingTerm.PRIVACY.link) },
        onTermsOfServiceClick = { uriHandler.openUri(OnboardingTerm.SERVICE.link) },
        onWithdrawalClick = onWithdrawalClick,
    )
}
