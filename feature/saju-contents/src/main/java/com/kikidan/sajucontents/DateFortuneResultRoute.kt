package com.kikidan.sajucontents

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.sajucontents.model.DateFortuneResultSideEffect
import com.kikidan.sajucontents.screen.DateFortuneResultScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DateFortuneResultRoute(
    ids: List<String>,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DateFortuneResultViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val loadErrorMessage = stringResource(R.string.date_fortune_result_load_error)

    LaunchedEffect(ids) {
        viewModel.loadResults(ids)
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            DateFortuneResultSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(loadErrorMessage)
            }
        }
    }

    DateFortuneResultScreen(
        state = state,
        onBackClick = onNavigateBack,
        onTabSelect = viewModel::selectTabResult,
        onShareClick = {},
        onExportClick = {},
        onAskTodakClick = onAskTodakClick,
        modifier = modifier,
    )
}
