package com.kikidan.sajucontents

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.model.YearFortuneSideEffect
import com.kikidan.sajucontents.screen.YearSelectionScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun YearSelectionRoute(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (year: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: YearFortuneViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val loadErrorMessage = stringResource(R.string.year_fortune_load_error)

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is YearFortuneSideEffect.NavigateToResult -> onNavigateToResult(effect.year)
            YearFortuneSideEffect.ShowError -> snackbarHostState.showSnackbar(loadErrorMessage)
        }
    }

    YearSelectionScreen(
        state = state,
        onBackClick = onNavigateBack,
        onYearSelect = viewModel::onYearSelect,
        onSubmit = viewModel::onSubmit,
        modifier = modifier,
    )
}
