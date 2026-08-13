package com.kikidan.mypage.notification.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.notification.NotificationSettingViewModel
import com.kikidan.mypage.notification.model.NotificationSettingSideEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NotificationSettingRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NotificationSettingSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    NotificationSettingScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onMorningReportToggle = viewModel::toggleMorningReport,
        onMorningReportTimeChange = viewModel::updateMorningReportTime,
        onTodakiToggle = viewModel::toggleTodaki,
        onLuckyActionReminderToggle = viewModel::toggleLuckyActionReminder,
    )
}
