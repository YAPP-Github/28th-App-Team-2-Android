package com.kikidan.luckaction

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.luckaction.model.LuckActionSideEffect
import com.kikidan.luckaction.screen.LuckActionScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LuckActionRoute(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: LuckActionViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.luck_action_default_error)

    LaunchedEffect(Unit) { viewModel.load() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is LuckActionSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    LuckActionScreen(
        state = state,
        onToggleAction = viewModel::onToggleAction,
        onPrevDateClick = viewModel::onPrevDateClick,
        onNextDateClick = viewModel::onNextDateClick,
        onCompleteOverlayDismiss = viewModel::onCompleteOverlayDismiss,
        modifier = modifier,
    )
}
