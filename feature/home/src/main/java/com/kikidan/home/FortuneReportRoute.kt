package com.kikidan.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.home.model.FortuneReportSideEffect
import com.kikidan.home.model.HomeSideEffect
import com.kikidan.home.screen.FortuneReportScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FortuneReportRoute(
    fortuneId: String,
    snackbarHostState: SnackbarHostState,
    onNavigateToBack: () -> Unit,
    onNavigateToLuckAction: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FortuneReportViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.home_default_error)
    LaunchedEffect(fortuneId) { viewModel.load(fortuneId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is FortuneReportSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    FortuneReportScreen(
        state = state,
        onBackClick = onNavigateToBack,
        onNavigateToLuckAction = onNavigateToLuckAction,
        onNavigateToChat = onNavigateToChat,
        modifier = modifier,
    )
}
