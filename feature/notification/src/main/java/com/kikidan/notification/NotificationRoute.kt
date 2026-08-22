package com.kikidan.notification

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.notification.model.NotificationSideEffect
import com.kikidan.notification.screen.NotificationScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    onNavigateToDeepLink: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.notification_default_error)

    LaunchedEffect(Unit) { viewModel.load() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is NotificationSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
            is NotificationSideEffect.NavigateToDeepLink -> onNavigateToDeepLink(effect.deepLink)
        }
    }

    NotificationScreen(
        state = state,
        onBackClick = onBackClick,
        onNotificationClick = viewModel::onNotificationClick,
        modifier = modifier,
    )
}
