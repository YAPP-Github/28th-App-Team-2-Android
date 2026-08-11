package com.kikidan.sajucontents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.model.DateFortuneSideEffect
import com.kikidan.sajucontents.screen.DateFortuneInputScreen
import kotlinx.collections.immutable.ImmutableList
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DateFortuneInputRoute(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DateFortuneViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val maxDateSelectionMessage = stringResource(R.string.date_fortune_max_dates_toast)
    val submitErrorMessage = stringResource(R.string.date_fortune_submit_error)

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is DateFortuneSideEffect.ShowToast -> {
                snackbarHostState.showSnackbar(maxDateSelectionMessage)
            }

            is DateFortuneSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(submitErrorMessage)
            }

            is DateFortuneSideEffect.NavigateToResult -> {
                onNavigateToResult(effect.id)
            }

            DateFortuneSideEffect.NavigateBack -> {
                onNavigateBack()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        DateFortuneInputScreen(
            state = state,
            onPurposeSelect = viewModel::selectPurpose,
            onOpenDateSheet = viewModel::openDateSheet,
            onCloseDateSheet = viewModel::closeDateSheet,
            onDateToggle = viewModel::toggleDate,
            onDateRemove = viewModel::removeDate,
            onReset = viewModel::reset,
            onSubmit = viewModel::submit,
        )
    }
}
