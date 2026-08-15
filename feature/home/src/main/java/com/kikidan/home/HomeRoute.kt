package com.kikidan.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.home.model.HomeSideEffect
import com.kikidan.home.screen.HomeScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun HomeRoute(
    snackbarHostState: SnackbarHostState,
    onNavigateToLuckAction: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.home_default_error)

    LaunchedEffect(Unit) { viewModel.load() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is HomeSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    HomeScreen(
        state = state,
        onCategoryClick = viewModel::openDetail,
        onDetailDismiss = viewModel::closeDetail,
        onNavigateToReport = onNavigateToReport,
        onNavigateToLuckAction = onNavigateToLuckAction,
        modifier = modifier,
    )
}
